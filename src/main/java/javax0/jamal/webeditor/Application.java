package javax0.jamal.webeditor;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "jamal-editor")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        if (args.length > 0) {
            new Configuration(new File(args[0]));
        } else {
            new Configuration(new File("."));
        }
        SpringApplication.run(Application.class, args);
    }

}
