package au.org.garvan.kccg.annotations.pipeline.lexicons;

import au.org.garvan.kccg.annotations.pipeline.entities.lexical.APGene;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ahmed on 3/10/17.
 */
public class GenesHandler extends BaseLexiconHandler {
    private final List<String> HGNCFileHeader = Arrays.asList("HGNC ID", "Approved Symbol", "Approved Name", "Status", "Previous Symbols", "Synonyms", "Chromosome", "Accession Numbers", "RefSeq IDs", "Gene Family Tag", "Gene family description", "Gene family ID");
    private Map<String, Integer> headerMap;


    private Map<String, APGene> geneList = new HashMap<>();

    public GenesHandler(String fName) {

        fileName = fName;
        headerMap = IntStream.range(0, HGNCFileHeader.size()).boxed().collect(Collectors.toMap(HGNCFileHeader::get, Function.identity()));
    }


    public void loadGenes() {
        int[] arr = {0};
        try {
            readFile("\t");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (verifyHeader()) {
            for (List<String> d : data) {
                arr[0]++;
                String geneSymbol = d.get(headerMap.get("Approved Symbol"));
                if (geneList.containsKey(geneSymbol))
                    System.out.print(String.format("Found Duplicate ID %s", geneSymbol));
                if (geneSymbol.isEmpty())
                    System.out.print(String.format("Found Empty ID %s", d.get(headerMap.get("HGNC ID"))));

                System.out.print(arr[0] + ":" + geneSymbol + "\n" );

                APGene tempGene = new APGene(
                        Integer.parseInt(d.get(headerMap.get("HGNC ID"))),
                        d.get(headerMap.get("Approved Symbol")),
                        d.get(headerMap.get("Approved Name")),
                        d.get(headerMap.get("Status")),
                        Arrays.asList(d.get(headerMap.get("Previous Symbols")).split(",")),
                        Arrays.asList(d.get(headerMap.get("Synonyms")).split(",")),
                        Arrays.asList(d.get(headerMap.get("Chromosome")).split(",")),
                        Arrays.asList(d.get(headerMap.get("Accession Numbers")).split(",")),
                        Arrays.asList(d.get(headerMap.get("RefSeq IDs")).split(",")),
                        d.get(headerMap.get("Gene Family Tag")),
                        d.get(headerMap.get("Gene family description")),
                        Integer.parseInt(d.get(headerMap.get("Gene family ID")))
                );
                geneList.put(geneSymbol, tempGene);
            }


        }


    }

    public APGene getGene(String tokenText) {
        if (geneList.containsKey(tokenText))
            return geneList.get(tokenText);
        else
            return null;
    }

    private boolean verifyHeader() {
        //Point: Check the Header and in case file got different header than reject the file.
        return fileHeader.toString().equals(HGNCFileHeader.toString());

    }


}
