package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.APMultiWordAnnotationMapper;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.minBy;

@Component
public class AnnotationControl {
    private static Integer filtersCount = 200;
    private static final List<String> controlList = Arrays.asList("HP:0002664", "11515", "HP:0012531","MONDO:0005070","MONDO:0004992");


    public static List<ConceptFilter> getControlledFilters(List<ConceptFilter> input){

        List<ConceptFilter> restrictedList = input.stream().filter(f-> !controlList.contains(f.getId()))
                .filter(f->f.getFilteredArticleCount()>0).collect(Collectors.toList());

        restrictedList.sort(Comparator.comparing(ConceptFilter::getFilteredArticleCount).reversed());

        Map<String, List<ConceptFilter>> groupedFilters = restrictedList.stream()
                .collect(groupingBy(ConceptFilter::getType));

        Integer rank = restrictedList.size();
        List<ConceptFilter> finalList = new ArrayList<>();

        Integer counter = 0;
        Set<String> exhausted = new HashSet<>();
        while (exhausted.size() < groupedFilters.size()){
            for(List<ConceptFilter> conceptFilters : groupedFilters.values() ){
                if(counter<conceptFilters.size()){
                    ConceptFilter thisFilter = conceptFilters.get(counter);
                    thisFilter.setRank(rank-counter);
                    finalList.add(thisFilter);
                }
                else
                {
                    exhausted.add(conceptFilters.get(0).getType());
                }

            }
            counter ++;
        }
        return finalList.subList(0, Math.min(finalList.size(),filtersCount));


    }



    public static List<APMultiWordAnnotationMapper> getControlledMWAnnotations(List<APMultiWordAnnotationMapper> input){
        return input.stream().filter(f-> !controlList.contains(f.getId())).collect(Collectors.toList());
    }


    public static List<APGene> getControlledGeneAnnotations(List<APGene> input){
        return input.stream().filter(f-> !controlList.contains(String.valueOf(f.getHGNCID()))).collect(Collectors.toList());
    }


}
