package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Disease.APDisease;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.engine.preprocessors.DocumentPreprocessor;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Tokenizer;

public class Utilities {

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
        }

        return conceptFilter;
    }


    public static String getFirstHypotheticalSentence(String input, Integer maxSize){

        String result;
        if(input.length()>maxSize)
        {
            String [] sents = input.split( "\\. ");
            if(sents.length>0){
                if(sents[0].length()<=maxSize)
                    result=  sents[0];
                else
                    result= sents[0].substring(0,maxSize).trim();
            }
            else
                result = input.substring(0, maxSize).trim();

            return String.format("%s...", result);

        }
        else
            return input;




    }

}
