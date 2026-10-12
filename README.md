# scicat-rocrate

Implementation of the [RO-Crate API](https://github.com/paulscherrerinstitute/rocrate-api) for [SciCat](https://github.com/SciCatProject/scicat-backend-next).


## How to work on this project?

To setup the project, it is recomended to use the provided [Development Containers](https://containers.dev/).


### Running the application in dev mode

Note: It is possible to use Maven CLI directly (`quarkus <command>` is equivalent to `./mvnw quarkus:<command>`).

```console
quarkus dev
```

### Runing tests in watch mode

```console
quarkus test
```

### Building a Docker image

```console
quarkus build
```

### Publishing a Docker image to GitHub registry

```console
quarkus build -Dquarkus.container-image.push=true
```

## Deployment

This service can be configured using the following environment variables:

| Name                               | Description                                                                                              | Default value               |
|------------------------------------|----------------------------------------------------------------------------------------------------------|-----------------------------|
| `QUARKUS_REST_CLIENT_SCICAT_URL`   | Base URL of the SciCat backend                                                                           | http://backend.localhost    |
| `QUARKUS_REST_CLIENT_S3BROKER_URL` | Base URL of the S3 Broker                                                                                | https://s3-broker.localhost |
| `SCICAT_CLI_PATH`                  | Path of the scicat-cli executable                                                                        | /usr/local/bin/scicat-cli   |
| `SCICAT_PID_PREFIX`                | PID prefix of the SciCat instance (used to match cli output)                                             | PID.SAMPLE.PREFIX           |
| `ROCRATE_EXTRACT_DIRECTORY`        | Path where the service extracts crates                                                                   | /rocrate/extract            |
| `ROCRATE_ARCHIVE_DIRECTORY`        | Path where the service moves data for archival (must be on the same filesystem as the extract directory) | /rocrate/archive            |
| `ROCRATE_MAX_PATH_LENGTH`          | Maximum length (in bytes) of an extracted path                                                           | 4096                        |
| `ROCRATE_MAX_PATH_SEGMENT_LENGTH`  | Maximum length (in bytes) of a single segment of an extracted path                                       | 256                         |
| `JSONLD_PROCESSING_TIMEOUT`        | Maximum allowed time (in seconds) to process a JSON-LD document.                                         | 10                          |
| `ROCRATE_CLEANUP_MAX_AGE_DAYS`     | Age (in days) after which files are deleted from the extract and archive directories                     | 7                           |
| `ROCRATE_CLEANUP_CRON`             | Schedule of the cleanup, as a Quartz cron expression (daily at midnight by default)                      | 0 0 0 * * ?                 |

### Cleanup of the extract and archive directories

On the schedule defined by `ROCRATE_CLEANUP_CRON` (daily at midnight by default), the service deletes files from `ROCRATE_EXTRACT_DIRECTORY` and `ROCRATE_ARCHIVE_DIRECTORY` that were last modified more than `ROCRATE_CLEANUP_MAX_AGE_DAYS` days ago, along with directories that are old and empty. The two directories themselves are never deleted. The schedule is a [Quartz cron expression](https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html) (`seconds minutes hours day-of-month month day-of-week`) evaluated in the timezone of the server (usually UTC in containers).

Files moved to the archive directory keep the modification time they had when they were extracted, so the delay is counted from the upload of the crate, not from its archival.
