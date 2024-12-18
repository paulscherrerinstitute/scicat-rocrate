## SciCat RO-Crate
This repository contains scicat type definitions (schemas) and a profile specifying a conforming RO-Crate.

Type definitions are present under [scicat-types](scicat-types/). So far, only `scicat:PublishedData` type is specified. Other related types e.g. `scicat:Dataset` will be added under this directory as needed.
The profile specifies the structural and field-level constraints, provides a description of the fields, and provides a mapping to schema.org and Datacite fields to facilitate semantic interpretation of the schema.

Key files in this repo:
1. The SciCat RO-Crate profile: [profile.md](profile.md)
2. The SciCat PublishedData schema / type definition: [published-data.jsonld](scicat-types/published-data.jsonld)
3. A conformant RO-Crate example (hand-crafted): [ro-crate-metadata.json](ro-crate-metadata.json) + data files
4. A HTML preview generated by running [ro-crate-html](https://www.npmjs.com/package/ro-crate-html) on the example crate.