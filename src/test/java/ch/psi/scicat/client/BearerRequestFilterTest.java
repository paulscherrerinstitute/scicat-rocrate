package ch.psi.scicat.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import ch.psi.scicat.client.v4.BearerRequestFilter;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BearerRequestFilterTest {
  @Inject BearerRequestFilter filter;

  @Test
  @DisplayName("null getHeaders()")
  public void test00() throws Exception {
    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);

    MultivaluedMap<String, Object> originalHeaders = requestContext.getHeaders();
    filter.filter(requestContext);
    assertEquals(originalHeaders, requestContext.getHeaders());
  }

  @Test
  @DisplayName("No 'Authorization' header")
  public void test01() throws Exception {
    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);
    when(requestContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());

    MultivaluedMap<String, Object> originalHeaders = requestContext.getHeaders();
    filter.filter(requestContext);
    assertEquals(originalHeaders, requestContext.getHeaders());
  }

  @Test
  @DisplayName("One 'Authorization' header")
  public void test02() throws Exception {
    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);
    MultivaluedMap<String, Object> originalHeaders = new MultivaluedHashMap<>();
    originalHeaders.putSingle(BearerRequestFilter.AUTH_HEADER, "<token>");
    when(requestContext.getHeaders()).thenReturn(originalHeaders);

    filter.filter(requestContext);
    assertEquals(
        "Bearer <token>", requestContext.getHeaders().getFirst(BearerRequestFilter.AUTH_HEADER));
  }

  @Test
  @DisplayName("Multiple 'Authorization' header")
  public void test03() throws Exception {
    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);
    MultivaluedMap<String, Object> originalHeaders = new MultivaluedHashMap<>();
    originalHeaders.put(BearerRequestFilter.AUTH_HEADER, List.of("<token-1>", "<token-2>"));
    when(requestContext.getHeaders()).thenReturn(originalHeaders);

    filter.filter(requestContext);
    assertEquals(
        List.of("Bearer <token-1>", "Bearer <token-2>"),
        requestContext.getHeaders().get(BearerRequestFilter.AUTH_HEADER));
  }

  @Test
  @DisplayName("Already prefixed 'Authorization' header")
  public void test04() throws Exception {
    ClientRequestContext requestContext = Mockito.mock(ClientRequestContext.class);
    MultivaluedMap<String, Object> originalHeaders = new MultivaluedHashMap<>();
    originalHeaders.putSingle(BearerRequestFilter.AUTH_HEADER, "Bearer <token>");
    when(requestContext.getHeaders()).thenReturn(originalHeaders);

    filter.filter(requestContext);
    assertEquals(
        "Bearer <token>", requestContext.getHeaders().getFirst(BearerRequestFilter.AUTH_HEADER));
  }
}
