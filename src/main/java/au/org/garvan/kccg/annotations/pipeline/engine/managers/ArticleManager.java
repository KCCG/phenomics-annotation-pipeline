package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.model.RawArticle;
import org.apache.tomcat.jni.Local;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
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

        slf4jLogger.info(String.format("Initializing Article Manager"));

        dbManager = new DatabaseManager();
    }


    @Async
    public void processArticles(List<RawArticle> articleList)
    {
        for (RawArticle input: articleList){

            Article article = constructArticle(input);
            try {
                if (!isDuplicate(article)) {
                    article.getArticleAbstract().hatch();
                } else {
                    //TODO: Log and exit
                }

                slf4jLogger.info(String.format("Article processed successfully, ID: %d", article.getPubMedID()));

            }
            catch (Exception e){
                slf4jLogger.error(String.format("Error in processing article with ID: %d", article.getPubMedID()));
            }
            try {
                dbManager.persistArticle(article);
                slf4jLogger.info(String.format("Article persisted successfully, ID: %d", article.getPubMedID()));

            }
            catch (Exception e){
                slf4jLogger.error(String.format("Error in persisting article with ID: %d", article.getPubMedID()));

            }


        }//Article Loop


    }

    @Async
    public void sampleAsyncMethod() {
        long time = System.currentTimeMillis();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // We've been interrupted
            System.out.println(String.format("Task interrupted after %d milliseconds", System.currentTimeMillis() - time));
            return;
        }

        System.out.println(String.format("Task completed after %d milliseconds", System.currentTimeMillis() - time));
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
