package ch.psi.scicat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.RoCrate.RoCrateBuilder;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;

public class ROCrateExporter {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        RoCrate crate = new RoCrateBuilder().build();
        Schema schema = new Schema(true, true);
        for (DataEntity entity : schema.entities) {
            crate.addDataEntity(entity, true);
        }

        ScicatClient client = new ScicatClient("https://dacat.psi.ch/api/v3");
        Optional<PublishedData> publishedData = client
                // .getPublishedData("10.16907%2F808de0df-a9d3-4698-8e9f-d6e091516650");
                // .getPublishedData("10.16907%2F2c501d81-99a4-4bce-a6de-765b86ede4ab");
                .getPublishedData("10.16907%2F7eb141d3-11f1-47a6-9d0e-76f8832ed1b2");

        publishedData.ifPresent(pb -> {
            List<Dataset> datasets = new ArrayList<>();
            pb.pidArray().forEach(pid -> {
                try {
                    client.getDataset(pid).ifPresent(ds -> datasets.add(ds));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            crate.addFromCollection(schema.genPublishedDataEntities(pb, datasets));

            RootDataEntity root = crate.getRootDataEntity();
            root.addToHasPart(pb.doi());
            root.addProperty("name", pb.title());
            root.addProperty("description", pb._abstract());
            root.addProperty("datePublished", Long.toString(pb.publicationYear()));
            root.addProperty("license", "Unknown");

            RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
            folderRoCrateWriter.save(crate, "out");
        });
    }
}
