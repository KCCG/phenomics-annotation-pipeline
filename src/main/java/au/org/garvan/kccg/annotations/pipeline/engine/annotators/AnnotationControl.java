package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.APMultiWordAnnotationMapper;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnnotationControl {
    private static Integer filtersCount = 200;
    private static final List<String> controlList = Arrays.asList("HP:0002664", "11515", "HP:0012531");


    public static List<ConceptFilter> getControlledFilters(List<ConceptFilter> input){
        return input.stream().filter(f-> !controlList.contains(f.getId())).limit(filtersCount).collect(Collectors.toList());
    }



    public static List<APMultiWordAnnotationMapper> getControlledMWAnnotations(List<APMultiWordAnnotationMapper> input){
        return input.stream().filter(f-> !controlList.contains(f.getId())).collect(Collectors.toList());
    }


    public static List<APGene> getControlledGeneAnnotations(List<APGene> input){
        return input.stream().filter(f-> !controlList.contains(String.valueOf(f.getHGNCID()))).collect(Collectors.toList());
    }


}
