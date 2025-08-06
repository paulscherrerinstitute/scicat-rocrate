package ch.psi.ord.core;

import static org.modelmapper.Conditions.isNotNull;

import ch.psi.ord.model.Person;
import ch.psi.ord.model.Publication;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.PublishedData;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

@Singleton
public class ScicatModelMapper {
  Converter<List<Person>, List<String>> personToStringList =
      new Converter<>() {
        @Override
        public List<String> convert(MappingContext<List<Person>, List<String>> context) {
          return context.getSource().stream().map(Person::name).collect(Collectors.toList());
        }
      };

  Converter<List<String>, List<Person>> stringListToPerson =
      new Converter<>() {
        @Override
        public List<Person> convert(MappingContext<List<String>, List<Person>> context) {
          return context.getSource().stream()
              .map(str -> new Person().name(str))
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

  @Produces
  public ModelMapper createPublicationModelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setSkipNullEnabled(true);

    mapper
        .typeMap(Publication.class, CreatePublishedDataDto.class)
        .addMappings(
            m -> {
              m.when(isNotNull()).map(Publication::identifier, (dst, val) -> dst._id((String) val));
              m.when(isNotNull()).map(Publication::identifier, (dst, val) -> dst.doi((String) val));
              m.when(isNotNull())
                  .using(personToStringList)
                  .map(Publication::creator, (dst, val) -> dst.creator((List<String>) val));
              m.when(isNotNull()).map(Publication::title, (dst, val) -> dst.title((String) val));
              m.when(isNotNull())
                  .map(src -> src.publisher().name(), (dst, val) -> dst.publisher((String) val));
              m.when(isNotNull())
                  .map(Publication::abstract_, (dst, val) -> dst.abstract_((String) val));
              m.when(isNotNull())
                  .map(Publication::description, (dst, val) -> dst.dataDescription((String) val));
              m.when(isNotNull())
                  .using(dateToYear)
                  .map(
                      Publication::datePublished,
                      (dst, val) -> dst.publicationYear(val != null ? (int) val : 0));

              // Default values
              m.map(src -> "derived", (dst, val) -> dst.resourceType((String) val));
              m.map(src -> "registered", (dst, val) -> dst.status((String) val));
            });

    mapper
        .typeMap(CreatePublishedDataDto.class, PublishedData.class)
        .addMappings(
            m -> {
              m.map(CreatePublishedDataDto::doi, (dst, val) -> dst.doi((String) val));
              m.map(
                  CreatePublishedDataDto::affiliation, (dst, val) -> dst.affiliation((String) val));
              m.map(CreatePublishedDataDto::creator, (dst, val) -> dst.creator((List<String>) val));
              m.map(CreatePublishedDataDto::publisher, (dst, val) -> dst.publisher((String) val));
              m.map(
                  CreatePublishedDataDto::publicationYear,
                  (dst, val) -> dst.publicationYear(val != null ? (int) val : 0));
              m.map(CreatePublishedDataDto::title, (dst, val) -> dst.title((String) val));
              m.map(CreatePublishedDataDto::url, (dst, val) -> dst.url((String) val));
              m.map(CreatePublishedDataDto::abstract_, (dst, val) -> dst.abstract_((String) val));
              m.map(
                  CreatePublishedDataDto::dataDescription,
                  (dst, val) -> dst.dataDescription((String) val));
              m.map(
                  CreatePublishedDataDto::resourceType,
                  (dst, val) -> dst.resourceType((String) val));
              m.map(
                  CreatePublishedDataDto::numberOfFiles,
                  (dst, val) -> dst.numberOfFiles(val != null ? (long) val : 0));
              m.map(
                  CreatePublishedDataDto::sizeOfArchive,
                  (dst, val) -> dst.sizeOfArchive(val != null ? (long) val : 0));
              m.map(
                  CreatePublishedDataDto::pidArray, (dst, val) -> dst.pidArray((List<String>) val));
              m.map(CreatePublishedDataDto::authors, (dst, val) -> dst.authors((List<String>) val));
              m.map(
                  CreatePublishedDataDto::registeredTime,
                  (dst, val) -> dst.registeredTime((String) val));
              m.map(CreatePublishedDataDto::status, (dst, val) -> dst.status((String) val));
              m.map(CreatePublishedDataDto::scicatUser, (dst, val) -> dst.scicatUser((String) val));
              m.map(CreatePublishedDataDto::thumbnail, (dst, val) -> dst.thumbnail((String) val));
              m.map(
                  CreatePublishedDataDto::relatedPublications,
                  (dst, val) -> dst.relatedPublications((List<String>) val));
              m.map(
                  CreatePublishedDataDto::downloadLink,
                  (dst, val) -> dst.downloadLink((String) val));
            });

    mapper
        .typeMap(PublishedData.class, CreatePublishedDataDto.class)
        .addMappings(
            m -> {
              m.map(PublishedData::doi, (dst, val) -> dst._id((String) val));
              m.map(PublishedData::doi, (dst, val) -> dst.doi((String) val));
              m.map(PublishedData::affiliation, (dst, val) -> dst.affiliation((String) val));
              m.map(PublishedData::creator, (dst, val) -> dst.creator((List<String>) val));
              m.map(PublishedData::publisher, (dst, val) -> dst.publisher((String) val));
              m.map(
                  PublishedData::publicationYear,
                  (dst, val) -> dst.publicationYear(val != null ? (int) val : 0));
              m.map(PublishedData::title, (dst, val) -> dst.title((String) val));
              m.map(PublishedData::url, (dst, val) -> dst.url((String) val));
              m.map(PublishedData::abstract_, (dst, val) -> dst.abstract_((String) val));
              m.map(
                  PublishedData::dataDescription, (dst, val) -> dst.dataDescription((String) val));
              m.map(PublishedData::resourceType, (dst, val) -> dst.resourceType((String) val));
              m.map(
                  PublishedData::numberOfFiles,
                  (dst, val) -> dst.numberOfFiles(val != null ? (int) val : 0));
              m.map(
                  PublishedData::sizeOfArchive,
                  (dst, val) -> dst.sizeOfArchive(val != null ? (int) val : 0));
              m.map(PublishedData::pidArray, (dst, val) -> dst.pidArray((List<String>) val));
              m.map(PublishedData::authors, (dst, val) -> dst.authors((List<String>) val));
              m.map(PublishedData::registeredTime, (dst, val) -> dst.registeredTime((String) val));
              m.map(PublishedData::status, (dst, val) -> dst.status((String) val));
              m.map(PublishedData::scicatUser, (dst, val) -> dst.scicatUser((String) val));
              m.map(PublishedData::thumbnail, (dst, val) -> dst.thumbnail((String) val));
              m.map(
                  PublishedData::relatedPublications,
                  (dst, val) -> dst.relatedPublications((List<String>) val));
              m.map(PublishedData::downloadLink, (dst, val) -> dst.downloadLink((String) val));
            });

    return mapper;
  }
}
