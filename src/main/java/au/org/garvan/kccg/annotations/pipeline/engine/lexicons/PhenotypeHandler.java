package au.org.garvan.kccg.annotations.pipeline.engine.lexicons;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APPhenotype;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ahmed on 19/01/18.
 */
public class PhenotypeHandler extends BaseLexiconHandler {
    private final List<String> tempHPOHeader = Arrays.asList("Id", "Text");
    private Map<String, Integer> headerMap;
    private List<APPhenotype> phenotypeList = new ArrayList<>();


    public PhenotypeHandler(String fName) {

        fileName = fName;
        headerMap = IntStream.range(0, tempHPOHeader.size()).boxed().collect(Collectors.toMap(tempHPOHeader::get, Function.identity()));
    }

    public void loadPhenotypes() {
        //Load Genes and process them to store in order form.
        try {
            readFile("\t");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (verifyHeader(tempHPOHeader)) {
            for (List<String> d : data) {
                phenotypeList.add(new APPhenotype(d.get(headerMap.get("Id")), d.get(headerMap.get("Text"))));
            }
        }
    }

    public List<APPhenotype> serchPhenotype(String text) {
         List<APPhenotype> collectedPhenotypes = phenotypeList.stream().
                filter(x -> x.getText().toUpperCase().contains(text))
                 .collect(Collectors.toList());
        return collectedPhenotypes;


    }


}
