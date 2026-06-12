# Aare and SciLog architecture

```mermaid
sequenceDiagram
    autonumber
    participant scilog as aare / scilog
    participant rocrate as rocrate (PSI)
    participant scicat
    participant arema
    participant scicat-cli

    scilog->>rocrate: Upload metadata.jsonld (POST HTTP /rocrate/import)
    rocrate->>rocrate: Flatten metadata.jsonld & split into<br/>1+ metadata.json (1 per Dataset) & rest.jsonld

    rocrate->>scicat: POST 1 create_job (with payload containing archiving options & all metadata.json contents, each file listings)
    scicat-->>rocrate: Return JOBID

    scicat->>arema: Arema picks up create_job

    loop For each metadata.json dataset in job payload
        arema->>arema: Check that user submitting the job has file permissions (or impersonate user)
        arema->>scicat-cli: Trigger ingest with filelist and metadata.json
        scicat-cli->>scicat-cli: Inspect files in NFS and compute file size
        scicat-cli->>scicat: POST dataset and origdatablocks
        scicat-->>scicat-cli: Return datasetID
        scicat-cli-->>arema: Return datasetID
    end
    arema->>scicat: PATCH job status to "entities_created"

    loop Job Dependency Polling
        rocrate->>scicat: Get job status
        scicat-->>rocrate: Return status (Wait until status == "entities_created")
    end

    rocrate->>rocrate: Extract datasetIds from job response

    rocrate->>scicat: POST rest.json to scicat (using datasetIDs for linking when required)
    scicat-->>rocrate: Return all rest.json IDs

    arema->>arema: Move data to tape
    arema->>scicat: Arema updates JOB when completed to "data_ingested (or finishedSuccessful)" (PATCH /job/{id})
```
