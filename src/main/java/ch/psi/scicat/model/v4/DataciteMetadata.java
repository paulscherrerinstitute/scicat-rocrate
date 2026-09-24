package ch.psi.scicat.model.v4;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataciteMetadata {
  @JsonProperty private List<Creator> creators = new ArrayList<>();
  @JsonProperty private Publisher publisher;
  @JsonProperty private Integer publicationYear;
  @JsonProperty private List<Contributor> contributors = new ArrayList<>();
  @JsonProperty private List<Subject> subjects = new ArrayList<>();
  @JsonProperty private List<Date> dates = new ArrayList<>();
  @JsonProperty private String language;
  @JsonProperty private Type types;
  @JsonProperty private List<AlternateIdentifier> alternateIdentifiers = new ArrayList<>();
  @JsonProperty private List<RelatedIdentifier> relatedIdentifiers = new ArrayList<>();
  @JsonProperty private List<String> sizes = new ArrayList<>();
  @JsonProperty private List<String> formats = new ArrayList<>();
  @JsonProperty private String version;
  @JsonProperty private List<Right> rightsList = new ArrayList<>();
  @JsonProperty private List<Description> descriptions = new ArrayList<>();
  @JsonProperty private List<FundingReference> fundingReferences = new ArrayList<>();
  @JsonProperty private List<RelatedItem> relatedItems = new ArrayList<>();

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Creator {
    @JsonProperty private String name;
    @JsonProperty private NameType nameType;
    @JsonProperty private String givenName;
    @JsonProperty private String familyName;
    @JsonProperty private List<Affiliation> affiliation = new ArrayList<>();
    @JsonProperty private List<NameIdentifier> nameIdentifiers = new ArrayList<>();
    @JsonProperty private String lang;
  }

  public enum NameType {
    @JsonProperty("Personal")
    PERSONAL,
    @JsonProperty("Organizational")
    ORGANIZATIONAL
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Affiliation {
    @JsonProperty private String affiliationIdentifier;
    @JsonProperty private String affiliationIdentifierScheme;
    @JsonProperty private String name;
    @JsonProperty private String schemeUri;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class NameIdentifier {
    @JsonProperty private String nameIdentifier;
    @JsonProperty private String nameIdentifierScheme;
    @JsonProperty private String schemeUri;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  public static class Contributor extends Creator {
    @JsonProperty private ContributorType contributorType;
  }

  public enum ContributorType {
    @JsonProperty("Personal")
    PERSONAL,
    @JsonProperty("Organizational")
    ORGANIZATIONAL,
    @JsonProperty("ContactPerson")
    CONTACT_PERSON,
    @JsonProperty("DataCollector")
    DATA_COLLECTOR,
    @JsonProperty("DataCurator")
    DATA_CURATOR,
    @JsonProperty("DataManager")
    DATA_MANAGER,
    @JsonProperty("Distributor")
    DISTRIBUTOR,
    @JsonProperty("Editor")
    EDITOR,
    @JsonProperty("HostingInstitution")
    HOSTING_INSTITUTION,
    @JsonProperty("Producer")
    PRODUCER,
    @JsonProperty("ProjectLeader")
    PROJECT_LEADER,
    @JsonProperty("ProjectManager")
    PROJECT_MANAGER,
    @JsonProperty("ProjectMember")
    PROJECT_MEMBER,
    @JsonProperty("RegistrationAgency")
    REGISTRATION_AGENCY,
    @JsonProperty("RegistrationAuthority")
    REGISTRATION_AUTHORITY,
    @JsonProperty("RelatedPerson")
    RELATED_PERSON,
    @JsonProperty("Researcher")
    RESEARCHER,
    @JsonProperty("ResearchGroup")
    RESEARCH_GROUP,
    @JsonProperty("RightsHolder")
    RIGHTS_HOLDER,
    @JsonProperty("Sponsor")
    SPONSOR,
    @JsonProperty("Supervisor")
    SUPERVISOR,
    @JsonProperty("Translator")
    TRANSLATOR,
    @JsonProperty("WorkPackageLeader")
    WORK_PACKAGE_LEADER,
    @JsonProperty("Other")
    OTHER
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Publisher {
    @JsonProperty private String name;
    @JsonProperty private String publisherIdentifier;
    @JsonProperty private String publisherIdentifierScheme;
    @JsonProperty private String schemeUri;
    @JsonProperty private String lang;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Subject {
    @JsonProperty private String subject;
    @JsonProperty private String subjectScheme;
    @JsonProperty private String schemeUri;
    @JsonProperty private String valueUri;
    @JsonProperty private String lang;
    @JsonProperty private String classificationCode;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Date {
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant date;

    @JsonProperty private DateType dateType;
    @JsonProperty private String dateInformation;
  }

  public enum DateType {
    @JsonProperty("Accepted")
    ACCEPTED,
    @JsonProperty("Available")
    AVAILABLE,
    @JsonProperty("Copyrighted")
    COPYRIGHTED,
    @JsonProperty("Collected")
    COLLECTED,
    @JsonProperty("Coverage")
    COVERAGE,
    @JsonProperty("Created")
    CREATED,
    @JsonProperty("Issued")
    ISSUED,
    @JsonProperty("Submitted")
    SUBMITTED,
    @JsonProperty("Updated")
    UPDATED,
    @JsonProperty("Valid")
    VALID,
    @JsonProperty("Withdrawn")
    WITHDRAWN,
    @JsonProperty("Other")
    OTHER
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Type {
    @JsonProperty private String resourceType;
    @JsonProperty private ResourceTypeGeneral resourceTypeGeneral;
  }

  public enum ResourceTypeGeneral {
    @JsonProperty("Audiovisual")
    AUDIOVISUAL,
    @JsonProperty("Award")
    AWARD,
    @JsonProperty("Book")
    BOOK,
    @JsonProperty("BookChapter")
    BOOK_CHAPTER,
    @JsonProperty("Collection")
    COLLECTION,
    @JsonProperty("ComputationalNotebook")
    COMPUTATIONAL_NOTEBOOK,
    @JsonProperty("ConferencePaper")
    CONFERENCE_PAPER,
    @JsonProperty("ConferenceProceeding")
    CONFERENCE_PROCEEDING,
    @JsonProperty("DataPaper")
    DATA_PAPER,
    @JsonProperty("Dataset")
    DATASET,
    @JsonProperty("Dissertation")
    DISSERTATION,
    @JsonProperty("Event")
    EVENT,
    @JsonProperty("Image")
    IMAGE,
    @JsonProperty("InteractiveResource")
    INTERACTIVE_RESOURCE,
    @JsonProperty("Instrument")
    INSTRUMENT,
    @JsonProperty("Journal")
    JOURNAL,
    @JsonProperty("JournalArticle")
    JOURNAL_ARTICLE,
    @JsonProperty("Model")
    MODEL,
    @JsonProperty("OutputManagementPlan")
    OUTPUT_MANAGEMENT_PLAN,
    @JsonProperty("PeerReview")
    PEER_REVIEW,
    @JsonProperty("PhysicalObject")
    PHYSICAL_OBJECT,
    @JsonProperty("Poster")
    POSTER,
    @JsonProperty("Preprint")
    PREPRINT,
    @JsonProperty("Presentation")
    PRESENTATION,
    @JsonProperty("Project")
    PROJECT,
    @JsonProperty("Report")
    REPORT,
    @JsonProperty("Service")
    SERVICE,
    @JsonProperty("Software")
    SOFTWARE,
    @JsonProperty("Sound")
    SOUND,
    @JsonProperty("Standard")
    STANDARD,
    @JsonProperty("StudyRegistration")
    STUDY_REGISTRATION,
    @JsonProperty("Text")
    TEXT,
    @JsonProperty("Workflow")
    WORKFLOW,
    @JsonProperty("Other")
    OTHER
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class AlternateIdentifier {
    @JsonProperty private String alternateIdentifierType;
    @JsonProperty private String alternateIdentifier;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class RelatedIdentifier {
    @JsonProperty private String relatedIdentifier;
    @JsonProperty private RelatedIdentifierType relatedIdentifierType;
    @JsonProperty private RelationType relationType;
    @JsonProperty private String relatedMetadataScheme;
    @JsonProperty private String schemeURI;
    @JsonProperty private String schemeType;
    @JsonProperty private ResourceTypeGeneral resourceTypeGeneral;
    @JsonProperty private String relationTypeInformation;
  }

  public enum RelatedIdentifierType {
    @JsonProperty("ARK")
    ARK,
    @JsonProperty("arXiv")
    AR_XIV,
    @JsonProperty("bibcode")
    BIBCODE,
    @JsonProperty("CSTR")
    CSTR,
    @JsonProperty("DOI")
    DOI,
    @JsonProperty("EAN13")
    EAN_13,
    @JsonProperty("EISSN")
    EISSN,
    @JsonProperty("Handle")
    HANDLE,
    @JsonProperty("IGSN")
    IGSN,
    @JsonProperty("ISBN")
    ISBN,
    @JsonProperty("ISSN")
    ISSN,
    @JsonProperty("ISTC")
    ISTC,
    @JsonProperty("LISSN")
    LISSN,
    @JsonProperty("LSID")
    LSID,
    @JsonProperty("PMID")
    PMID,
    @JsonProperty("PURL")
    PURL,
    @JsonProperty("RAiD")
    RAID,
    @JsonProperty("RRID")
    RRID,
    @JsonProperty("SWHID")
    SWHID,
    @JsonProperty("UPC")
    UPC,
    @JsonProperty("URL")
    URL,
    @JsonProperty("URN")
    URN,
    @JsonProperty("w3id")
    W3ID
  }

  public enum RelationType {
    @JsonProperty("IsCitedBy")
    IS_CITED_BY,
    @JsonProperty("Cites")
    CITES,
    @JsonProperty("IsSupplementTo")
    IS_SUPPLEMENT_TO,
    @JsonProperty("IsSupplementedBy")
    IS_SUPPLEMENTED_BY,
    @JsonProperty("IsContinuedBy")
    IS_CONTINUED_BY,
    @JsonProperty("Continues")
    CONTINUES,
    @JsonProperty("IsDescribedBy")
    IS_DESCRIBED_BY,
    @JsonProperty("Describes")
    DESCRIBES,
    @JsonProperty("HasMetadata")
    HAS_METADATA,
    @JsonProperty("IsMetadataFor")
    IS_METADATA_FOR,
    @JsonProperty("HasVersion")
    HAS_VERSION,
    @JsonProperty("IsVersionOf")
    IS_VERSION_OF,
    @JsonProperty("IsNewVersionOf")
    IS_NEW_VERSION_OF,
    @JsonProperty("IsPreviousVersionOf")
    IS_PREVIOUS_VERSION_OF,
    @JsonProperty("IsPartOf")
    IS_PART_OF,
    @JsonProperty("HasPart")
    HAS_PART,
    @JsonProperty("IsPublishedIn")
    IS_PUBLISHED_IN,
    @JsonProperty("IsReferencedBy")
    IS_REFERENCED_BY,
    @JsonProperty("References")
    REFERENCES,
    @JsonProperty("IsDocumentedBy")
    IS_DOCUMENTED_BY,
    @JsonProperty("Documents")
    DOCUMENTS,
    @JsonProperty("IsCompiledBy")
    IS_COMPILED_BY,
    @JsonProperty("Compiles")
    COMPILES,
    @JsonProperty("IsVariantFormOf")
    IS_VARIANT_FORM_OF,
    @JsonProperty("IsOriginalFormOf")
    IS_ORIGINAL_FORM_OF,
    @JsonProperty("IsIdenticalTo")
    IS_IDENTICAL_TO,
    @JsonProperty("IsReviewedBy")
    IS_REVIEWED_BY,
    @JsonProperty("Reviews")
    REVIEWS,
    @JsonProperty("IsDerivedFrom")
    IS_DERIVED_FROM,
    @JsonProperty("IsSourceOf")
    IS_SOURCE_OF,
    @JsonProperty("IsRequiredBy")
    IS_REQUIRED_BY,
    @JsonProperty("Requires")
    REQUIRES,
    @JsonProperty("IsObsoletedBy")
    IS_OBSOLETED_BY,
    @JsonProperty("Obsoletes")
    OBSOLETES,
    @JsonProperty("IsCollectedBy")
    IS_COLLECTED_BY,
    @JsonProperty("Collects")
    COLLECTS,
    @JsonProperty("IsTranslationOf")
    IS_TRANSLATION_OF,
    @JsonProperty("HasTranslation")
    HAS_TRANSLATION,
    @JsonProperty("Other")
    OTHER
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Right {
    @JsonProperty private String rights;
    @JsonProperty private String rightsUri;
    @JsonProperty private String schemeUri;
    @JsonProperty private String rightsIdentifier;
    @JsonProperty private String rightsIdentifierScheme;
    @JsonProperty private String lang;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Description {
    @JsonProperty private String lang;
    @JsonProperty private String description;
    @JsonProperty private DescriptionType descriptionType;
  }

  public enum DescriptionType {
    @JsonProperty("Abstract")
    ABSTRACT,
    @JsonProperty("Methods")
    METHODS,
    @JsonProperty("SeriesInformation")
    SERIES_INFORMATION,
    @JsonProperty("TableOfContents")
    TABLE_OF_CONTENTS,
    @JsonProperty("TechnicalInfo")
    TECHNICAL_INFO,
    @JsonProperty("Other")
    OTHER
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class FundingReference {
    @JsonProperty private String awardUri;
    @JsonProperty private String awardTitle;
    @JsonProperty private String funderName;
    @JsonProperty private String awardNumber;
    @JsonProperty private String funderIdentifier;
    @JsonProperty private FunderIdentifierType funderIdentifierType;
  }

  public enum FunderIdentifierType {
    @JsonProperty("Crossref Funder ID")
    CROSSREF_FUNDER_ID,
    @JsonProperty("GRID")
    GRID,
    @JsonProperty("ISNI")
    ISNI,
    @JsonProperty("ROR")
    ROR,
    @JsonProperty("Other")
    Other
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class RelatedItem {
    @JsonProperty private ResourceTypeGeneral relatedItemType;
    @JsonProperty private RelationType relationType;
    @JsonProperty private String relationTypeInformation;
    @JsonProperty private RelatedItemIdentifier relatedItemIdentifier;
    @JsonProperty private List<Creator> creators = new ArrayList<>();
    @JsonProperty private List<Title> titles = new ArrayList<>();
    @JsonProperty private String publicationYear;
    @JsonProperty private String volume;
    @JsonProperty private String issue;
    @JsonProperty private String number;
    @JsonProperty private NumberType numberType;
    @JsonProperty private String firstPage;
    @JsonProperty private String lastPage;
    @JsonProperty private String publisher;
    @JsonProperty private String edition;
    @JsonProperty private List<Contributor> contributors = new ArrayList<>();
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class RelatedItemIdentifier {
    @JsonProperty private String relatedItemIdentifier;
    @JsonProperty private RelatedIdentifierType relatedItemIdentifierType;
    @JsonProperty private String relatedMetadataScheme;
    @JsonProperty private String schemeURI;
    @JsonProperty private String schemeType;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Title {
    @JsonProperty private String title;
    @JsonProperty private TitleType titleType;
    @JsonProperty private String lang;
  }

  public enum TitleType {
    @JsonProperty("AlternativeTitle")
    ALTERNATIVE_TITLE,
    @JsonProperty("Subtitle")
    SUBTITLE,
    @JsonProperty("TranslatedTitle")
    TRANSLATED_TITLE,
    @JsonProperty("Other")
    OTHER
  }

  public enum NumberType {
    @JsonProperty("Article")
    ARTICLE,
    @JsonProperty("Chapter")
    CHAPTER,
    @JsonProperty("Report")
    REPORT,
    @JsonProperty("Other")
    OTHER
  }
}
