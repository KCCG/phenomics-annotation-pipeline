package au.org.garvan.kccg.annotations.pipeline.engine.utilities.config;


import com.google.common.base.Strings;
import org.mapdb.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ConfigLoader {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(ConfigLoader.class);



    @Autowired
    public  ConfigLoader(){

    }

    @Value("${spring.affinityconnector.endpoint}") String affinityEndPoint;



    @PostConstruct
    public void init(){

        if (!Strings.isNullOrEmpty(affinityEndPoint)) {
            EngineConfig.setAffinityEndpoint(affinityEndPoint);

        } else {
            EngineConfig.setAffinityEndpoint("http://localhost:9020");
        }


    }


}
