package au.org.garvan.kccg.annotations.pipeline.web;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.lambda.WorkerLambdaConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.ArticleManager;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.AnnotationLog;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.EngineEnvironment;
import au.org.garvan.kccg.annotations.pipeline.model.annotation.RawArticle;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Created by ahmed on 27/11/17.
 */
@RestController
public class AdminController {

    @ApiOperation(value = "startWorker", nickname = "startWorker")
    @RequestMapping(value = "/worker", method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = boolean.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    public boolean startWorker() {
        WorkerLambdaConnector.invokeWorkerLambda(EngineEnvironment.getWorkerID(),true);
        return true;
    }


    @ApiOperation(value = "getAnnotationLog", nickname = "startWorker")
    @RequestMapping(value = "/annotationLogs", method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = boolean.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    public List<String> getAnnotationLog(@RequestParam(value = "startIndex", required = false) @ApiParam Integer startIndex,
                                         @RequestParam(value = "endIndex", required = false) @ApiParam Integer endIndex) {

        return AnnotationLog.getAnnotationLog(startIndex, endIndex);
    }


}
