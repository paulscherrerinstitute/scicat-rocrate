#!/bin/sh

set -e

version="$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)"
ghcr_image="ghcr.io/paulscherrerinstitute/scicat-rocrate:${version}"
gitea_image="gitea.psi.ch/data-catalog-services/scicat-rocrate:${version}"

docker push "$ghcr_image"
docker tag "$ghcr_image" "$gitea_image"
docker push "$gitea_image"
