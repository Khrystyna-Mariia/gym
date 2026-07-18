package org.gymcrm;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.gymcrm.config.AppConfig;
import org.gymcrm.config.WebConfig;
import org.gymcrm.filter.AuthenticationContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.nio.file.Files;

import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            logger.info("Starting Embedded Tomcat server on port {}...", PORT);

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(PORT);

            File baseDir = Files.createTempDirectory("tomcat-base-dir").toFile();
            tomcat.setBaseDir(baseDir.getAbsolutePath());

            tomcat.getConnector();

            String docBase = new File(".").getAbsolutePath();
            Context context = tomcat.addContext("", docBase);

            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName("authenticationContextFilter");
            filterDef.setFilterClass(AuthenticationContextFilter.class.getName());
            context.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName("authenticationContextFilter");
            filterMap.addURLPattern("/*");
            context.addFilterMap(filterMap);

            context.setParentClassLoader(Thread.currentThread().getContextClassLoader());

            AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
            rootContext.register(AppConfig.class, WebConfig.class);

            DispatcherServlet dispatcherServlet = new DispatcherServlet(rootContext);
            var dispatcherWrapper = Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
            dispatcherWrapper.setLoadOnStartup(1);

            context.addServletMappingDecoded("/", "dispatcher");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Stopping Tomcat server...");
                    tomcat.stop();
                    tomcat.destroy();

                    deleteDirectory(baseDir);
                    logger.info("Tomcat server stopped successfully.");
                } catch (Exception e) {
                    logger.error("Error while stopping Tomcat", e);
                }
            }));

            tomcat.start();
            logger.info("Tomcat server successfully started!");
            logger.info("Swagger UI is available at: http://localhost:{}/swagger-ui/index.html", PORT);

            tomcat.getServer().await();

        } catch (Exception e) {
            logger.error("Failed to start Embedded Tomcat server", e);
        }
    }
}