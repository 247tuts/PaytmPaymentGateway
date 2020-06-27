package com.paytm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * here we are implement the swagger for testing our api's
 * references link : https://swagger.io/docs/specification/2-0/what-is-swagger/
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.paytm.controller"))
				.paths(PathSelectors.any())
				.build()
				.apiInfo(metaData());
				
	}
	
	 private ApiInfo metaData() {
		 return new ApiInfoBuilder()
	                .title("Paytm Payment Gateway Integration ")
	                .description("API Documentation For Paytm Payment Integration")
	                .termsOfServiceUrl("")
	                .contact("")
	                .license("Payment Gateway Integration 1.0")
	                .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
	                .version("2.0")
	                .build();
	    }
	 
	 	
}
