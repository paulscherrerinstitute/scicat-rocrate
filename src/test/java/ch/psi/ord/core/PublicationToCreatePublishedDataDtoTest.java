package ch.psi.ord.core;

import ch.psi.ord.model.Organization;
import ch.psi.ord.model.Person;
import ch.psi.ord.model.Publication;
import ch.psi.scicat.model.v4.CreatePublishedDataDto;
import ch.psi.scicat.model.v4.DataciteMetadata.Affiliation;
import ch.psi.scicat.model.v4.DataciteMetadata.Creator;
import ch.psi.scicat.model.v4.DataciteMetadata.Description;
import ch.psi.scicat.model.v4.DataciteMetadata.DescriptionType;
import ch.psi.scicat.model.v4.DataciteMetadata.RelatedIdentifier;
import ch.psi.scicat.model.v4.DataciteMetadata.RelatedIdentifierType;
import ch.psi.scicat.model.v4.DataciteMetadata.RelationType;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.modelmapper.ModelMapper;

public class PublicationToCreatePublishedDataDtoTest {
  private static final String DOI = "10.16907/abc-123";
  private static final String HZDR_ROR = "https://ror.org/01zy2cs03";

  private final ModelMapper mapper = new ScicatModelMapper().createPublicationModelMapper();

  private CreatePublishedDataDto map(Publication publication) {
    return mapper.map(publication, CreatePublishedDataDto.class);
  }

  private static Organization organization(String name, String resourceIdentifier) {
    return new Organization().setName(name).setResourceIdentifier(resourceIdentifier);
  }

  private static void assertRor(String expectedRor, Affiliation affiliation) {
    Assertions.assertEquals(expectedRor, affiliation.getAffiliationIdentifier());
    Assertions.assertEquals("ROR", affiliation.getAffiliationIdentifierScheme());
    Assertions.assertEquals("https://ror.org", affiliation.getSchemeUri());
  }

  @Test
  @DisplayName("Full publication")
  public void test00() {
    Publication publication =
        new Publication()
            .setIdentifier(DOI)
            .setTitle("A title")
            .setAbstract("An abstract")
            .setDescription("A description")
            .setCreator(
                List.of(
                    new Person()
                        .setName("Doe, John")
                        .setAffiliation(List.of(organization("HZDR", HZDR_ROR)))));

    CreatePublishedDataDto dto = map(publication);

    Assertions.assertEquals("A title", dto.getTitle());
    Assertions.assertEquals("An abstract", dto.getAbstract());
    Creator creator = dto.getMetadata().getCreators().getFirst();
    Assertions.assertEquals("Doe, John", creator.getName());
    assertRor(HZDR_ROR, creator.getAffiliation().getFirst());
    Assertions.assertEquals(
        "A description", dto.getMetadata().getDescriptions().getFirst().getDescription());
    Assertions.assertEquals(
        DOI, dto.getMetadata().getRelatedIdentifiers().getFirst().getRelatedIdentifier());
  }

  @Test
  @DisplayName("Creator names")
  public void test01() {
    Publication publication =
        new Publication()
            .setCreator(
                List.of(
                    new Person().setName("Doe, John").setGivenName("John").setFamilyName("Doe")));

    Creator creator = map(publication).getMetadata().getCreators().getFirst();

    Assertions.assertEquals("Doe, John", creator.getName());
    Assertions.assertEquals("John", creator.getGivenName());
    Assertions.assertEquals("Doe", creator.getFamilyName());
  }

  @ParameterizedTest
  @DisplayName("No creator")
  @NullAndEmptySource
  public void test02(List<Person> persons) {
    Publication publication = new Publication().setCreator(persons);

    Assertions.assertTrue(map(publication).getMetadata().getCreators().isEmpty());
  }

  @ParameterizedTest
  @DisplayName("No affiliation")
  @NullAndEmptySource
  public void test03(List<Organization> affiliations) {
    Publication publication =
        new Publication().setCreator(List.of(new Person().setAffiliation(affiliations)));

    Assertions.assertTrue(
        map(publication).getMetadata().getCreators().getFirst().getAffiliation().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("Affiliation ROR URL is normalized")
  @ValueSource(
      strings = {
        HZDR_ROR,
        "http://ror.org/01zy2cs03",
        "https://www.ror.org/01zy2cs03",
        "https://ror.org/01zy2cs03/"
      })
  public void test05(String id) {
    Affiliation affiliation = mapper.map(organization("HZDR", id), Affiliation.class);

    Assertions.assertEquals("HZDR", affiliation.getName());
    assertRor(HZDR_ROR, affiliation);
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("Affiliation non ROR identifier is ignored")
  @NullSource
  @ValueSource(
      strings = {
        "https://example.org/org/1",
        "https://ror.org/",
        "https://ror.org/abc",
        "https://ror.org/11zy2cs03",
        "https://ror.org/01zy2cs03/extra",
        "https://notror.org/01zy2cs03",
        "01zy2cs03"
      })
  public void test06(String id) {
    Affiliation affiliation = mapper.map(organization("HZDR", id), Affiliation.class);

    Assertions.assertEquals("HZDR", affiliation.getName());
    Assertions.assertNull(affiliation.getAffiliationIdentifier());
    Assertions.assertNull(affiliation.getAffiliationIdentifierScheme());
    Assertions.assertNull(affiliation.getSchemeUri());
  }

  @Test
  @DisplayName("Description")
  public void test07() {
    List<Description> descriptions =
        map(new Publication().setDescription("A description")).getMetadata().getDescriptions();

    Assertions.assertEquals(1, descriptions.size());
    Description description = descriptions.getFirst();
    Assertions.assertEquals("A description", description.getDescription());
    Assertions.assertEquals("en", description.getLang());
    Assertions.assertEquals(DescriptionType.OTHER, description.getDescriptionType());
  }

  @Test
  @DisplayName("No description")
  public void test08() {
    Assertions.assertTrue(map(new Publication()).getMetadata().getDescriptions().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("DOI identifier is a related identifier")
  @ValueSource(strings = {DOI, "https://doi.org/" + DOI, "doi:" + DOI})
  public void test09(String identifier) {
    List<RelatedIdentifier> relatedIdentifiers =
        map(new Publication().setIdentifier(identifier)).getMetadata().getRelatedIdentifiers();

    Assertions.assertEquals(1, relatedIdentifiers.size());
    RelatedIdentifier relatedIdentifier = relatedIdentifiers.getFirst();
    Assertions.assertEquals(DOI, relatedIdentifier.getRelatedIdentifier());
    Assertions.assertEquals(
        RelatedIdentifierType.DOI, relatedIdentifier.getRelatedIdentifierType());
    Assertions.assertEquals(RelationType.IS_IDENTICAL_TO, relatedIdentifier.getRelationType());
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("Non DOI identifier is not a related identifier")
  @NullSource
  @ValueSource(strings = {"https://example.org/publication/1"})
  public void test10(String identifier) {
    Assertions.assertTrue(
        map(new Publication().setIdentifier(identifier))
            .getMetadata()
            .getRelatedIdentifiers()
            .isEmpty());
  }
}
