package au.org.garvan.kccg.annotations.pipeline.engine.utilities.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class EngineConfig {

    @Getter
    @Setter
    private static String affinityEndpoint;

}
