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
        //Load Genes and process them to store in order form.
        try {
            readFile("\t");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (verifyHeader(HGNCFileHeader)) {
            for (List<String> d : data) {
                String geneSymbol = d.get(headerMap.get("Approved Symbol"));
                if (geneList.containsKey(geneSymbol)) {
                    APGene preExistedGene = geneList.get(geneSymbol);
                    preExistedGene.getGeneFamilyTag().add(d.get(headerMap.get("Gene Family Tag")));
                    preExistedGene.getGeneFamilyDescription().add(d.get(headerMap.get("Gene family description")));
                    preExistedGene.getGeneFamilyID().add(Integer.parseInt(d.get(headerMap.get("Gene family ID"))));

                } else {

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
                            //Point: Next three are string but to handle duplicate entries they are stored as editable lists.
                            new ArrayList<> (Arrays.asList(d.get(headerMap.get("Gene Family Tag")).isEmpty()?"None" : d.get(headerMap.get("Gene Family Tag")))),
                            new ArrayList<> (Arrays.asList(d.get(headerMap.get("Gene family description")).isEmpty()?"None":d.get(headerMap.get("Gene family description")))),
                            new ArrayList<> (Arrays.asList(Integer.parseInt(d.get(headerMap.get("Gene family ID")).isEmpty()? "0" : d.get(headerMap.get("Gene family ID")))))
                    );
                    geneList.put(geneSymbol, tempGene);
                }
            }
        }

    }

    public APGene getGene(String tokenText) {
        if (geneList.containsKey(tokenText))
            return geneList.get(tokenText);
        else
            return null;
    }




}
