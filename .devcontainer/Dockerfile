FROM mcr.microsoft.com/devcontainers/java:25

ARG SCICAT_CLI_VERSION=3.1.0
ENV NVM_DIR="/usr/local/share/nvm"

RUN mkdir --parents --mode=777 /rocrate/archive /rocrate/extract

RUN apt-get update \
    && apt-get install -y  \
    pipx \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN curl -L -s "https://github.com/paulscherrerinstitute/scicat-cli/releases/download/v${SCICAT_CLI_VERSION}/scicat-cli_${SCICAT_CLI_VERSION}_Linux_x86_64.tar.gz" \
    | tar -xzf - --strip-components=1 -C /usr/local/bin scicat-cli_${SCICAT_CLI_VERSION}_Linux_x86_64/scicat-cli

USER vscode
RUN pipx ensurepath && pipx install pre-commit
RUN . "$NVM_DIR/nvm.sh" && nvm install --lts
RUN curl -Ls https://sh.jbang.dev | bash -s - trust add https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/ \
    && curl -Ls https://sh.jbang.dev | bash -s - app install --fresh --force quarkus@quarkusio
