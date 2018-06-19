package au.org.garvan.kccg.annotations.pipeline.engine.utilities;

import lombok.Data;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationLog {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(AnnotationLog.class);
    private static ConcurrentHashMap<String, LogEntry> logs = new ConcurrentHashMap<>();



    public static void  logAnnotation(String text, String id){

        LogEntry logEntry;
        if(logs.containsKey(text.toLowerCase())){
            logEntry = logs.get(text.toLowerCase());
        }
        else
        {
            logEntry = new LogEntry();
            logs.put(text.toLowerCase(), logEntry);
        }
        logEntry.count ++;
        logEntry.annotationIds.add(id);
    }

    public static void logAnnotations(JSONArray jsonArray, String abstractText)
    {

        for(Object obj: jsonArray){
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray annotations = (JSONArray) jsonObject.get("annotations");

            for(Object innerObject:annotations){
                JSONObject singleAnnotationJson = (JSONObject) innerObject;
                Pair<Integer, Integer> offset = constructOffset(singleAnnotationJson.get("globalOffset").toString());
                String id = singleAnnotationJson.get("annotationId").toString();
                String text = abstractText.substring(offset.getFirst(),offset.getSecond());
                logAnnotation(text, id);
            }

        }
    }

    private static Pair<Integer, Integer> constructOffset(String globalOffset){
        String[] offsets =  globalOffset.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(offsets[0]), Integer.parseInt(offsets[1]));
    }

    @Data
    private static class LogEntry{
        Integer count = 0;
        HashSet<String> annotationIds = new HashSet<>();



    }

    public static List<String> getAnnotationLog(Integer stIndex, Integer enIndex){

        Integer begin =  stIndex==null? 0: stIndex;
        Integer end =    enIndex==null? logs.size(): enIndex;

        List<String> returnList = new ArrayList<>();
        List<String> keys = new ArrayList<String> (logs.keySet());
        Collections.sort(keys, Comparator.comparing(String::length));


        for (String key: keys.subList(Math.min(begin,keys.size()), Math.min(end,keys.size() ))){
            LogEntry entry = logs.get(key);
            String item = String.format("Text:%s | Count:%d | Ids:%s", key, entry.count, String.join("-", entry.annotationIds));
            returnList.add(item);
        }
        return returnList;

    }

}
