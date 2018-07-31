package au.org.garvan.kccg.annotations.pipeline.web;

import au.org.garvan.kccg.annotations.pipeline.engine.managers.ArticleManager;
import au.org.garvan.kccg.annotations.pipeline.model.annotation.OnDemandText;
import au.org.garvan.kccg.annotations.pipeline.model.annotation.RawArticle;
import au.org.garvan.kccg.annotations.pipeline.model.query.AnnotatedTextDocument;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Created by ahmed on 27/11/17.
 */
@RestController
public class OnDemandAnnotationController {

    @Autowired
    private ArticleManager engine;

    @ApiOperation(value = "postArticles", nickname = "postArticles")
    @RequestMapping(value = "/annotatedtext", method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = AnnotatedTextDocument.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    public AnnotatedTextDocument annotateText(@ApiParam("onDemandText") @RequestBody OnDemandText onDemandText) {
        return engine.processOnDemandText(onDemandText);
    }


    


}


