package javax0.jamal.webeditor;

import de.f0rce.ace.enums.AceTheme;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Configuration {

    public static Configuration INSTANCE;

    public final boolean AUTO_COMPLETE;
    public final AceTheme THEME;

    public final Properties properties = new Properties();

    public final Pattern CLIENT;

    public Configuration(final File rootDir) {
        INSTANCE = this;
        final var configFile = new File(rootDir, "jamal-editor.properties");
        try {
            properties.load(configFile.toURI().toURL().openStream());
        } catch (IOException ignore) {
            // properties just remain empty
        }
        if (properties.contains("theme")) {
            THEME = AceTheme.valueOf(properties.getProperty("theme"));
        } else {
            THEME = AceTheme.eclipse;
        }
        if (properties.contains("autoComplete")) {
            AUTO_COMPLETE = Boolean.parseBoolean(properties.getProperty("autoComplete"));
        } else {
            AUTO_COMPLETE = true;
        }

        if (properties.contains("client")) {
            CLIENT = Pattern.compile(properties.getProperty("client"));
        } else {
            CLIENT = null;
        }

    }

}
