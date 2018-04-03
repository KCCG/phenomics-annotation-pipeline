package au.org.garvan.kccg.annotations.pipeline;

import au.org.garvan.kccg.annotations.pipeline.engine.connectors.lambda.WorkerLambdaConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.ArticleManager;
import au.org.garvan.kccg.annotations.pipeline.engine.managers.QueryManager;
import au.org.garvan.kccg.annotations.pipeline.engine.utilities.EngineEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Created by ahmed on 27/11/17.
 */
@Component
public class ApplicationInitializer {
    protected static final Logger log = LoggerFactory.getLogger(ApplicationInitializer.class);

    @Autowired
    private ArticleManager articleManager;

    @Autowired
    private QueryManager queryManager;


    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        log.info("Initializing Application");

        try {
            articleManager.init();
            log.info("Initialized Processing Engine successfully");

        } catch(RuntimeException ex) {
            log.error("Failed to initialize Processing engine.", ex);
            System.exit(1);
        }


        try {
            queryManager.init();
            log.info("Initialized Query Engine successfully");

        } catch(RuntimeException ex) {
            log.error("Failed to initialize Query engine.", ex);
            System.exit(1);
        }

        if(EngineEnvironment.getSelfIngestionEnabled())
        {
            log.info("Self Ingestion is enabled.Invoking Lambda ");
            WorkerLambdaConnector.invokeWorkerLambda(EngineEnvironment.getWorkerID(), true);
        }
        else{
            log.info("Self Ingestion is disabled. ");
        }

    }
}

