package ch.psi.scicat;

import static java.util.Map.entry;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScicatContainers
        implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private String integrationTestNetworkId;
    private MongoDBContainer mongoDBContainer;
    private GenericContainer<?> scicatBackendContainer;

    private static final Logger LOG = Logger.getLogger(ScicatContainers.class);

    private static final Map<String, String> ScicatBackendEnv = Map.ofEntries(
            entry("SITE", "CI"),
            entry("JWT_SECRET", "secret"),
            entry("ADMIN_GROUPS", "admin"),
            entry("DELETE_GROUPS", "archivemanager"),
            entry("CREATE_DATASET_GROUPS", "ingestor"),
            entry("PROPOSAL_GROUPS", "proposalingestor"),
            entry("SAMPLE_GROUPS", "ingestor"));

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        context.containerNetworkId().ifPresent(id -> {
            this.integrationTestNetworkId = id;
            LOG.infof("Container network name: %s", integrationTestNetworkId);
        });
    }

    @SuppressWarnings("resource")
    @Override
    public Map<String, String> start() {

        // Network network = Network.newNetwork();
        mongoDBContainer = new MongoDBContainer("mongo:latest")
                // .withNetwork(containerNetworkId)
                .withNetworkMode(integrationTestNetworkId);
        // .withNetworkAliases("mongo");
        this.mongoDBContainer.start();

        Map<String, String> env = new HashMap<>(ScicatBackendEnv);
        mongoDBContainer.getCurrentContainerInfo().getConfig().getHostName();
        env.put("MONGODB_URI", String.format("mongodb://%s:27017/scicat-ci",
                mongoDBContainer.getCurrentContainerInfo().getConfig().getHostName()));
        LOG.infof("Mongo URI: %s", env.get("MONGODB_URI"));
        scicatBackendContainer = new GenericContainer<>(
                "ghcr.io/scicatproject/backend-next:v4.12.2")
                .withNetworkMode(integrationTestNetworkId)
                .dependsOn(mongoDBContainer)
                .withEnv(env)
                .waitingFor(Wait.forHttp("/api/v3/health"));

        this.scicatBackendContainer.start();

        // String scicatIp =
        // scicatBackendContainer.getCurrentContainerInfo().getConfig().getHostName();
        String scicatIp = scicatBackendContainer
                .getContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .get(integrationTestNetworkId)
                .getIpAddress();
        String backendUrl = String.format("http://%s:%d/api/v3", scicatIp, 3000);
        LOG.infof("Backend URL: %s", backendUrl);
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.rest-client.scicat-api.url", backendUrl);
        // config.put("test.url", "http://host.docker.internal:8081");
        // config.put("test.url", "http://host-gateway:8081");

        return config;
    }

    @Override
    public void stop() {
        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
        }

        if (scicatBackendContainer != null) {
            scicatBackendContainer.stop();
        }
    }
}