package ch.eth.sis.rocrate.facade;


import java.util.List;

public interface IType
{
    /* Returns the ID of this type */
    String getId();

    /* Returns IDs of the types this type inherits from */
    List<String> getSubClassOf();

    /* Returns the ontological annotations of this type */
    List<String> getOntologicalAnnotations();


}
