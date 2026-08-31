package ch.psi.ord.core;

import static org.modelmapper.Conditions.isNotNull;

import ch.psi.ord.model.Person;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ZenodoDataset;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.DatasetType;
import ch.psi.scicat.model.v4.CreatePublishedDataDto;
import ch.psi.scicat.model.v4.DataciteMetadata.Affiliation;
import ch.psi.scicat.model.v4.DataciteMetadata.Creator;
import ch.psi.scicat.model.v4.DataciteMetadata.Description;
import ch.psi.scicat.model.v4.DataciteMetadata.DescriptionType;
import ch.psi.scicat.model.v4.DataciteMetadata.RelatedIdentifier;
import ch.psi.scicat.model.v4.DataciteMetadata.RelatedIdentifierType;
import ch.psi.scicat.model.v4.DataciteMetadata.RelationType;
import ch.psi.scicat.model.v4.PublishedData;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

@Singleton
public class ScicatModelMapper {
  private final Converter<List<Person>, String> personListToOwnerString =
      context ->
          context.getSource().stream()
              .map(p -> String.format("%s %s", p.getGivenName(), p.getFamilyName()))
              .collect(Collectors.joining("; "));

  private final Converter<List<Person>, String> personListToOwnerEmails =
      context ->
          context.getSource().stream().map(Person::getEmail).collect(Collectors.joining("; "));

  private final Converter<List<Person>, List<String>> personToStringList =
      context -> context.getSource().stream().map(Person::getName).collect(Collectors.toList());

  private final Converter<List<String>, List<Person>> stringListToPerson =
      context ->
          context.getSource().stream()
              .map(str -> new Person().setName(str))
              .collect(Collectors.toList());

  private final Converter<String, Integer> dateToYear =
      context -> {
        DateTimeFormatter openBisFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"); // Pattern used by openBIS
        List<DateTimeFormatter> supportedFormats =
            List.of(openBisFormatter, DateTimeFormatter.ISO_ZONED_DATE_TIME);

        String input = context.getSource();
        if (context.getSource().matches("\\d{4}")) {
          input += "-01-01T00:00:00Z";
        }
        for (DateTimeFormatter format : supportedFormats) {
          try {
            OffsetDateTime date = OffsetDateTime.parse(input, format);
            return date.getYear();
          } catch (DateTimeParseException e) {
          }
        }
        throw new IllegalArgumentException("Invalid date format: " + input);
      };

  private final Converter<String, String> doiToDoiUrl =
      context -> DoiUtils.buildStandardUrl(context.getSource());

  private final Converter<List<String>, List<String>> keywordsConverter =
      context -> {
        List<String> source = context.getSource();

        if (source == null || source.isEmpty()) {
          return new ArrayList<>();
        }

        return source.stream()
            .filter(Objects::nonNull)
            .map(entry -> entry.split(","))
            .flatMap(Arrays::stream)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(Collectors.toList());
      };

  private final Converter<String, String> uriPathExtractor =
      context -> {
        String sourceId = context.getSource();
        try {
          URI uri = new URI(sourceId);
          return uri.getPath();
        } catch (Exception e) {
          return sourceId;
        }
      };

  private final Converter<String, String> uriHostExtractor =
      context -> {
        String sourceId = context.getSource();
        try {
          URI uri = new URI(sourceId);
          return uri.getHost();
        } catch (Exception e) {
          return sourceId;
        }
      };

  private final Converter<List<Creator>, List<Person>> dataciteCreatorToPerson =
      context ->
          context.getSource().stream()
              .map(
                  creator ->
                      new Person()
                          .setName(creator.getName())
                          .setGivenName(creator.getGivenName())
                          .setFamilyName(creator.getFamilyName()))
              .toList();

