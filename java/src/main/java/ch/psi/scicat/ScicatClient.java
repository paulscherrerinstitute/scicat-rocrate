package ch.psi.scicat;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import ch.psi.scicat.model.CredentialsDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.ReturnedAuthLoginDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ScicatClient {
    @Inject
    @RestClient
    ScicatService scicatService;

    public boolean isHealthy() {
        RestResponse<Void> response = scicatService.isHealthy();
        return response.getStatus() == 200;
    }

    public RestResponse<PublishedData> getPublishedDataById(String doi) {
        RestResponse<PublishedData> clientResponse = scicatService.getPublishedDataById(doi);
        return RestResponse.fromResponse(clientResponse);
    }

    public RestResponse<PublishedData> getPublishedDataById(PublishedData publishedData) {
        RestResponse<PublishedData> clientResponse = scicatService.createPublishedData("Bearer: TODO", publishedData);
        return RestResponse.fromResponse(clientResponse);
    }

    public RestResponse<Dataset> getDatasetByPid(String pid) {
        RestResponse<Dataset> clientResponse = scicatService.getDatasetByPid(pid);
        return RestResponse.fromResponse(clientResponse);
    }

    public RestResponse<ReturnedAuthLoginDto> login(CredentialsDto credentials) {
        RestResponse<ReturnedAuthLoginDto> clientResponse = scicatService.login(credentials);
        return RestResponse.fromResponse(clientResponse);
    }
}
