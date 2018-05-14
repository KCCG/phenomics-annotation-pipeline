package au.org.garvan.kccg.annotations.pipeline.engine.utilities.constants;

import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;

public class GraphDBConstants {

    public static final String ARTICLE_NODE_LABEL = "Article";
    public static final String ARTICLE_NODE_ID = "PMID";
    public static final String ARTICLE_NODE_LANGUAGE = "Language";
    public static final String ARTICLE_NODE_PROCESSING_DATE = "ProcessingDate";

    public static final String AUTHOR_NODE_LABEL = "Author";
    public static final String AUTHOR_NODE_INITIALS = "Initials";
    public static final String AUTHOR_NODE_FORE_NAME = "ForeName";
    public static final String AUTHOR_NODE_LAST_NAME = "LastName";
    public static final String AUTHOR_EDGE_TYPE = "WROTE";
    public static final String AUTHOR_EDGE_ORDER = "Order";


    public static final String MESH_HEADING_NODE_LABEL = "MeshHeading";
    public static final String MESH_HEADING_NODE_ID = "UI";
    public static final String MESH_HEADING_TEXT = "Text";
    public static final String MESH_HEADING_EDGE_TYPE = "MESHED";


    public static final String PUBLICATION_NODE_LABEL = "Publication";
    public static final String PUBLICATION_NODE_TITLE = "Title";
    public static final String PUBLICATION_NODE_ISO_ABBREVIATION = "IsoAbbreviation";
    public static final String PUBLICATION_NODE_ISSN_TYPE = "IssnType";
    public static final String PUBLICATION_NODE_ISSN_NUMBER = "IssnNumber";
    public static final String PUBLICATION_EDGE_TYPE = "PUBLISHED";
    public static final String PUBLICATION_EDGE_DATE_PUBLISHED = "DatePublished";


    public static final String ENTITY_NODE_LABEL = "Entity";

    public static final String ENTITY_NODE_ID = "EID";
    public static final String ENTITY_NODE_TEXT = "Text";
    public static final String ENTITY_NODE_VERSION = "Version";
    public static final String ENTITY_NODE_STANDARD = "Standard";
    public static final String ENTITY_NODE_TYPE = "Type";
    public static final String ENTITY_EDGE_TYPE = "CONTAINS";
    public static final String ENTITY_EDGE_SENT_ID= "SentID";
    public static final String ENTITY_EDGE_DOC_OFFSET_BEGIN= "DocOffsetBegin";
    public static final String ENTITY_EDGE_DOC_OFFSET_END= "DocOffsetEnd";
    public static final String ENTITY_EDGE_FIELD= "Field";



    public static String getEntityLabel(AnnotationType type) {
        switch (type) {
            case GENE:
                return "Gene";
            case DISEASE:
                return "Disease";
            case PHENOTYPE:
                return "Phenotype";
            case SYMPTOM:
                return "Symptom";
            case DRUG:
                return "Drug";
            case VARIANT:
                return "Variant";
            default:
                return "Entity";
        }
    }

    public static String CACHED_QUERY_ARTICLE_RESULT_SET_PAID_LABEL = "pmid";
    public static String CACHED_QUERY_ARTICLE_RESULT_SET_SEARCH_COUNTS_LABEL = "shits";



}
