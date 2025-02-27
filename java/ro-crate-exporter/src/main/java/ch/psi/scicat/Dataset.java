package ch.psi.scicat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Dataset(String pid, String datasetName, String description, String size, int numberOfFiles) {
}
