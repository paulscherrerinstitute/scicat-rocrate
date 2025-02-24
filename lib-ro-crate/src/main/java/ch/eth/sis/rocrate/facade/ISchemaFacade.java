package ch.eth.sis.rocrate.facade;

import java.util.List;

public interface ISchemaFacade
{

    /* Adds a single class */
    void addType(IType rdfsClass);

    /** Retrieves all Classes */
    List<IType> getTypes();

    /* Get a single type by its ID */
    IType getTypes(String id);

    /* Adds a single property */
    void addPropertyType(IPropertyType property);

    /* Get all Properties */
    List<IPropertyType> getPropertyTypes();

    /* Gets a single property by its ID. */
    IPropertyType getPropertyType(String id);

    /* Add a single metadata entry */
    void addEntry(IMetadataEntry entry);

    /* Get a single metadata entry by its ID */
    IMetadataEntry getEntry(String id);

    /* Get all metadata entities */
    List<IMetadataEntry> getEntries(String rdfsClassId);

}
