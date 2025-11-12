package com.example.backend;

import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.awt.*;
import java.io.File;
import java.util.Properties;

@SpringBootApplication
public class BackendApplication {

    private static final String JAVA_GLOBAL_EJB_MODULE_BENEFICIO_EJB_SERVICE = "java:global/ejb-module/BeneficioEjbService";
    private static Container tomee;

    public static void main(String[] args) {
//        startTomEE();
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Inicializa TomEE Embedded APENAS para fornecer container EJB/JNDI.
     * Não expõe HTTP - o Spring Boot Tomcat já cuida disso na porta 8080.
     */
//    private static void startTomEE() {
//        try {
//            if (tomee != null) return;
//
//            // Configuração do DataSource JNDI
//            Properties resources = new Properties();
//            resources.setProperty("jdbc/bip", "new://Resource?type=DataSource");
//            resources.setProperty("jdbc/bip.JtaManaged", "true");
//            resources.setProperty("jdbc/bip.JdbcDriver", "org.postgresql.Driver");
//            resources.setProperty("jdbc/bip.JdbcUrl", "jdbc:postgresql://localhost:5432/bip");
//            resources.setProperty("jdbc/bip.UserName", "bip");
//            resources.setProperty("jdbc/bip.Password", "bip");
//            resources.setProperty("jdbc/bip.MaxActive", "20");
//            resources.setProperty("jdbc/bip.MaxIdle", "5");
//
//            Configuration cfg = new Configuration();
//            cfg.setQuickSession(true);
//            cfg.setDir("target/tomee");
//            cfg.setProperties(resources);
//
//            // Desabilita HTTP completamente - apenas container EJB/JNDI
////            cfg.setHttpPort(-1);  // -1 desabilita o servidor HTTP
////            cfg.setStopPort(-1);
//
//            tomee = new Container(cfg);
//
//            tomee.start();
//
//            // Configura InitialContextFactory para lookup de EJB
//            System.setProperty("java.naming.factory.initial",
//                "org.apache.openejb.client.LocalInitialContextFactory");
//
//            System.out.println("✓ TomEE Embedded iniciado (somente container EJB/JNDI)");
//
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                try {
//                    if (tomee != null) {
//                        tomee.stop();
//                        tomee = null;
//                    }
//                } catch (Exception ignored) {
//                }
//            }));
//
//        } catch (Exception e) {
//            throw new IllegalStateException("Falha ao iniciar TomEE embedded", e);
//        }
//    }
//
//    /**
//     * Expõe o EJB como bean do Spring via JNDI lookup.
//     * Permite injeção via @Autowired nos controllers.
//     */
//    @Bean
//    public com.example.ejb.BeneficioEjbService beneficioEjbService() {
//        try {
//            Properties env = new Properties();
//            env.setProperty("java.naming.factory.initial",
//                "org.apache.openejb.client.LocalInitialContextFactory");
//
//            JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
//            factory.setJndiEnvironment(env);
//            factory.setExpectedType(com.example.ejb.BeneficioEjbService.class);
//            factory.setLookupOnStartup(true);
//            factory.setCache(true);
//
//            // Tenta primeiro o nome simples
//            try {
//                factory.setJndiName("java:global/BeneficioEjbService");
//                factory.afterPropertiesSet();
//                System.out.println("✓ EJB encontrado: java:global/BeneficioEjbService");
//                return (com.example.ejb.BeneficioEjbService) factory.getObject();
//            } catch (Exception e) {
//                // Fallback: tenta com nome do módulo
//                factory.setJndiName("java:global/ejb-module/BeneficioEjbService");
//                factory.afterPropertiesSet();
//                System.out.println("✓ EJB encontrado: java:global/ejb-module/BeneficioEjbService");
//                return (com.example.ejb.BeneficioEjbService) factory.getObject();
//            }
//        } catch (Exception e) {
//            throw new IllegalStateException("Falha ao obter EJB via JNDI", e);
//        }
//    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}