package au.org.garvan.kccg.annotations.pipeline.web;

import au.org.garvan.kccg.annotations.pipeline.engine.managers.SubscriptionManager;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.Pair;
import au.org.garvan.kccg.annotations.pipeline.model.SubscriptionQuery;
import au.org.garvan.kccg.annotations.pipeline.model.SubscriptionResult;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ahmed on 28/11/17.
 */

@RestController
public class SubscriptionController {

    @Autowired
    private SubscriptionManager engine;

    @ApiOperation(value = "subscribeQuery", nickname = "subscribeQuery" , notes = "")
    @RequestMapping(value = "/subscription", method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseEntity.class ),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public ResponseEntity<?> subscribeQuery(@ApiParam("subscriptionQuery") @RequestBody SubscriptionQuery subscriptionQuery) {

        Pair<Boolean,Object> result =  engine.processSubscription(subscriptionQuery);
        if(result.getFirst()){
            SubscriptionResult returnObject =   new SubscriptionResult(result.getSecond().toString(),subscriptionQuery.getSubscriptionId());
            return new ResponseEntity<>(returnObject, HttpStatus.OK);

        }
        else{
            return new ResponseEntity<>(new CustomErrorType(result.getSecond().toString()),
                    HttpStatus.BAD_REQUEST);

        }

    }

    @ApiOperation(value = "getSubscription", nickname = "getSubscription" , notes = "")
    @RequestMapping(value = "/subscription/{subscriptionId}", method = RequestMethod.GET, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseEntity.class ),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public ResponseEntity<?> getSubscription(@PathVariable(value="subscriptionId") @ApiParam("subscriptionId") String subscriptionId) {

        Pair<Boolean, Object> result = engine.getSubscription(subscriptionId);
        if (result.getFirst()) {
            return new ResponseEntity<>(result.getSecond(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new CustomErrorType(result.getSecond().toString()),
                    HttpStatus.NO_CONTENT);
        }
    }


    @ApiOperation(value = "getSubscriptions", nickname = "getSubscriptions" , notes = "")
    @RequestMapping(value = "/subscription/", method = RequestMethod.GET, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseEntity.class ),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public ResponseEntity<?> getSubscriptions() {

        Pair<Boolean, Object> result = engine.getSubscriptions();
        if (result.getFirst()) {
            return new ResponseEntity<>(result.getSecond(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new CustomErrorType(result.getSecond().toString()),
                    HttpStatus.NO_CONTENT);
        }
    }

    @ApiOperation(value = "deleteSubscription", nickname = "deleteSubscription" , notes = "")
    @RequestMapping(value = "/subscription/{subscriptionId}", method = RequestMethod.DELETE, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseEntity.class ),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public ResponseEntity<?> deleteSubscription(@PathVariable(value="subscriptionId") @ApiParam("subscriptionId") String subscriptionId) {

        Pair<Boolean, Object> result = engine.deleteSubscription(subscriptionId);
        if (result.getFirst()) {
            return new ResponseEntity<>(HttpStatus.OK);

        } else {
            return new ResponseEntity<>(new CustomErrorType(result.getSecond().toString()),
                    HttpStatus.NO_CONTENT);
        }
    }




    @ApiOperation(value = "updateSubscription", nickname = "updateSubscription" , notes = "")
    @RequestMapping(value = "/subscription/{subscriptionId}/{runDate}/{timeStamp}", method = RequestMethod.PUT, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseEntity.class ),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    @CrossOrigin
    public ResponseEntity<?> updateSubscriptionTime(@PathVariable(value="subscriptionId") @ApiParam("subscriptionId") String subscriptionId,
                                                    @PathVariable(value="timeStamp") @ApiParam("timeStamp") String timeStamp,
                                                    @PathVariable(value="runDate") @ApiParam("runDate") long runDate) {

        Pair<Boolean, Object> result = engine.updateSubscriptionTime(subscriptionId,runDate, timeStamp);
        if (result.getFirst()) {
            return new ResponseEntity<>(HttpStatus.OK);

        } else {
            return new ResponseEntity<>(new CustomErrorType(result.getSecond().toString()),
                    HttpStatus.NO_CONTENT);
        }
    }



    public class CustomErrorType {

        private String errorMessage;

        public CustomErrorType(String errorMessage){
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

    }

}
