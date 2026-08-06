package ch.psi.ord.core;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.http.DefaultHttpClient;
import com.apicatalog.jsonld.http.HttpResponse;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.FileLoader;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class LoggingDocumentLoader implements DocumentLoader {
  private final DocumentLoader loader;

  LoggingDocumentLoader() {
    this(loggingSchemeRouter());
  }

  LoggingDocumentLoader(DocumentLoader delegate) {
    this.loader = delegate;
  }

  private static DocumentLoader loggingSchemeRouter() {
    DocumentLoader httpLoader = new HttpLoader(LoggingDocumentLoader::send);
    return new SchemeRouter()
        .set("http", httpLoader)
        .set("https", httpLoader)
        .set("file", new FileLoader());
  }

  private static HttpResponse send(URI url, String requestProfile) throws JsonLdError {
    HttpResponse response = DefaultHttpClient.defaultInstance().send(url, requestProfile);
    if (response.statusCode() / 100 == 3) {
      log.info("Redirected '{}' -> '{}'", url, response.location().orElse("?"));
    }
    return response;
  }

  @Override
  public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
    log.info("Loading remote json-ld document '{}'", url);
    try {
      return loader.loadDocument(url, options);
    } catch (JsonLdError e) {
      log.error("Failed to load remote json-ld document '{}' ({})", url, e.getMessage());
      throw e;
    }
  }
}
