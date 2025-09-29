package ch.psi.rdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

@Slf4j
public class RdfUtils {
  /**
   * Switches the scheme of a given URI from HTTP to HTTPS or vice versa.
   *
   * @param uri the URI to switch the scheme for
   * @return the URI with the switched scheme, or the original URI if it does not start with
   *     "http://" or "https://"
   */
  public static String switchScheme(String uri) {
    if (uri.startsWith("http://")) {
      return uri.replace("http://", "https://");
    } else if (uri.startsWith("https://")) {
      return uri.replace("https://", "http://");
    }
    log.info("Can't switch scheme for {}", uri);

    return uri;
  }

  /**
   * Switches the scheme of a given Resource from HTTP to HTTPS or vice versa.
   *
   * @param resource the Resource to switch the scheme for
   * @return the Resource with the switched scheme, or the original Resource if it does not start
   *     with "http://" or "https://"
   */
  public static Resource switchScheme(Resource resource) {
    String uri = resource.getURI();
    if (uri == null) {
      log.info("Can't switch scheme of non-URI resource {}", resource.toString());
      return resource;
    }

    return ResourceFactory.createResource(switchScheme(uri));
  }

  /**
   * Switches the scheme of a given Property from HTTP to HTTPS or vice versa.
   *
   * @param property the property to switch the scheme for
   * @return the property with the switched scheme, or the original Property if it does not start
   *     with "http://" or "https://"
   */
  public static Property switchScheme(Property property) {
    String uri = property.getURI();
    if (uri == null) {
      log.info("Can't switch scheme of non-URI resource {}", property.toString());
      return property;
    }

    return ResourceFactory.createProperty(switchScheme(uri));
  }
}
