package au.org.garvan.kccg.annotations.pipeline.connectors;

import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.enums.CommonParams;
import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APDocument;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ahmed on 25/7/17.
 */

public class JsonConnector implements BaseConnector {
    protected final Logger log = LoggerFactory.getLogger(JsonConnector.class);
    @Override
    public List<APDocument> getDocuments(String fileName, CommonParams type) {
        if (type.equals(CommonParams.FILENAME)) {
            JSONParser parser = new JSONParser();
            List<APDocument> collectedDocs = new ArrayList<>();
            try {

                File file = new File(getClass().getClassLoader().getResource("docs/" + fileName).getFile());
                Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
                JSONArray jsonDocs = (JSONArray) obj;

                jsonDocs.forEach(x -> collectedDocs.add(new APDocument(Integer.parseInt(((JSONObject) x).get("PMID").toString()), ((JSONObject) x).get("articleAbstract").toString())));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return collectedDocs;
        }
        else
        {
            log.warn(String.format("Invalid param type:%s for this connector class:", type.toString(), JsonConnector.class.toString()));
            return new ArrayList<>();
        }
    }





    @Override
    public List<APDocument> getDocuments(LocalDate date) throws IOException {
        return null;
    }

    @Override
    public APDocument getDocument(String PMID) {
        return null;
    }


    @Override
    public List<Article> getArticles(String fileName, CommonParams type) {
        if (type.equals(CommonParams.FILENAME)) {
            JSONParser parser = new JSONParser();
            List<Article> collectedArticles = new ArrayList<>();
            try {

                File file = new File(getClass().getClassLoader().getResource("docs/" + fileName).getFile());
                Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
                JSONArray jsonDocs = (JSONArray) obj;


                for (Object jd:jsonDocs ){
                    JSONObject jsonDoc = (JSONObject) jd;
                    Article article = new Article(jsonDoc);
                    collectedArticles.add(article);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return collectedArticles;
        }
        else
        {
            log.warn(String.format("Invalid param type:%s for this connector class:", type.toString(), JsonConnector.class.toString()));
            return new ArrayList<>();
        }
    }
}
