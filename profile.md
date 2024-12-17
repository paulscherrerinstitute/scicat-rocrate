# SciCat RO-Crate profile

This document specifies the metadata profile for representing published data within an RO-Crate package.

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

### Required Properties

| Property          | Expected value range   | Definition                                                          |
| ----------------- | ---------------------- | ------------------------------------------------------------------- |
| `doi`             | string                 | Digital Object Identifier for the dataset.                          |
| `creator`         | List<string>           | List of creators of the dataset.                                    |
| `publisher`       | string                 | Organization or entity publishing the dataset.                      |
| `publicationYear` | number                 | Year the dataset was published.                                     |
| `title`           | string                 | Title of the dataset.                                               |
| `abstract`        | string                 | Abstract summarizing the dataset.                                   |
| `resourceType`    | Enum["raw", "derived"] | Type of the dataset (e.g., raw/derived).                            |
| `pidArray`        | List<string>           | Array of persistent identifiers (PIDs) associated with the dataset. |
| `registeredTime`  | timestamp              | Timestamp when the DOI was registered.                              |
| `status`          | string                 | Status in the publication workflow.                                 |
| `createdAt`       | timestamp              | Date the dataset was created (system-generated).                    |
| `updatedAt`       | timestamp              | Date the dataset was last updated (system-generated).               |
| `dataDescription` | string                 | Link to a description of how to reuse the dataset.                  |

### Optional Properties

| Property              | Expected value range | Definition                                                   |
| --------------------- | -------------------- | ------------------------------------------------------------ |
| `affiliation`         | string               | Affiliations of the dataset creators.                        |
| `url`                 | string               | Landing page URL for the dataset's DOI.                      |
| `numberOfFiles`       | number               | Number of files included in the dataset.                     |
| `sizeOfArchive`       | number               | Size of the dataset archive.                                 |
| `authors`             | List<string>         | List of contributors/authors of the dataset.                 |
| `scicatUser`          | string               | Username of the person initiating publication in the system. |
| `thumbnail`           | string               | Thumbnail image for the dataset (base64 encoded, < 16 MB).   |
| `relatedPublications` | List<string>         | List of URLs pointing to related publications.               |
| `downloadLink`        | string               | URL for downloading the dataset.                             |

<!-- ## Usage Notes

- **DOI and PIDs:** The `doi` property is essential for dataset identification, while the `pidArray` allows referencing multiple related PIDs.
- **Affiliation and Contributors:** These fields enhance attribution and recognition of involved entities.
- **Thumbnails:** Should comply with the specified size limits and provide a visual summary.
- **Status Tracking:** The `status` property aids in tracking the dataset's position within the publication workflow. -->

## RO-Crate Example
A conformant RO-Crate is present in [ro-crate-metadata.json](ro-crate-metadata.json)