package au.org.garvan.kccg.annotations.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Created by ahmed on 27/11/17.
 */
@SpringBootApplication
@EnableSwagger2
@EnableAsync
public class AnnotationApplication {


    public static void main(String[] args) {
        SpringApplication.run(AnnotationApplication.class, args);
    }


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(regex("/articles|/query"))
                .build();
    }

    @Bean
    UiConfiguration uiConfig() {
        return new UiConfiguration("validatorUrl");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Phenomics Annotation Pipeline")
                .description("This service is part of concept mining framework. Annotation controller is for internal document processing. Query controller can be use to search the database.")
                .version("Beta 1.0")
                .build();
    }

}
