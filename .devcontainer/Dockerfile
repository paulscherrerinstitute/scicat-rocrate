FROM mcr.microsoft.com/devcontainers/java:21

ENV NVM_DIR="/usr/local/share/nvm"

RUN apt-get update \
    && apt-get install -y  \
    pipx \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER vscode
RUN pipx ensurepath && pipx install pre-commit
RUN . "$NVM_DIR/nvm.sh" && nvm install --lts
RUN curl -Ls https://sh.jbang.dev | bash -s - trust add https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/ \
    && curl -Ls https://sh.jbang.dev | bash -s - app install --fresh --force quarkus@quarkusio
