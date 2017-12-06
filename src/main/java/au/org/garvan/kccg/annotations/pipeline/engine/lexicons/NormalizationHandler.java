package au.org.garvan.kccg.annotations.pipeline.engine.lexicons;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ahmed on 11/10/17.
 */
public class NormalizationHandler extends BaseLexiconHandler {
    private final List<String> normalizationHeader = Arrays.asList("Term", "Standard_Term");
    private Map<String, Integer> headerMap;
    private HashMap<String,String> normalizationLex = new HashMap<>();

    public NormalizationHandler(String fName) {
        fileName = fName;
        headerMap = IntStream.range(0, normalizationHeader.size()).boxed().collect(Collectors.toMap(normalizationHeader::get, Function.identity()));

    }

    public void loadLVGNormalizedList(){
        try {
            readFile(":");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(verifyHeader(normalizationHeader))
        {
            for (List<String> d : data) {
                if(!d.get(headerMap.get("Term")).equals(d.get(headerMap.get("Standard_Term"))))
                    normalizationLex.put(d.get(headerMap.get("Term")), d.get(headerMap.get("Standard_Term")));
            }
        }

    }

    public String getNormalizedText(String tokenText) {
        if (normalizationLex.containsKey(tokenText))
            return normalizationLex.get(tokenText);
        else
            return null;
    }



}
