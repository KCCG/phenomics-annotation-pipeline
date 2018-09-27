package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.GeoDataConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.geodata.LinkedGeoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class GeoLinkManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(GeoLinkManager.class);

    GeoDataConnector geoDataConnector;

    public void init(){
        geoDataConnector = new GeoDataConnector();
        slf4jLogger.info(String.format("GeoLinkManager Manager init() called."));
    }


    //TODO: process articles ID and return GEO linked data
    public Map<String, List<LinkedGeoData>> processArticleIds(String queryId, List<String> pubmedIDs)
    {
        Map<String, List<LinkedGeoData>> mappedPubmedResults = new HashMap<>();
        slf4jLogger.info(String.format("Geo Link request received for query id:%s", queryId));

        List<Long> linkedIds = geoDataConnector.getLinkedIds(pubmedIDs);
        if (linkedIds !=null && linkedIds.size()>0)
        {
            List<LinkedGeoData> finalData =  geoDataConnector.getLinkedData(linkedIds);
            if (finalData.size()>0){
                for (String pubmedId: pubmedIDs){
                    List<LinkedGeoData> subLinkedData = finalData.stream().filter(x->x.getPubmedIds().contains(pubmedId)).collect(Collectors.toList());
                    mappedPubmedResults.put(pubmedId,subLinkedData);
                }

            }

        }


        return mappedPubmedResults;

    }

}
