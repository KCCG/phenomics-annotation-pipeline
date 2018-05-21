package au.org.garvan.kccg.annotations.pipeline.web;

import au.org.garvan.kccg.annotations.pipeline.engine.managers.FeedbackManager;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.Feedback;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ahmed on 28/11/17.
 */

@RestController
public class FeedbackController {

    @Autowired
    private FeedbackManager engine;

    @ApiOperation(value = "annotationFeedback", nickname = "annotationFeedback")
    @RequestMapping(value = "/feedback", method = RequestMethod.POST, produces = "application/json")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseEntity.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public ResponseEntity sendFeedback(@ApiParam("feedback") @RequestBody Feedback feedback, HttpServletRequest request) {

        //TODO: Call appropriate manager and fit it in
        engine.processFeedback(feedback,request.getRemoteAddr().toString());
        return new ResponseEntity(HttpStatus.OK);
    }



}