  private final Converter<List<Person>, List<Creator>> personToDataciteCreator =
      context ->
          context.getSource().stream()
              .map(
                  creator ->
                      new Creator()
                          .setName(creator.getName())
                          .setGivenName(creator.getGivenName())
                          .setFamilyName(creator.getFamilyName())
                          .setAffiliation(
                              creator.getAffiliation().stream()
                                  .map(
                                      organization ->
                                          new Affiliation().setName(organization.getName()))
                                  .collect(Collectors.toList())))
              .toList();

  private final Converter<Publication, CreatePublishedDataDto> publicationPostConverter =
      context -> {
        Publication source = context.getSource();
        CreatePublishedDataDto destination = context.getDestination();

        if (source.getDescription() != null) {
          destination
              .getMetadata()
              .getDescriptions()
              .add(
                  new Description()
                      .setDescription(source.getDescription())
                      .setLang("en")
                      .setDescriptionType(DescriptionType.OTHER));
        }

        DoiUtils.extractDoi(source.getIdentifier())
            .ifPresent(
                doi ->
                    destination
                        .getMetadata()
                        .getRelatedIdentifiers()
                        .add(
                            new RelatedIdentifier()
                                .setRelatedIdentifierType(RelatedIdentifierType.DOI)
                                .setRelationType(RelationType.IS_IDENTICAL_TO)
                                .setRelatedIdentifier(doi)));

        return destination;
      };

  @Produces
  public ModelMapper createPublicationModelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setImplicitMappingEnabled(false);

    mapper
        .typeMap(Publication.class, CreatePublishedDataDto.class)
        .addMappings(
            m -> {
              m.when(isNotNull())
                  .using(personToDataciteCreator)
                  .map(
                      Publication::getCreator,
                      (CreatePublishedDataDto dst, List<Creator> v) ->
                          dst.getMetadata().setCreators(v));
              m.when(isNotNull()).map(Publication::getTitle, CreatePublishedDataDto::setTitle);
              m.when(isNotNull())
                  .map(Publication::getAbstract, CreatePublishedDataDto::setAbstract);
            })
        .setPostConverter(publicationPostConverter);

    mapper
        .typeMap(PublishedData.class, ZenodoDataset.class)
        .addMappings(
            m -> {
              m.when(isNotNull())
                  .using(doiToDoiUrl)
                  .map(PublishedData::getDoi, ZenodoDataset::setIdentifier);
              m.when(isNotNull()).map(PublishedData::getTitle, ZenodoDataset::setName);
              m.when(isNotNull()).map(PublishedData::getAbstract, ZenodoDataset::setDescription);
              m.when(isNotNull()).map(PublishedData::getCreatedAt, ZenodoDataset::setDateCreated);
              m.when(isNotNull())
                  .map(PublishedData::getRegisteredTime, ZenodoDataset::setDatePublished);
              m.when(isNotNull())
                  .map(
                      src -> src.getMetadata().getPublisher().getName(),
                      (dst, v) -> dst.getPublisher().setName((String) v));
              m.when(isNotNull())
                  .using(dataciteCreatorToPerson)
                  .map(src -> src.getMetadata().getCreators(), ZenodoDataset::setCreators);
            });

    mapper
        .typeMap(Publication.class, CreateDatasetDto.class)
        .addMappings(
            m -> {
              m.using(personListToOwnerString)
                  .map(Publication::getCreator, CreateDatasetDto::setPrincipalInvestigator);
              m.using(personListToOwnerString)
                  .map(Publication::getCreator, CreateDatasetDto::setOwner);
              m.map(Publication::getTitle, CreateDatasetDto::setDatasetName);
              m.map(src -> "rocrate", CreateDatasetDto::setCreationLocation);
              m.map(src -> DatasetType.RAW, CreateDatasetDto::setType);
              m.using(personListToOwnerEmails)
                  .map(Publication::getCreator, CreateDatasetDto::setContactEmail);
              m.map(src -> Instant.now(), CreateDatasetDto::setCreationTime);
              m.map(Publication::getDescription, CreateDatasetDto::setDescription);
            });

    return mapper;
  }
}
