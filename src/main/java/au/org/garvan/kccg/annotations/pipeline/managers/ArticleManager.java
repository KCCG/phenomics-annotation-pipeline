package au.org.garvan.kccg.annotations.pipeline.managers;

import au.org.garvan.kccg.annotations.pipeline.entities.publicational.Article;
import au.org.garvan.kccg.annotations.pipeline.profiles.ProcessingProfile;

import java.util.List;

/**
 * Created by ahmed on 22/11/17.
 */
public class ArticleManager {



    public void init(){

    }

    public void processArticles(List<Article> articleList)
    {
        for (Article article: articleList){
            if(!isDuplicate(article)){


            }
            else{
                //TODO: Log and exit
            }


        }

    }


    private boolean isDuplicate(Article article){
        return false;
    }



}
