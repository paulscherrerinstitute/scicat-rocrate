# SciCat Publication RO-Crate profile

This profile describes the RO-Crates that the SciCat RO-Crate service accepts on
`POST /api/v1/ro-crate/import`. A crate that follows it is imported into SciCat as:

- one **PublishedData** record per Publication entity in the crate, and
- one **Dataset** holding the files of that Publication. These files are archived after ingestion.

It adds constraints on top of the [RO-Crate 1.1 specification](https://w3id.org/ro/crate/1.1).
Everything that RO-Crate allows and this page doesn't mention is ignored by the importer.

> **Before importing, validate.** `POST /api/v1/ro-crate/validate` runs exactly the same checks as
> the import but writes nothing to SciCat. See [Validating a crate](#validating-a-crate).

## 1. Zip layout

```
my-crate.zip
├── ro-crate-metadata.json      ← required, at the root of the zip
└── data/
    ├── file1.txt               ← every file referenced in the metadata
    └── file2.h5                  must be present in the zip
```

- `ro-crate-metadata.json` must be at the **root of the archive**. Don't wrap it in a top-level
  folder (e.g. by zipping the folder instead of its content).
- Every local data entity (`File`/`MediaObject` or `Dataset`) referenced in the metadata must be in
  the zip. Otherwise validation fails with a `MissingDataError`.
- Paths are resolved relative to the zip root. Paths that leave the crate (`../`), paths longer than
  4096 bytes, and path segments longer than 256 bytes are rejected. Deployments can change these limits.
- Files present in the zip but not referenced in the metadata are **not** ingested.

You can also send only the metadata file (`Content-Type: application/ld+json`). In that case
there is no data to check or ingest. Only the metadata is imported.

## 2. Metadata graph

The importer looks for entities typed `Collection`. Each `Collection` is one Publication. The
root data entity (`./`) and the metadata descriptor are required by RO-Crate but are not read by
the importer.

```
./ (Dataset) ──hasPart──▶ Publication (Collection) ──creator──▶ Person ──affiliation──▶ Organization
                                    │
                                    ├──publisher──▶ Organization
                                    └──hasPart──▶ File / MediaObject  (data/file1.txt, …)
```

The [JSON-LD context](#context) can be the standard RO-Crate one. Both `http://schema.org/` and
`https://schema.org/` terms are accepted.

### Publication — `@type: Collection`

| Property        | Required | Cardinality | Value                                   | Stored in SciCat as                              |
|-----------------|:--------:|-------------|-----------------------------------------|--------------------------------------------------|
| `@id`           | ✔        | 1           | any IRI, e.g. the DOI URL               | key in the import response                       |
| `name`          | ✔        | 1           | text                                    | PublishedData `title`, Dataset `datasetName`     |
| `abstract`      | ✔        | 1           | text                                    | PublishedData `abstract`                         |
| `description`   | ✔        | 1           | text                                    | DataCite description, Dataset `description`      |
| `creator`       | ✔        | 1..n        | reference to a [Person](#person--type-person) | DataCite creators, Dataset `owner` / `principalInvestigator` / `contactEmail` |
| `hasPart`       | ✔        | 1..n        | references to data entities, see [Data entities](#data-entities) | files of the ingested Dataset |
| `identifier`    |          | 0..1        | a DOI (`10.xxxx/…` or `https://doi.org/10.xxxx/…`) | DataCite related identifier `IsIdenticalTo`; used to detect duplicates |
| `publisher`     |          | 0..1        | reference to an [Organization](#organization--type-organization) | — |
| `datePublished` |          | 0..1        | ISO 8601 date or a year (`2024`)        | —                                                |
| `dateCreated`   |          | 0..1        | ISO 8601 date-time                      | —                                                |
| `dateModified`  |          | 0..1        | ISO 8601 date-time                      | —                                                |

If `identifier` holds a DOI that already exists in SciCat, the import is rejected with
`409 Conflict`.

### Person — `@type: Person`

| Property      | Required | Cardinality | Value                                                    |
|---------------|:--------:|-------------|----------------------------------------------------------|
| `@id`         | ✔        | 1           | an ORCID URL is recommended, e.g. `https://orcid.org/0000-0002-1825-0097` |
| `name`        | ✔        | 1           | full name, text                                          |
| `givenName`   | ✔        | 1           | text                                                     |
| `familyName`  | ✔        | 1           | text                                                     |
| `email`       | ✔        | 1           | text                                                     |
| `affiliation` | ✔        | 1..n        | reference to an [Organization](#organization--type-organization) (not a plain string) |
| `identifier`  |          | 0..n        | text, e.g. an ORCID                                      |

If the `@id` or one of the `identifier`s is an ORCID, it is stored as the creator's DataCite name
identifier.

### Organization — `@type: Organization`

| Property | Required | Cardinality | Value                                                     |
|----------|:--------:|-------------|-----------------------------------------------------------|
| `@id`    | ✔        | 1           | a ROR URL is recommended, e.g. `https://ror.org/03eh3y714` |
| `name`   | ✔        | 1           | text                                                      |

If the `@id` is a ROR URL, it is stored as the DataCite affiliation identifier.

### Data entities

The Publication's `hasPart` must lead to at least one `File` (`MediaObject`) or `Dataset`.

- **List each file directly** in the Publication's `hasPart` as a `File`/`MediaObject` whose `@id`
  is its path relative to the zip root (e.g. `data/file1.txt`, percent-encoded if needed). These
  are the files that get ingested.
- Files listed only inside a directory `Dataset`'s own `hasPart` are checked for presence but
  **not ingested**. Don't rely on directory entities to carry files.
- Remote files (`https://…` `@id`s) are not downloaded. Ship the data in the zip.
- If the Publication has no `File` parts at all, SciCat gets a placeholder Dataset named
  "Original RO-Crate" that contains only `ro-crate-metadata.json`.

### General rules

- Every entity must have an `@id`. Parts without one are ignored, and errors on them can't be
  located.
- Text properties (`name`, `email`, …) must be plain strings. Properties that reference another
  entity (`creator`, `affiliation`, `publisher`, `hasPart`) must be `{"@id": "…"}` references.
- Required properties that occur more often than their cardinality allows (e.g. two `identifier`s
  on a Publication) are errors.

## 3. Example

<a id="context"></a>
A minimal crate with one Publication, one creator and two files. Its zip layout is the one shown
in [section 1](#1-zip-layout).

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
      "name": "Tomography of a juvenile rat lung",
      "description": "RO-Crate wrapping one SciCat publication",
      "datePublished": "2024-05-01",
      "license": { "@id": "https://creativecommons.org/licenses/by/4.0/" },
      "hasPart": [{ "@id": "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5" }]
    },
    {
      "@id": "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
      "@type": "Collection",
      "identifier": "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
      "name": "Tomography of a juvenile rat lung",
      "abstract": "Full-volume X-ray tomographic reconstruction of an intact rat lung.",
      "description": "Raw projections and reconstructed slices.",
      "datePublished": "2024",
      "creator": [{ "@id": "https://orcid.org/0000-0002-1825-0097" }],
      "publisher": { "@id": "https://ror.org/03eh3y714" },
      "hasPart": [{ "@id": "data/file1.txt" }, { "@id": "data/file2.h5" }]
    },
    {
      "@id": "https://orcid.org/0000-0002-1825-0097",
      "@type": "Person",
      "name": "Jane Doe",
      "givenName": "Jane",
      "familyName": "Doe",
      "email": "jane.doe@example.com",
      "affiliation": { "@id": "https://ror.org/03eh3y714" }
    },
    {
      "@id": "https://ror.org/03eh3y714",
      "@type": "Organization",
      "name": "Paul Scherrer Institute"
    },
    { "@id": "data/file1.txt", "@type": "File", "name": "file1.txt" },
    { "@id": "data/file2.h5",  "@type": "File", "name": "file2.h5" }
  ]
}
```

The standard RO-Crate context is fetched by the service when the crate is parsed (with a 10 s
timeout by default). If the service can't reach `w3id.org`, inline the context instead, mapping
each term to `https://schema.org/<term>` (see
[publication-with-files.json](../src/test/resources/publication-with-files.json)).

## 4. Validating and importing

### Validating a crate

```console
cd my-crate && zip -r ../my-crate.zip . && cd ..

curl -X POST https://<service>/api/v1/ro-crate/validate \
  -H 'Content-Type: application/zip' \
  --data-binary @my-crate.zip
```

A valid crate returns the Publications that would be imported:

```json
{ "isValid": true, "errors": [], "entities": ["https://doi.org/10.16907/d910159a-..."] }
```

An invalid crate lists the problems:

```json
{
  "isValid": false,
  "errors": [
    {
      "type": "PropertyError",
      "nodeId": "https://orcid.org/0000-0002-1825-0097",
      "property": "https://schema.org/email",
      "message": "Missing required property"
    },
    {
      "type": "MissingDataError",
      "message": "The path 'data/file2.h5' is referenced in the metadata descriptor but absent from the archive"
    }
  ]
}
```

| Error type         | Meaning                                                                       |
|--------------------|-------------------------------------------------------------------------------|
| `PropertyError`    | A property on `nodeId` is missing, has too many values, or the `@type` is wrong |
| `MissingDataError` | A data entity is referenced in the metadata but not in the zip                |
| `NoEntityFound`    | The graph contains no `Collection`                                            |

Errors that prevent reading the crate at all are returned as `{"message": "…"}` with
`400 Bad Request`. For example: `Archive doesn't contain a "ro-crate-metadata.json" file`,
`Invalid or empty zip archive`, or `Failed to parse the metadata descriptor` (invalid JSON-LD).

### Importing

```console
curl -X POST https://<service>/api/v1/ro-crate/import \
  -H 'Content-Type: application/zip' \
  -H 'api-key: <SciCat token>' \
  -H 'ownerGroup: <group>' \
  --data-binary @my-crate.zip
```

- `api-key` is required (`401` otherwise).
- `ownerGroup` is optional. It defaults to the first access group of the token's user.
- An invalid crate returns `400` with the validation report shown above.
- On success (`201 Created`) the response maps each imported crate entity to the SciCat identifier
  it was assigned. Files map to the Dataset PID, and the Publication maps to the PublishedData DOI:

```json
{
  "data/file1.txt": "PID.SAMPLE.PREFIX/1b6d…",
  "data/file2.h5": "PID.SAMPLE.PREFIX/1b6d…",
  "https://doi.org/10.16907/d910159a-...": "10.16907/…"
}
```
