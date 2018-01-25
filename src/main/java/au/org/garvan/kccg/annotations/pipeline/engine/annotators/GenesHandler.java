package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APGene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
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
        } catch (IOException e) {
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

    public List<String> serchGenes(String text){
          List<APGene> collectedGenes =  geneList.entrySet().stream().
                 filter(x->x.getKey().contains(text))
                  .map(map->map.getValue())
                  .collect(Collectors.toList());

          return collectedGenes.stream().map(g->g.getApprovedSymbol()).collect(Collectors.toList());


    }
    public List<APGene> searchGenes(String text){
          List<APGene> collectedGenes =  geneList.entrySet().stream().
                 filter(x->x.getKey().contains(text))
                  .map(map->map.getValue())
                  .collect(Collectors.toList());

          return collectedGenes;


    }

    /***
     * For the search query, Genes are treated as generic filters.
     * Only IDs are passed. This function will collect all genes passed as IDs in list of String.
     * @param IDs
     * @return
     */
    public List<APGene> geteGenesWithIDs(List<String> IDs)
    {
        return geneList.values().stream().filter( g->  IDs.contains(String.valueOf(g.getHGNCID()))).collect(Collectors.toList());
    }





}
