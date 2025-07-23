package ch.psi.ord.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationReport {
    private Set<ValidationError> errors = new HashSet<>();
    private List<Entity<? extends Object>> entities = new ArrayList<>();

    @JsonGetter("isValid")
    public boolean isValid() {
        return errors == null || errors.isEmpty();
    }

    @JsonGetter("errors")
    public Set<ValidationError> getErrors() {
        return errors;
    }

    @JsonGetter("entities")
    public List<String> getEntitiesId() {
        return entities
                .stream()
                .map(entity -> entity.id)
                .toList();
    }

    public List<Entity<? extends Object>> getEntities() {
        return entities;
    }

    public void addError(ValidationError e) {
        errors.add(e);
    }

    public void addEntity(Entity<? extends Object> e) {
        entities.add(e);
    }

    public record Entity<T>(@JsonProperty("id") String id, @JsonIgnore() T object) {
    }
}
