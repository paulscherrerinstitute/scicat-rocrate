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

| Name                               | Description                                                  | Default value               |
|------------------------------------|--------------------------------------------------------------|-----------------------------|
| `QUARKUS_REST_CLIENT_SCICAT_URL`   | Base URL of the SciCat backend                               | http://backend.localhost    |
| `QUARKUS_REST_CLIENT_S3BROKER_URL` | Base URL of the S3 Broker                                    | https://s3-broker.localhost |
| `SCICAT_CLI_PATH`                  | Path of the scicat-cli executable                            | /usr/local/bin/scicat-cli   |
| `SCICAT_PID_PREFIX`                | PID prefix of the SciCat instance (used to match cli output) | PID.SAMPLE.PREFIX           |
| `TITANIUM_JSONLD_CACHE_SIZE`       | Size of the JSON-LD context cache                            | 10                          |
