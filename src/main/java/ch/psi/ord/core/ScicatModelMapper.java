package ch.psi.ord.core;

import static org.modelmapper.Conditions.isNotNull;

import ch.psi.ord.model.Person;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ZenodoDataset;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.PublishedData;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

@Singleton
public class ScicatModelMapper {
  Converter<String, List<String>> identifierToRelatedPublications =
      new Converter<>() {
        @Override
        public List<String> convert(MappingContext<String, List<String>> context) {
          List<String> relatedPublications = new ArrayList<>();
          relatedPublications.add(
              String.format("%s (IsIdenticalTo)", DoiUtils.buildStandardUrl(context.getSource())));
          return relatedPublications;
        }
      };

  Converter<List<Person>, List<String>> personToStringList =
      new Converter<>() {
        @Override
        public List<String> convert(MappingContext<List<Person>, List<String>> context) {
          return context.getSource().stream().map(Person::getName).collect(Collectors.toList());
        }
      };

  Converter<List<String>, List<Person>> stringListToPerson =
      new Converter<>() {
        @Override
        public List<Person> convert(MappingContext<List<String>, List<Person>> context) {
          return context.getSource().stream()
              .map(str -> new Person().setName(str))
              .collect(Collectors.toList());
        }
      };

  Converter<String, Integer> dateToYear =
      new Converter<>() {
        @Override
        public Integer convert(MappingContext<String, Integer> context) {
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
        }
      };

  Converter<String, String> doiToDoiUrl =
      new Converter<String, String>() {
        @Override
        public String convert(MappingContext<String, String> context) {
          return DoiUtils.buildStandardUrl(context.getSource());
        }
      };

  @Produces
  public ModelMapper createPublicationModelMapper() {
    ModelMapper mapper = new ModelMapper();

    mapper
        .typeMap(Publication.class, CreatePublishedDataDto.class)
        .addMappings(
            m -> {
              m.when(isNotNull())
                  .using(identifierToRelatedPublications)
                  .map(Publication::getIdentifier, CreatePublishedDataDto::setRelatedPublications);
              m.when(isNotNull())
                  .using(personToStringList)
                  .map(Publication::getCreator, CreatePublishedDataDto::setCreator);
              m.when(isNotNull()).map(Publication::getTitle, CreatePublishedDataDto::setTitle);
              m.when(isNotNull())
                  .map(src -> src.getPublisher().getName(), CreatePublishedDataDto::setPublisher);
              m.when(isNotNull())
                  .map(Publication::getAbstract, CreatePublishedDataDto::setAbstract);
              m.when(isNotNull())
                  .map(Publication::getDescription, CreatePublishedDataDto::setDataDescription);
              m.when(isNotNull())
                  .using(dateToYear)
                  .map(Publication::getDatePublished, CreatePublishedDataDto::setPublicationYear);

              // Default values
              m.map(src -> "derived", CreatePublishedDataDto::setResourceType);
              m.map(src -> "pending_registration", CreatePublishedDataDto::setStatus);
            });

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
                      src -> src.getPublisher(),
                      (dst, v) -> dst.getPublisher().setName((String) v));
              m.when(isNotNull())
                  .using(stringListToPerson)
                  .map(PublishedData::getCreator, ZenodoDataset::setCreators);
            });

    return mapper;
  }
}
