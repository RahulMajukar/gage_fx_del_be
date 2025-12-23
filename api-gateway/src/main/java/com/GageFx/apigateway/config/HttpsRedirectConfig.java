//package com.GageFx.apigateway.config;
//
//import org.apache.catalina.connector.Connector;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class HttpsRedirectConfig {
//
//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
//        return factory -> factory.addAdditionalTomcatConnectors(createHttpConnector());
//    }
//
//    private Connector createHttpConnector() {
//        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL); // important!
//        connector.setScheme("http");
//        connector.setPort(8088);          // old HTTP port
//        connector.setSecure(false);
//        connector.setRedirectPort(8445);  // redirect to HTTPS
//        return connector;
//    }
//}
