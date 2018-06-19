package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease.APDisease;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Drug.APDrug;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utilities {
    private final static Logger slf4jLogger = LoggerFactory.getLogger(Utilities.class);


    public static AnnotationType getAnnotationTypeBasedOnId(String id){

        if (id.contains("HP")) {
            return AnnotationType.PHENOTYPE;
        }
        else if (StringUtils.isNumeric(id)){
            return AnnotationType.GENE;
        }
        else if (id.contains("MONDO")){
            return AnnotationType.DISEASE;
        }
        else if (id.contains("DB")){
            return AnnotationType.DRUG;
        }
        return
                AnnotationType.ENTITY;

    }

    public static ConceptFilter getFilterBasedOnId(String id) {
        ConceptFilter conceptFilter = new ConceptFilter();
        AnnotationType type = getAnnotationTypeBasedOnId(id);
        switch(type) {
            case PHENOTYPE:
                //Phenotype
                String text = DocumentPreprocessor.getPhenotypeHandler().getPhenotypeLabelWithId(id);
                conceptFilter.setId(id);
                conceptFilter.setType(type.toString());
                conceptFilter.setText(text);
                break;
            case GENE:
                // Gene
                APGene aGene = DocumentPreprocessor.getHGNCGeneHandler().getGeneWithId(id);
                if (aGene != null) {
                    conceptFilter.setId(id);
                    conceptFilter.setType(type.toString());
                    conceptFilter.setText(aGene.getApprovedSymbol());
                }
                break;

            case DISEASE:
                // Disease
                APDisease apDisease = DocumentPreprocessor.getMondoHandler().getDisease(id);
                if (apDisease != null) {
                    conceptFilter.setId(id);
                    conceptFilter.setType(type.toString());
                    conceptFilter.setText(apDisease.getLabel());
                }
                break;

            case DRUG:
                // Drug
                APDrug apDrug = DocumentPreprocessor.getDrugBankHandler().getDrug(id);
                if (apDrug != null) {
                    conceptFilter.setId(id);
                    conceptFilter.setType(type.toString());
                    conceptFilter.setText(apDrug.getLabel());
                }
                break;
        }

        return conceptFilter;
    }


    public static String getFirstHypotheticalSentence(String input, Integer maxSize) {
        if (Strings.isNullOrEmpty(input))
            return "N/A";

        try {
            String result;
            if (input.length() > maxSize) {
                String[] sents = input.split("\\. ");
                if (sents.length > 0) {
                    result = sents[0];
                } else
                    result = input;

                //Now result has a single sentence string.

                if (result.length() <= maxSize)
                    return String.format("%s. ...", result);
                else {
                    String builder = "";
                    boolean logicalBreak = false;
                    Integer index = 0;
                    while (!logicalBreak) {
                        Character ch = result.charAt(index);
                        builder = builder + Character.toString(ch);
                        if ((index > maxSize && ch == ' ') || (index == result.length() - 1))
                            logicalBreak = true;
                        index ++;
                    }
                    return String.format("%s...", builder);
                }


            }
            else {
                return input;
            }
        } catch (Exception e) {
            slf4jLogger.error(String.format("Issue in truncating description string : %s", input));
            return "N/A";
        }


    }

    public static String getAlphaPattern(String input){
        String newstr = input.replaceAll("\\P{L}+", "");
        return newstr;
    }

}
