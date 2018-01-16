package au.org.garvan.kccg.annotations.pipeline.web;

import au.org.garvan.kccg.annotations.pipeline.engine.managers.QueryManager;
import au.org.garvan.kccg.annotations.pipeline.model.PaginatedSearchResult;
import au.org.garvan.kccg.annotations.pipeline.model.SearchQuery;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by ahmed on 28/11/17.
 */

@RestController
public class QueryController {

    @Autowired
    private QueryManager engine;

    @ApiOperation(value = "searchArticles", nickname = "searchArticles", notes = "All attributes are optional; when more than one is provided, then search result will satisfy all conditions (Operation AND)")
    @RequestMapping(value = "/query", method = RequestMethod.POST, produces = "application/json")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = PaginatedSearchResult.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public PaginatedSearchResult searchArticles(@ApiParam("query") @RequestBody SearchQuery query,
                                                @RequestParam(value = "pageSize", required = false) @ApiParam Integer pageSize,
                                                @RequestParam(value = "pageNo", required = false) @ApiParam Integer pageNo
    ) {


        return engine.processQuery(query, pageSize, pageNo);
    }


    @ApiOperation(value = "getAutocomplete", nickname = "getAutocomplete", notes = "")
    @RequestMapping(value = "/query/autocomplete/{infix}", method = RequestMethod.GET, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public List<String> getAutocomplete(@PathVariable(value = "infix") @ApiParam("infix") String infix) {

        return engine.getAutocompleteList(infix.toUpperCase().trim());

    }


}
