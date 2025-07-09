package ch.psi.scicat.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PropertyError implements ValidationError {
    @JsonProperty()
    private String nodeId;
    @JsonProperty()
    private String property;

    private String message;

    public PropertyError(String nodeId, String property, String message) {
        this.nodeId = nodeId;
        this.property = property;
        this.message = message;
    }

    @Override
    public String getType() {
        return "PropertyError";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        PropertyError that = (PropertyError) obj;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(property, that.property)
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, property, message);
    }
}
