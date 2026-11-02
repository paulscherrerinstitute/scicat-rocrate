# SciCat RO-Crate profile

This page describes the RO-Crates the SciCat RO-Crate service imports, and what they become in SciCat.
The shared minimum for publications is described in the project's design document (D1.1). This profile
follows it, and is stricter where SciCat needs it: it requires more properties, and accepts only
`Collection` as the type of a publication.

Before importing, check a crate with `POST /api/v1/ro-crate/validate`: it runs the same checks as the
import and stores nothing.

## Contents

1. [Zip layout](#zip-layout)
2. [Finding what to import](#finding-what-to-import)
3. [Publications](#publications)
4. [Datasets](#datasets)
5. [People and organisations](#people-and-organisations)
6. [Scientific metadata](#scientific-metadata)
7. [Examples](#examples)
8. [Validating and importing](#validating-and-importing)

## Zip layout

```
crate.zip
├── ro-crate-metadata.json     at the root of the archive, not inside a folder
└── data/
    └── …                      every file and directory referenced in the metadata
```

- `ro-crate-metadata.json` must be at the root of the zip.
- Every file (`File` / `MediaObject`) and directory (`Dataset`) referenced with a local `@id` must be in the
  zip. Otherwise validation reports a `MissingDataError`.
- Files in the zip that the metadata doesn't reference aren't imported.
- Paths are relative to the zip root. Paths that leave the crate (`../`) and paths longer than the configured
  limits (by default 4096 bytes per path and 256 bytes per path segment) are rejected.

The metadata can also be sent on its own (`Content-Type: application/ld+json`). Publications can be imported
that way; datasets need the zip, since their data is in it.

Both `http://schema.org/` and `https://schema.org/` terms are accepted, so the standard RO-Crate context
(`https://w3id.org/ro/crate/1.1/context`) works.

## Finding what to import

The crate must have exactly one metadata descriptor (`ro-crate-metadata.json`, typed `CreativeWork`), whose
`about` points to exactly one root data entity typed `Dataset`.

The service then follows the root's `hasPart`:

- an entity typed `Collection` is imported as a **publication**;
- an entity typed `Dataset` is imported as a **dataset**;
- any other entity is walked through: its own `hasPart` is followed in the same way.

The walk stops at publications and datasets: the parts of a publication or of a dataset aren't imported on
their own. Entities that can't be reached from the root are ignored. A crate in which nothing can be imported
is reported with `NoEntityFound`.

## Publications

A publication is an entity typed `Collection`. It becomes a SciCat published data record.

| Property | Required | Value | Becomes in SciCat |
|---|:---:|---|---|
| `name` | ✔ | text | title |
| `abstract` | ✔ | text | abstract |
| `description` | ✔ | text | DataCite description |
| `creator` | ✔ | one or more [people](#people-and-organisations), each with at least one `affiliation` | DataCite creators |
| `hasPart` | ✔ | see below | the files of the publication |
| `identifier` | | at most one; a DOI (`10.xxxx/…` or `https://doi.org/10.xxxx/…`) | DataCite related identifier (`IsIdenticalTo`) |
| `publisher` | | an [organisation](#people-and-organisations) | not stored |
| `datePublished`, `dateCreated`, `dateModified` | | text | not stored |

**`hasPart`** must lead, directly or through other entities, to at least one file (`File` / `MediaObject`)
or directory (`Dataset`).

- The **files** become one SciCat dataset, containing exactly those files.
- **Directories** (`Dataset`) in a publication's `hasPart` must have a `name`. They aren't imported as part of
  the publication: a directory listed in the root's `hasPart` is imported as a [dataset](#datasets) on its own,
  without a link to the publication.
- If the publication has no files, SciCat gets a dataset named "Original RO-Crate" that contains only
  `ro-crate-metadata.json`.

**Duplicates.** If `identifier` holds a DOI that has already been imported, the import is rejected with
`409 Conflict`.

## Datasets

A dataset is an entity typed `Dataset` that is reached from the root's `hasPart`. It becomes a SciCat dataset
of type `base`.

| Property | Required | Value | Becomes in SciCat |
|---|:---:|---|---|
| `@id` | ✔ | the directory's path in the zip, for example `data/` | the directory whose content is ingested |
| `name` | ✔ | text | dataset name |
| `description` | | text | description |
| `creator` | | [people](#people-and-organisations) (no affiliation needed) | contact email: the creators' emails |
| `variableMeasured` | | [scientific metadata](#scientific-metadata) entries | scientific metadata |
| `identifier`, `size` | | | not stored |

- **The `@id` must be a directory of the zip.** All the files in that directory are ingested, whether or not
  the metadata lists them. Don't use an `@id` like `#dataset` or a UUID: it doesn't point to a directory.
- The dataset's creation time is the time of the import.

## People and organisations

**Person** (`@type: Person`):

| Property | Required | Value |
|---|:---:|---|
| `name` | ✔ | full name |
| `givenName` | ✔ | text |
| `familyName` | ✔ | text |
| `email` | ✔ | text |
| `affiliation` | for creators of a publication | one or more organisations |

**Organisation** (`@type: Organization`):

| Property | Required | Value |
|---|:---:|---|
| `name` | ✔ | text |

If an organisation's `@id` is a ROR URL (for example `https://ror.org/03eh3y714`), it's stored as the DataCite
affiliation identifier.

**References.** `creator`, `affiliation` and `publisher` must point to an entity, for example
`{ "@id": "https://ror.org/03eh3y714" }`, not to a plain text such as `"PSI"`. Text properties (`name`,
`email`, …) must be plain values, not entities.

## Scientific metadata

A dataset's `variableMeasured` is a list of `PropertyValue` entries, which become the dataset's scientific
metadata:

| Property | Required | Value |
|---|:---:|---|
| `name` | ✔ | the key in the scientific metadata |
| `value` | ✔ | a text, number or boolean, a nested `PropertyValue`, or a list of them |
| `unitText` | | a unit |

They're converted as follows:

| Entry | Scientific metadata |
|---|---|
| `{ "name": "energy", "value": 12.4 }` | `{ "energy": 12.4 }` |
| `{ "name": "energy", "value": 12.4, "unitText": "keV" }` | `{ "energy": { "value": 12.4, "unit": "keV" } }` |
| `{ "name": "sample", "value": { "name": "id", "value": "S-1" } }` | `{ "sample": { "id": "S-1" } }` |
| `{ "name": "angles", "value": [10, 20] }` | `{ "angles": [10, 20] }` |

A list made only of nested entries becomes an object; a list that mixes them with plain values becomes an
array. Values that are neither a plain value nor a `PropertyValue` are dropped.

## Examples

### A publication with two files

```json
{
  "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [
    {
      "@id": "ro-crate-metadata.json",
      "@type": "CreativeWork",
      "conformsTo": { "@id": "https://w3id.org/ro/crate/1.1" },
      "about": { "@id": "./" }
    },
    {
      "@id": "./",
      "@type": "Dataset",
      "name": "Tomography of a rat lung",
      "hasPart": [{ "@id": "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5" }]
    },
    {
      "@id": "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
      "@type": "Collection",
      "identifier": "10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
      "name": "Tomography of a rat lung",
      "abstract": "Full-volume X-ray tomographic reconstruction of an intact rat lung.",
      "description": "Raw projections and reconstructed slices.",
      "creator": [{ "@id": "#jane" }],
      "hasPart": [{ "@id": "data/projections.h5" }, { "@id": "data/slices.h5" }]
    },
    {
      "@id": "#jane",
      "@type": "Person",
      "name": "Jane Doe",
      "givenName": "Jane",
      "familyName": "Doe",
      "email": "jane.doe@example.com",
      "affiliation": { "@id": "https://ror.org/03eh3y714" }
    },
    { "@id": "https://ror.org/03eh3y714", "@type": "Organization", "name": "Paul Scherrer Institute" },
    { "@id": "data/projections.h5", "@type": "File" },
    { "@id": "data/slices.h5", "@type": "File" }
  ]
}
```

### A dataset with scientific metadata

```json
{
  "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [
    {
      "@id": "ro-crate-metadata.json",
      "@type": "CreativeWork",
      "conformsTo": { "@id": "https://w3id.org/ro/crate/1.1" },
      "about": { "@id": "./" }
    },
    { "@id": "./", "@type": "Dataset", "name": "Beamtime", "hasPart": [{ "@id": "data/" }] },
    {
      "@id": "data/",
      "@type": "Dataset",
      "name": "Beamtime 2026-10",
      "description": "Measurements of the LaB6 reference sample.",
      "creator": { "@id": "#jane" },
      "variableMeasured": [
        { "@type": "PropertyValue", "name": "energy", "value": 12.4, "unitText": "keV" },
        {
          "@type": "PropertyValue",
          "name": "sample",
          "value": { "@type": "PropertyValue", "name": "id", "value": "LaB6" }
        }
      ]
    },
    {
      "@id": "#jane",
      "@type": "Person",
      "name": "Jane Doe",
      "givenName": "Jane",
      "familyName": "Doe",
      "email": "jane.doe@example.com"
    }
  ]
}
```

## Validating and importing

```console
cd crate && zip -r ../crate.zip . && cd ..

curl -X POST https://<service>/api/v1/ro-crate/validate \
  -H 'Content-Type: application/zip' --data-binary @crate.zip

curl -X POST https://<service>/api/v1/ro-crate/import \
  -H 'Content-Type: application/zip' \
  -H 'api-key: <SciCat token>' \
  -H 'ownerGroup: <group>' \
  --data-binary @crate.zip
```

`ownerGroup` is optional; it defaults to the first access group of the user.

**A successful import** (`201 Created`) returns the identifiers SciCat created, by crate `@id`: the DOI of a
publication, and the PID of the dataset that holds its files, or of an imported dataset.

```json
{
  "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5": "10.16907/…",
  "data/projections.h5": "PID.SAMPLE.PREFIX/1b6d…",
  "data/slices.h5": "PID.SAMPLE.PREFIX/1b6d…"
}
```

For a publication without files, the placeholder dataset is listed under `"ro-crate-metadata.json"`.

**A crate that fails validation** returns a validation report:

```json
{
  "isValid": false,
  "entities": [],
  "errors": [
    {
      "type": "PropertyError",
      "nodeId": "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
      "property": "https://schema.org/name",
      "message": "Missing required property"
    },
    {
      "type": "MissingDataError",
      "message": "The path 'data/slices.h5' is referenced in the metadata descriptor but absent from the archive"
    }
  ]
}
```

| Error `type` | Meaning |
|---|---|
| `PropertyError` | A required property is missing, has too many values, or an entity has the wrong `@type` |
| `MissingDataError` | A file or directory referenced in the metadata isn't in the zip |
| `NoEntityFound` | Nothing in the crate can be imported |

**Status codes:**

| Status | When | Body |
|---|---|---|
| `200` | `validate`: the result, valid or not | validation report |
| `201` | `import`: the crate was imported | identifiers by `@id` |
| `400` | the crate fails validation; or the body isn't valid JSON-LD or a valid zip | validation report, or `{"message": …}` |
| `401` | missing or invalid `api-key` | |
| `403` | the user isn't in any group, and no `ownerGroup` was given | |
| `409` | the publication's DOI has already been imported | `{"message": …}` |
| `422` | no metadata descriptor, several descriptors, or no root data entity | `{"message": …}` |
