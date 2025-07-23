package ch.psi.ord.core;

import static org.modelmapper.Conditions.isNotNull;

import ch.psi.ord.model.Person;
import ch.psi.ord.model.Publication;
import ch.psi.scicat.model.CreatePublishedDataDto;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
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

  @Produces
  public ModelMapper createPublicationModelMapper() {
    ModelMapper mapper = new ModelMapper();

    mapper
        .typeMap(Publication.class, CreatePublishedDataDto.class)
        .addMappings(
            m -> {
              m.when(isNotNull()).map(Publication::getIdentifier, CreatePublishedDataDto::setDoi);
              m.when(isNotNull()).map(Publication::getTitle, CreatePublishedDataDto::setTitle);
              m.when(isNotNull())
                  .using(personToStringList)
                  .map(Publication::getCreator, CreatePublishedDataDto::setCreator);
            });

    mapper
        .typeMap(CreatePublishedDataDto.class, Publication.class)
        .addMappings(
            m -> {
              m.map(CreatePublishedDataDto::getDoi, Publication::setIdentifier);
              m.map(CreatePublishedDataDto::getTitle, Publication::setTitle);
              m.using(stringListToPerson)
                  .map(CreatePublishedDataDto::getCreator, Publication::setCreator);
            });

    // mapper.validate();

    return mapper;
  }
}
