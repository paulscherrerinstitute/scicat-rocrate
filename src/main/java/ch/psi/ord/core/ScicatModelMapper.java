package ch.psi.ord.core;

import static org.modelmapper.Conditions.isNotNull;

import ch.psi.ord.model.Person;
import ch.psi.ord.model.PropertyValue;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ScicatDataset;
import ch.psi.ord.model.ZenodoDataset;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.DatasetType;
import ch.psi.scicat.model.v3.PublishedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

  Converter<List<Person>, String> personListToOwnerString =
      new Converter<>() {
        @Override
        public String convert(MappingContext<List<Person>, String> context) {
          return context.getSource().stream()
              .map(p -> String.format("%s %s", p.getGivenName(), p.getFamilyName()))
              .collect(Collectors.joining("; "));
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

  Converter<List<PropertyValue>, ObjectNode> scientficMetadaConverter =
      new Converter<List<PropertyValue>, ObjectNode>() {
        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public ObjectNode convert(MappingContext<List<PropertyValue>, ObjectNode> context) {
          List<PropertyValue> source = context.getSource();
          if (source == null) return null;

          ObjectNode root = mapper.createObjectNode();
          for (PropertyValue property : source) {
            processProperty(root, property);
          }
          return root;
        }

        private void processProperty(ObjectNode parent, PropertyValue property) {
          String name = property.getName();
          Object val = property.getValue();
          String unit = property.getUnitText();

          if (val instanceof String stringVal) {
            if (unit != null && !unit.isEmpty()) {
              ObjectNode valueWithUnit = mapper.createObjectNode();
              valueWithUnit.put("value", stringVal);
              valueWithUnit.put("unit", unit);
              parent.set(name, valueWithUnit);
            } else {
              parent.put(name, stringVal);
            }
          } else if (val instanceof List) {
            @SuppressWarnings("unchecked")
            List<PropertyValue> nestedList = (List<PropertyValue>) val;
            ObjectNode childNode = mapper.createObjectNode();

            for (PropertyValue childProperty : nestedList) {
              processProperty(childNode, childProperty);
            }
            parent.set(name, childNode);
          }
        }
      };

  Converter<List<String>, List<String>> keywordsConverter =
      new Converter<List<String>, List<String>>() {

        @Override
        public List<String> convert(MappingContext<List<String>, List<String>> context) {
          List<String> source = context.getSource();

          if (source == null || source.isEmpty()) {
            return new java.util.ArrayList<>();
          }

          return source.stream()
              .filter(Objects::nonNull)
              .map(entry -> entry.split(","))
              .flatMap(Arrays::stream)
              .map(String::trim)
              .filter(str -> !str.isEmpty())
              .collect(Collectors.toList());
        }
      };

  Converter<String, String> uriPathExtractor =
      context -> {
        String sourceId = context.getSource();
        try {
          URI uri = new URI(sourceId);
          return uri.getPath();
        } catch (Exception e) {
          return sourceId;
        }
      };
  Converter<String, String> uriHostExtractor =
      context -> {
        String sourceId = context.getSource();
        try {
          URI uri = new URI(sourceId);
          return uri.getHost();
        } catch (Exception e) {
          return sourceId;
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

    mapper
        .typeMap(ScicatDataset.class, CreateDatasetDto.class)
        .addMappings(
            m -> {
              m.using(scientficMetadaConverter)
                  .map(
                      ScicatDataset::getScientificMetadata,
                      CreateDatasetDto::setScientificMetadata);
              m.map(src -> "rocrate", CreateDatasetDto::setPrincipalInvestigator);
              m.map(src -> "rocrate", CreateDatasetDto::setOwnerGroup);
              m.using(personListToOwnerString)
                  .map(ScicatDataset::getOwner, CreateDatasetDto::setOwner);
              m.map(ScicatDataset::getName, CreateDatasetDto::setDatasetName);
              m.map(src -> "rocrate", CreateDatasetDto::setCreationLocation);
              m.map(src -> DatasetType.RAW, CreateDatasetDto::setType);
              m.map(src -> "rocrate@psi.ch", CreateDatasetDto::setContactEmail);
              m.when(isNotNull())
                  .using(uriPathExtractor)
                  .map(ScicatDataset::getId, CreateDatasetDto::setSourceFolder);
              m.when(isNotNull())
                  .using(uriHostExtractor)
                  .map(ScicatDataset::getId, CreateDatasetDto::setSourceFolderHost);
              m.map(src -> Instant.now(), CreateDatasetDto::setCreationTime);
              m.map(ScicatDataset::getDescription, CreateDatasetDto::setDescription);
              m.using(keywordsConverter)
                  .map(ScicatDataset::getKeywords, CreateDatasetDto::setKeywords);
            });

    return mapper;
  }
}
