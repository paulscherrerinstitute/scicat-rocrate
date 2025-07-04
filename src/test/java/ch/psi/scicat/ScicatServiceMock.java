package ch.psi.scicat;

import org.jboss.resteasy.reactive.RestResponse;

import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;

public class ScicatServiceMock {
    public static ScicatService generate(boolean isHealthy) {
        return new ScicatService() {
            @Override
            public RestResponse<Void> isHealthy() {
                if (isHealthy)
                    return RestResponse.ok();

                throw new UnsupportedOperationException("Unimplemented method 'getPublishedDataById'");
            }

            @Override
            public RestResponse<PublishedData> getPublishedDataById(String doi) {
                throw new UnsupportedOperationException("Unimplemented method 'getPublishedDataById'");
            }

            @Override
            public RestResponse<PublishedData> createPublishedData(String accessToken, PublishedData publishedData) {
                throw new UnsupportedOperationException("Unimplemented method 'createPublishedData'");
            }

            @Override
            public RestResponse<Dataset> getDatasetByPid(String pid) {
                throw new UnsupportedOperationException("Unimplemented method 'getDatasetByPid'");
            }
        };
    }
}
