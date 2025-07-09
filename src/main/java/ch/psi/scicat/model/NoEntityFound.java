package ch.psi.scicat.model;

public class NoEntityFound implements ValidationError {
    @Override
    public String getType() {
        return "NoEntityFound";
    }

    @Override
    public String getMessage() {
        return "No suitable entity found in the graph";
    }
}
