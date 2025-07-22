package ch.psi.scicat;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class ScicatClient {
    @Inject
    @RestClient
    ScicatService scicatService;

    private static final Logger logger = LoggerFactory.getLogger(ScicatClient.class);

    public boolean isHealthy() {
        try {
            RestResponse<Void> response = scicatService.isHealthy();
            return response.getStatus() == 200;
        } catch (WebApplicationException e) {
            return false;
        }
    }

    public RestResponse<PublishedData> getPublishedDataById(String doi) {
        RestResponse<PublishedData> clientResponse = scicatService.getPublishedDataById(doi);
        // Required on backend-next because of this bug:
        // https://github.com/SciCatProject/scicat-backend-next/issues/2036
        if (clientResponse.getStatus() == 200 && !clientResponse.hasEntity()) {
            throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
        }
        return RestResponse.fromResponse(clientResponse);
    }

    public RestResponse<PublishedData> createPublishedData(CreatePublishedDataDto publishedData, String scicatToken) {
        try {
            logger.debug(new ObjectMapper().writeValueAsString(publishedData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        RestResponse<PublishedData> clientResponse = scicatService.createPublishedData("Bearer " + scicatToken,
                publishedData);
        return RestResponse.fromResponse(clientResponse);
    }

    public RestResponse<Dataset> getDatasetByPid(String pid) {
        RestResponse<Dataset> clientResponse = scicatService.getDatasetByPid(pid);
        return RestResponse.fromResponse(clientResponse);
    }
}
