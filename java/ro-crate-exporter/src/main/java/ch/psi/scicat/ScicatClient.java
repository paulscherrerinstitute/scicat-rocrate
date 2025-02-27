package ch.psi.scicat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ScicatClient {
    private String baseUrl;
    private HttpClient client = HttpClient.newHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    public ScicatClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Optional<PublishedData> getPublishedData(String doi)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/publisheddata/" + doi)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            PublishedData publishedData = mapper.readValue(response.body(), PublishedData.class);
            return Optional.of(publishedData);
        } else {
            System.err.println(
                    "Failed to retreive PublishedData associated with the following DOI: "
                            + doi);
            System.err.println("HTTP Status code: " + response.statusCode());
            System.err.println("HTTP message: " + response.body());

            return Optional.empty();
        }
    }

    public Optional<Dataset> getDataset(String pid)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/datasets/" + pid.replace("/", "%2F"))).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Dataset ds = mapper.readValue(response.body(), Dataset.class);
            return Optional.of(ds);
        } else {
            System.err.println(
                    "Failed to retreive Dataset associated with the following PID: "
                            + pid);
            System.err.println("HTTP Status code: " + response.statusCode());
            System.err.println("HTTP message: " + response.body());

            return Optional.empty();
        }
    }
}
