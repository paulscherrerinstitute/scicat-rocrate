# SciCat - ORD RO-Crate mappings

## Publications

| Datacite                                                                                                             | Schema.org                                             | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks                                                         |
|----------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------|--------------------------|-------------------|-----------------------------------------------------------------|
| [1. identifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/identifier)                        | `@id`<br>[`identifier`](https://schema.org/identifier) | x                 | `_id` <br> `doi`         | x (only `doi`)    | Should we make sure the format is valid?                        |
| [1.a identifierType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/identifier/#a-identifiertype) | N/A                                                    |                   |                          |                   | Only one possbile value in DataCite so we can assume it's a DOI |
<table>
<tr>
<td> DataCite </td> <td> Schema.org </td> <td> SciCat </td>
</tr>
<tr>
<td>

```xml
<identifier identifierType="DOI">10.21384/foo</identifier>
```

</td>
<td>

```json
"@id": "10.21384/foo",
"identifier": "10.21384/foo"
```

</td>
<td>

```json
"_id": "10.21384/foo",
"doi": "10.21384/foo"
```

</td>
</tr>
</table>

| Datacite                                                                                                                                      | Schema.org                                                   | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks                                                                          |
|-----------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|-------------------|--------------------------|-------------------|----------------------------------------------------------------------------------|
| [2 creator](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/)                                                       | [`creator`](https://schema.org/creator)                      | x                 | `creator`                | x                 |                                                                                  |
| [2.1 creatorName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#creatorname)                                     | [`creator.name`](https://schema.org/name)                    | x                 |                          |                   |                                                                                  |
| [2.1.a nameType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#a-nametype)                                       | `creator.@type`                                              | x                 |                          |                   | Can be either a `Person` or an `Organization`, do we want to support the latter? |
| [2.2 givenName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#givenname)                                         | [`creator.givenName`](https://schema.org/givenName)          | x                 |                          |                   | :warning: Not in SciCat                                                          |
| [2.3 familyName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#familyname)                                       | [`creator.familyName`](https://schema.org/familyName)        | x                 |                          |                   | :warning: Not in SciCat                                                          |
| [2.4 nameIdentifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#nameidentifier)                               | `creator.@id`                                                |                   |                          |                   | Only ORCID supported?                                                            |
| [2.4.a nameIdentifierScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#a-nameidentifierscheme)               | N/A                                                          |                   |                          |                   |                                                                                  |
| [2.4.b schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#b-schemeuri)                                     | N/A                                                          |                   |                          |                   |                                                                                  |
| [2.5 affiliation](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#affiliation)                                     | [`creator.affiliation.name`](https://schema.org/affiliation) | x                 |                          |                   | :warning: Not in SciCat                                                          |
| [2.5.a affiliationIdentifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#a-affiliationidentifier)             | `creator.affiliation.@id`                                    |                   |                          |                   | :warning: Not in SciCat                                                          |
| [2.5.b affiliationIdentifierScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#b-affiliationidentifierscheme) | N/A                                                          |                   |                          |                   |                                                                                  |
| [2.5.c schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/creator/#c-schemeuri)                                     | N/A                                                          |                   |                          |                   |                                                                                  |
<table>
<tr>
<td> DataCite </td> <td> Schema.org </td> <td> SciCat </td>
</tr>
<tr>
<td>

```xml
<creators>
  <creator>
    <creatorName nameType="Personal">
      Garcia, Sofia
    </creatorName>
    <givenName>Sofia</givenName>
    <familyName>Garcia</familyName>
    <nameIdentifier schemeURI="https://orcid.org/" nameIdentifierScheme="ORCID">
      0000-0001-5727-2427
    </nameIdentifier>
    <affiliation affiliationIdentifier="https://ror.org/03efmqc40" affiliationIdentifierScheme="ROR" schemeURI="https://ror.org">
      Arizona State University
    </affiliation>
  </creator>
</creators>
```

</td>
<td>

```json
"creator": [
  {
    "@id": "https://orcid.org/0000-0001-5727-2427",
    "@type": "Person",
    "name": "Garcia, Sofia",
    "givenName": "Sofia",
    "familyName": "Garcia",
    "affiliation": {
      "@id": "https://ror.org/03yrm5c26",
      "@type": "Organization",
      "name": "Arizona State University"
    }
  }
]
```

</td>
<td>

```json
"creator": [
  "Garcia, Sofia"
]
```

</td>
</tr>
</table>

| Datacite                                                                                              | Schema.org                          | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks                                    |
|-------------------------------------------------------------------------------------------------------|-------------------------------------|-------------------|--------------------------|-------------------|--------------------------------------------|
| [3 title](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/title/)                   | [`title`](https://schema.org/title) | x                 | `title`                  |                   | Can we assume title are always in english? |
| [3.1 titleType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/title/#a-titletype) | N/A                                 |                   |                          |                   |                                            |
<table>
<tr>
<td> DataCite </td> <td> Schema.org </td> <td> SciCat </td>
</tr>
<tr>
<td>

```xml
<titles>
    <title xml:lang="en">Example title</title>
</titles>
```

</td>
<td>

```json
"title": "Example title"
```

</td>
<td>

```json
"title": "Example title"
```

</td>
</tr>
</table>

| Datacite                                                                                                                                  | Schema.org                                       | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|-------------------|--------------------------|-------------------|---------|
| [4 publisher](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publisher/)                                               | [`publisher.name`](https://schema.org/publisher) | x                 | `publisher`              | x                 |         |
| [4.a publisherIdentifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publisher/#a-publisheridentifier)             | `publisher.@id`                                  | x                 |                          |                   |         |
| [4.b publisherIdentifierScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publisher/#b-publisheridentifierscheme) | N/A                                              | x                 |                          |                   |         |
| [4.c schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publisher/#c-schemeuri)                                 | N/A                                              | x                 |                          |                   |         |
<table>
<tr>
<td> DataCite </td> <td> Schema.org </td> <td> SciCat </td>
</tr>
<tr>
<td>

```xml
<publisher xml:lang="en" publisherIdentifier="https://ror.org/04z8jg394" publisherIdentifierScheme="ROR" schemeURI="https://ror.org/">Helmholtz Centre Potsdam - GFZ German Research Centre for Geosciences</publisher>
```

</td>
<td>

```json
"publisher": {
  "@id": "https://ror.org/04z8jg394",
  "@type": "Organization",
  "@name": "Helmholtz Centre Potsdam - GFZ German Research Centre for Geosciences"
}
```

</td>
<td>

```json
"publisher": "Helmholtz Centre Potsdam - GFZ German Research Centre for Geosciences"
```

</td>
</tr>
</table>

| Datacite                                                                                                | Schema.org                                          | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks                                                                 |
|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------|-------------------|--------------------------|-------------------|-------------------------------------------------------------------------|
| [5 publicationYear](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/publicationyear/) | [`datePublished`](https://schema.org/datePublished) | x                 | `publicationYear`        | x                 | Schema.org expects a full ISO 8601 date but SciCat only stores the year |

| Datacite                                                                                                                  | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks                                  |
|---------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|------------------------------------------|
| [6. subject](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/subject/)                                  |            |                   |                          |                   | Keywords are stored at the dataset level |
| [6.a subjectScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/subject/#a-subjectscheme)           |            |                   |                          |                   |                                          |
| [6.b schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/subject/#b-schemeuri)                   |            |                   |                          |                   |                                          |
| [6.c valueURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/subject/#c-valueuri)                     |            |                   |                          |                   |                                          |
| [6.d classificationCode](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/subject/#d-classificationcode) |            |                   |                          |                   |                                          |

| Datacite                                                                                                                                          | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|---------------------------------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [7. contributor](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/)                                                  |            |                   |                          |                   |         |
| [7.a contributorType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#a-contributortype)                           |            |                   |                          |                   |         |
| [7.1 contributorName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#contributorname)                             |            |                   |                          |                   |         |
| [7.1.a nameType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#a-nametype)                                       |            |                   |                          |                   |         |
| [7.2 givenName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#givenname)                                         |            |                   |                          |                   |         |
| [7.3 familyName](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#familyname)                                       |            |                   |                          |                   |         |
| [7.4 nameIdentifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#nameidentifier)                               |            |                   |                          |                   |         |
| [7.4.a nameIdentifierScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#a-nameidentifierscheme)               |            |                   |                          |                   |         |
| [7.4.b schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#b-schemeuri)                                     |            |                   |                          |                   |         |
| [7.5 affiliation](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#affiliation)                                     |            |                   |                          |                   |         |
| [7.5.a affiliationIdentifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#a-affiliationidentifier)             |            |                   |                          |                   |         |
| [7.5.b affiliationIdentifierScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#b-affiliationidentifierscheme) |            |                   |                          |                   |         |
| [7.5.c schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/contributor/#c-schemeuri)                                     |            |                   |                          |                   |         |

| Datacite                                                                                                         | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [8. date](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/date/)                               |            |                   |                          |                   |         |
| [8.a dateType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/date/#a-datetype)               |            |                   |                          |                   |         |
| [8.b dateInformation](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/date/#b-dateinformation) |            |                   |                          |                   |         |

| Datacite                                                                                   | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|--------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [9. language](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/language/) |            |                   |                          |                   |         |

| Datacite                                                                                                                          | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-----------------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [10. resource type](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/resourcetype/)                              |            |                   |                          |                   |         |
| [10.a resourceTypeGeneral](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/resourcetype/#a-resourcetypegeneral) |            |                   |                          |                   |         |

| Datacite                                                                                                                                         | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|--------------------------------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [11. alternate identifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/alternateidentifier/)                               |            |                   |                          |                   |         |
| [11.a alternateIdentifierType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/alternateidentifier/#a-alternateidentifiertype) |            |                   |                          |                   |         |

| Datacite                                                                                                                                   | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|--------------------------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [12. related identifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/)                             |            |                   |                          |                   |         |
| [12.a relatedIdentiferType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/#a-relatedidentifiertype)  |            |                   |                          |                   |         |
| [12.b relationType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/#b-relationtype)                   |            |                   |                          |                   |         |
| [12.c relatedMetadataScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/#c-relatedmetadatascheme) |            |                   |                          |                   |         |
| [12.d schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/#d-schemeuri)                         |            |                   |                          |                   |         |
| [12.e schemeType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/#e-schemetype)                       |            |                   |                          |                   |         |
| [12.f resourceTypeGeneral](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relatedidentifier/#f-resourcetypegeneral)     |            |                   |                          |                   |         |

| Datacite                                                                            | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [13. size](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/size/) |            |                   |                          |                   |         |

| Datacite                                                                                | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-----------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [14. format](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/format/) |            |                   |                          |                   |         |

| Datacite                                                                                  | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [15. version](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/version/) |            |                   |                          |                   |         |

| Datacite                                                                                                                          | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-----------------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [16. rights](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/rights/)                                           |            |                   |                          |                   |         |
| [16.a rightsURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/rights/#a-rightsuri)                           |            |                   |                          |                   |         |
| [16.b rightsIdentifier](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/rights/#b-rightsidentifier)             |            |                   |                          |                   |         |
| [16.c rightsIdentifierScheme](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/rights/#c-rightsidentifierscheme) |            |                   |                          |                   |         |
| [16.d schemeURI](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/rights/#d-schemeuri)                           |            |                   |                          |                   |         |

| Datacite                                                                                                                 | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|--------------------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [17. description](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/description/)                        |            |                   |                          |                   |         |
| [17.a descriptionType](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/description/#a-descriptiontype) |            |                   |                          |                   |         |

| Datacite                                                                                          | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|---------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [18. geolocation](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/geolocation/) |            |                   |                          |                   |         |

| Datacite                                                                                                    | Schema.org | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks |
|-------------------------------------------------------------------------------------------------------------|------------|-------------------|--------------------------|-------------------|---------|
| [19. fundingreference](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/fundingreference/) |            |                   |                          |                   |         |

| Datacite                                                                                          | Schema.org                                    | Required (API-03) | `CreatePublishedDataDto` | Required (Scicat) | Remarks                                            |
|---------------------------------------------------------------------------------------------------|-----------------------------------------------|-------------------|--------------------------|-------------------|----------------------------------------------------|
| [20. relateditem](https://datacite-metadata-schema.readthedocs.io/en/4.6/properties/relateditem/) |                                               |                   |                          |                   |                                                    |
| N/A                                                                                               | N/A                                           |                   | `affiliation`            |                   | Affiliation is for a `Person` not a `CreativeWork` |
| TODO                                                                                              |                                               |                   |                          |                   |                                                    |
| TODO                                                                                              | [url](https://schema.org/url)                 |                   | `url`                    |                   |                                                    |
| TODO                                                                                              | [abstract](https://schema.org/abstract)       |                   | `abstract`               | x                 |                                                    |
| TODO                                                                                              | [description](https://schema.org/description) |                   | `dataDescription`        | x                 |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `resourceType`           | x                 |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `numberOfFiles`          |                   |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `sizeOfArchive`          |                   |                                                    |
| TODO                                                                                              | [hasPart](https://schema.org/hasPart)         |                   | `pidArray`               | x                 |                                                    |
| TODO                                                                                              | [author](https://schema.org/author)           |                   | `authors`                |                   |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `registeredTime`         |                   |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `status`                 |                   |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `scicatUser`             |                   |                                                    |
| TODO                                                                                              | [thumbnail](https://schema.org/thumbnail)     |                   | `thumbnail`              |                   |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `relatedPublications`    |                   |                                                    |
| TODO                                                                                              | [](https://schema.org/)                       |                   | `downloadLink`           |                   |                                                    |
