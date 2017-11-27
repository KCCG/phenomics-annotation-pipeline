package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.model.RawArticle;
import org.apache.tomcat.jni.Local;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class ArticleManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(ArticleManager.class);
    DatabaseManager dbManager;

    public void init(){
         dbManager = new DatabaseManager();
    }

    public void processArticles(List<RawArticle> articleList)
    {
        for (RawArticle input: articleList){

            Article article = constructArticle(input);
            try {


                if (!isDuplicate(article)) {
                    article.getArticleAbstract().hatch();
                    dbManager.persistArticle(article);

                } else {
                    //TODO: Log and exit
                }
            }
            catch (Exception e){
                slf4jLogger.error(String.format("Error in processing article with ID: %d", article.getPubMedID()));

            }


        }

    }


    private boolean isDuplicate(Article article){
        return false;
    }


    private Article constructArticle(RawArticle rawArticle){
        Article article = new Article(rawArticle.getPubMedID(),
                LocalDate.parse(rawArticle.getDatePublished()),
                LocalDate.parse(rawArticle.getDateCreated()),
                LocalDate.parse(rawArticle.getDateRevised()),
                rawArticle.getArticleTitle(),
                new APDocument(rawArticle.getArticleAbstract()),
                rawArticle.getLanguage(),
                rawArticle.getAuthors(),
                rawArticle.getPublication());


        return article;
    }



}
