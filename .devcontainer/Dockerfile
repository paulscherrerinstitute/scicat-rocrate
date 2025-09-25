FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

ENV NVM_DIR="/usr/local/share/nvm"

RUN apt-get update \
    && apt-get install -y  \
    python3-pip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER vscode
RUN pip install pre-commit
RUN . "$NVM_DIR/nvm.sh" && nvm install --lts
RUN curl -Ls https://sh.jbang.dev | bash -s - trust add https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/ \
    && curl -Ls https://sh.jbang.dev | bash -s - app install --fresh --force quarkus@quarkusio
