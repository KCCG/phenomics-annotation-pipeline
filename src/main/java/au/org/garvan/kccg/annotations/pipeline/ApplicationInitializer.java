package au.org.garvan.kccg.annotations.pipeline;

import au.org.garvan.kccg.annotations.pipeline.engine.managers.ArticleManager;
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
    ArticleManager articleManager;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        log.info("Initializing Article Manager Engine");

        try {
            log.info("Initialized Engine successfully");

        } catch(RuntimeException ex) {
            log.error("Failed to initialize engine.", ex);
            System.exit(1);
        }
    }
}

