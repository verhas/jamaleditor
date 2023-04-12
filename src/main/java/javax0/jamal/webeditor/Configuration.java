package javax0.jamal.webeditor;

import de.f0rce.ace.enums.AceTheme;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Configuration {

    // snipline JAMAL_EDITOR_PROPERTIES filter="(.*)"
    public static final String JAMAL_EDITOR_PROPERTIES = "jamal-editor.properties";
    // snipline AUTO_COMPLETE_KEY filter="(.*)"
    public static final String AUTO_COMPLETE_KEY = "autoComplete";
    /**
     * snippet CONFIG001
     * * `{%@snip AUTO_COMPLETE_KEY%}`
     * +
     * can be used to configure the auto-completion.
     * Auto-completion is enabled only for Jamal files by default, and it focuses on Jamal commands.
     * The completion will suggest the built-in and user defined macros that are available at the point of editing.
     *
     * In some cases the autocompletion may present too much computation on the application.
     * In this case the auto-completion can be disabled by setting the value of this key to `false`.
     * end snippet
     */
    // snipline THEME_KEY filter="(.*)"
    public static final String THEME_KEY = "theme";
    /**
     * snippet CONFIG002
     * * `{%@snip THEME_KEY%}`
     * +
     * can be used to configure the theme of the editor.
     * This is how the editor starts up.
     * It can also be changed interactively by the user.
     * Changing the themes on the user interface does not change the configuration file.
     * <p>
     * end snippet
     */
    // snipline CLIENT_KEY filter="(.*)"
    public static final String CLIENT_KEY = "client";
    /**
     * snippet CONFIG003
     * * `{%@snip CLIENT_KEY%}`
     * +
     * can be used to configure a regular expression that is used to identify the client.
     * If this configuration key is missing only the local host can be used as a client.
     * If this key is defined it is compared against the client IP address as a string.
     * <p>
     * end snippet
     */
    public static Configuration INSTANCE;

    public final boolean AUTO_COMPLETE;
    public final AceTheme THEME;

    public final Properties properties = new Properties();

    public final Pattern CLIENT;

    public Configuration(final File rootDir) {
        INSTANCE = this;
        final var configFile = new File(rootDir, JAMAL_EDITOR_PROPERTIES);
        try {
            properties.load(configFile.toURI().toURL().openStream());
        } catch (IOException ignore) {
            // properties just remain empty
        }
        if (properties.containsKey(THEME_KEY)) {
            THEME = AceTheme.valueOf(properties.getProperty(THEME_KEY));
        } else {
            THEME = AceTheme.eclipse;
        }
        if (properties.contains(AUTO_COMPLETE_KEY)) {
            AUTO_COMPLETE = Boolean.parseBoolean(properties.getProperty(AUTO_COMPLETE_KEY));
        } else {
            AUTO_COMPLETE = true;
        }

        if (properties.contains(CLIENT_KEY)) {
            CLIENT = Pattern.compile(properties.getProperty(CLIENT_KEY));
        } else {
            CLIENT = null;
        }

    }

}
