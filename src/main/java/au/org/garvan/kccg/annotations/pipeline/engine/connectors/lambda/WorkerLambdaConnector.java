package au.org.garvan.kccg.annotations.pipeline.engine.connectors.lambda;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkerLambdaConnector {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(WorkerLambdaConnector.class);

    private static String functionName;

    private static Regions region = Regions.fromName("ap-southeast-2");
    private static AWSLambdaAsyncClientBuilder builder = AWSLambdaAsyncClientBuilder.standard()
            .withRegion(region);

   private static AWSLambdaAsync client = builder.build();

    @Autowired
    public  WorkerLambdaConnector(@Value("${spring.lambdaconnector.workerfunctionname}") String workerFunctionName){
        functionName = workerFunctionName;
    }
    public static void invokeWorkerLambda(String workerID, Boolean isIgnitionCall) {
        JSONObject payload = new JSONObject();
        payload.put("workerID", workerID);
        payload.put("ignitionCall", isIgnitionCall);
        InvokeRequest request = new InvokeRequest();
        request.setInvocationType("Event");
        request.withFunctionName(functionName).withPayload(payload.toString());
        InvokeResult invoke = client.invoke(request);
        slf4jLogger.info(String.format("Invoking Lambda Function with worker ID:%s and ignition call:%s", workerID, isIgnitionCall.toString()));
    }
}
