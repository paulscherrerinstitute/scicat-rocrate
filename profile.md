# SciCat RO-Crate profile

This document specifies the SciCat PublishedData profile. A RO-Crate that conforms to this profile must include "scicat:PublishedData" as a @type for each element that is a part of root data entity.

<!-- ## Context and Namespaces
```json
{
  "@context": {
    "@vocab": "https://schema.org/",
    "dcterms": "http://purl.org/dc/terms/",
    "datacite": "https://purl.org/datacite/schema/kernel-4/",
    "ro": "https://w3id.org/ro/terms#"
  }
}
``` -->

## Metadata Elements
All properties have a prefix `scicat:` (as shown in the [example](ro-crate-metadata.json)), but is omitted from the tables below for brevity.

### Required Properties

| Property          | Expected value range     | Definition                                                 | Equivalent Schema.org property                        | Equivalent Datacite property                                                                                             |
| ----------------- | ------------------------ | ---------------------------------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `doi`             | `string`                 | Digital Object Identifier for the dataset.                 | [identifier](https://schema.org/identifier)           | [identifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/identifier/)                              |
| `creator`         | `List<string>`           | List of creators of the dataset.                           | [creator > Person > name](https://schema.org/creator) | [creator#creatorName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#creatorname)            |
| `publisher`       | `string`                 | Organization or entity publishing the dataset.             | [publisher](https://schema.org/publisher)             | [publisher](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publisher/)                                |
| `publicationYear` | `number`                 | Year the dataset was published.                            | [datePublished](https://schema.org/datePublished)     | [publicationYear](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publicationyear/)                    |
| `title`           | `string`                 | Title of the dataset.                                      | [name](https://schema.org/name)                       | [title](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/title/)                                        |
| `abstract`        | `string`                 | Abstract summarizing the dataset.                          | [abstract](https://schema.org/abstract)               | [description](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/description/)                            |
| `resourceType`    | `Enum["raw", "derived"]` | Type of the dataset (e.g., raw/derived).                   | [additionalType](https://schema.org/additionalType)   | [resourceType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/resourcetype/)                          |
| `pidArray`        | `List<string>`           | Array of one or more persistent identifiers which make up the published data | [identifier](https://schema.org/identifier)           | [relatedIdentifiers](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/)               |
| `registeredTime`  | `timestamp`              | Timestamp when the DOI was registered.                     | [dateCreated](https://schema.org/dateCreated)         | [dates#date#submitted](https://datacite-metadata-schema.readthedocs.io/en/4.6/appendices/appendix-1/dateType/#submitted) |
| `status`          | `string`                 | Status in the publication workflow.                        | [status](https://schema.org/status)                   | N/A                                                                                                                      |
| `createdAt`       | `timestamp`              | Date the dataset was created (system-generated).           | [dateCreated](https://schema.org/dateCreated)         | [dates#date#created](https://datacite-metadata-schema.readthedocs.io/en/4.6/appendices/appendix-1/dateType/#created)     |
| `updatedAt`       | `timestamp`              | Date the dataset was last updated (system-generated).      | [dateModified](https://schema.org/dateModified)       | [dates#date#updated](https://datacite-metadata-schema.readthedocs.io/en/4.6/appendices/appendix-1/dateType/#updated)     |
| `dataDescription` | `string`                 | Link to a description of how to reuse the dataset.         | ?? [url](https://schema.org/url)                      | ?? [description](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/description/)                         |

### Optional Properties

| Property              | Expected value range | Definition                                                   | Equivalent Schema.org property                | Equivalent Datacite property                                                                                                           |
| --------------------- | -------------------- | ------------------------------------------------------------ | --------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `affiliation`         | `string`             | Affiliations of the dataset creators.                        | [affiliation](https://schema.org/affiliation) | [creator#affiliation](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#affiliation)                          |
| `url`                 | `string`             | Landing page URL for the dataset's DOI.                      | [url](https://schema.org/url)                 | [relatedIdentifier, relatedIdentifierType=URL](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/)   |
| `numberOfFiles`       | `number`             | Number of files included in the dataset.                     | [size](https://schema.org/size)               | [sizes](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/size/)                                                       |
| `sizeOfArchive`       | `number`             | Size of the dataset archive.                                 | [contentSize](https://schema.org/contentSize) | [sizes](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/size/)                                                       |
| `authors`             | `List<string>`       | List of contributors/authors of the dataset.                 | [contributor](https://schema.org/contributor) | [contributors](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/)                                         |
| `scicatUser`          | `string`             | Username of the person initiating publication in the system. | [accountName](https://schema.org/accountName) | ?? [contributor, contributorType=?relatedPerson?](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/)      |
| `thumbnail`           | `string`             | Thumbnail image for the dataset (base64 encoded, < 16 MB).   | [image](https://schema.org/image)             | [relatedItem, relatedItemType=Image](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relateditem/)                   |
| `relatedPublications` | `List<string>`       | List of URLs pointing to related publications.               | [isBasedOn](https://schema.org/isBasedOn)     | [relatedIdentifiers](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/)                             |
| `downloadLink`        | `string`             | URL for downloading the dataset.                             | [contentUrl](https://schema.org/contentUrl)   | [relatedIdentifier, resourceTypeGeneral=Dataset](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/) |


<!-- ## Usage Notes

- **DOI and PIDs:** The `doi` property is essential for dataset identification, while the `pidArray` allows referencing multiple related PIDs.
- **Affiliation and Contributors:** These fields enhance attribution and recognition of involved entities.
- **Thumbnails:** Should comply with the specified size limits and provide a visual summary.
- **Status Tracking:** The `status` property aids in tracking the dataset's position within the publication workflow. -->

## RO-Crate Example
A conformant RO-Crate is present in [ro-crate-metadata.json](ro-crate-metadata.json)