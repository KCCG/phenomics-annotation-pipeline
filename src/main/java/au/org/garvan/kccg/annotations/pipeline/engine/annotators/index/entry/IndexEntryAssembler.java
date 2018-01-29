package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.ParsedObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.remote.RemoteIndexTerm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tudor on 18/01/17.
 */
public class IndexEntryAssembler {

    public static IndexEntry fromParsedObject(ParsedObject parsedObject) {
        IndexEntry indexEntry = new IndexEntry(parsedObject.getUri(),
                parsedObject.getPreferredLabel(),
                parsedObject.getType());
        List<String> synonyms = new ArrayList<>();
        for (String key : parsedObject.getSynonyms().keySet()) {
            synonyms.add(key);
        }
        indexEntry.setSynonyms(synonyms);
        indexEntry.setStopWords(parsedObject.getStopWords());
        indexEntry.setProcessExact(parsedObject.isProcessExact());
        indexEntry.setExpansionWords(parsedObject.getExpansionWords());
        return indexEntry;
    }

    public static IndexEntry fromRemoteTermDto(RemoteIndexTerm remoteIndexTerm) {
        IndexEntry indexEntry = new IndexEntry(remoteIndexTerm.getUri(),
                remoteIndexTerm.getLabel(),
                remoteIndexTerm.getType());
        indexEntry.setSynonyms(remoteIndexTerm.getSynonyms());
        indexEntry.setStopWords(remoteIndexTerm.getProcessingMetadata().getStopWords());
        indexEntry.setProcessExact(remoteIndexTerm.getProcessingMetadata().getProcessExact().booleanValue());
        indexEntry.setExpansionWords(remoteIndexTerm.getProcessingMetadata().getExpansionWords());
        return indexEntry;
    }
}
