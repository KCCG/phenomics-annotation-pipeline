package au.org.garvan.kccg.annotations.pipeline.web;

import au.org.garvan.kccg.annotations.pipeline.engine.managers.ArticleManager;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.QueryManager;
import au.org.garvan.kccg.annotations.pipeline.model.RawArticle;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import au.org.garvan.kccg.annotations.pipeline.model.SearchResult;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by ahmed on 28/11/17.
 */

@RestController
public class QueryController {

    @Autowired
    private QueryManager engine;

    @ApiOperation(value = "searchArticles", nickname = "searchArticles" , notes = "All attributes are optional; when more than one is provided, then search result will satisfy all conditions (Operation AND)")
    @RequestMapping(value = "/query", method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = SearchResult.class , responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})

    public List<SearchResult> searchArticles(@ApiParam("query") @RequestBody SearchQuery query) {
        return engine.processQuery(query);
    }

}
