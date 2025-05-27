package ch.psi.scicat.model;

public class PropertyRequirements {
    private boolean isRequired;
    private boolean isResource;
    private boolean isMultivalued;

    public PropertyRequirements(boolean isRequired) {
        this.isRequired = isRequired;
        this.isResource = false;
        this.isMultivalued = false;
    }

    public PropertyRequirements(boolean isRequired, boolean isResource) {
        this.isRequired = isRequired;
        this.isResource = isResource;
        this.isMultivalued = false;
    }

    public PropertyRequirements(boolean isRequired, boolean isResource, boolean isMultivalued) {
        this.isRequired = isRequired;
        this.isResource = isResource;
        this.isMultivalued = isMultivalued;
    }

    public boolean isResource() {
        return isResource;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isMultivalued() {
        return isMultivalued;
    }
}
