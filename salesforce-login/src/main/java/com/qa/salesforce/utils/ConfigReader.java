package com.qa.salesforce.utils;

import com.qa.salesforce.exceptions.FrameworkException;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Single source of configuration. Resolution order per key:
 * <ol>
 *     <li>JVM system property (e.g. {@code -DSF_URL=...})</li>
 *     <li>OS environment variable</li>
 *     <li>{@code .env} file in the project root (via dotenv-java)</li>
 *     <li>the supplied default when a key is absent</li>
 * </ol>
 */
public final class ConfigReader {

    public static final String SF_URL = "SF_URL";
    public static final String HEADLESS = "HEADLESS";
    public static final String EXPLICIT_WAIT = "EXPLICIT_WAIT";

    private static final String DEFAULT_URL = "https://login.salesforce.com/?locale=in";
    private static final String DEFAULT_HEADLESS = "false";
    private static final int DEFAULT_EXPLICIT_WAIT = 20;

    private static final ConfigReader INSTANCE = new ConfigReader();

    private final Dotenv dotenv;

    private ConfigReader() {
        this.dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }

    public static ConfigReader getInstance() {
        return INSTANCE;
    }

    /** Returns the value for {@code key}, or {@code null} when unset at every source. */
    public String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key);
        }
        if (isBlank(value)) {
            value = dotenv.get(key);
        }
        return isBlank(value) ? null : value.trim();
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    public String getSalesforceUrl() {
        return get(SF_URL, DEFAULT_URL);
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(get(HEADLESS, DEFAULT_HEADLESS));
    }

    public int getExplicitWait() {
        String raw = get(EXPLICIT_WAIT);
        if (raw == null) {
            return DEFAULT_EXPLICIT_WAIT;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new FrameworkException("Configuration '" + EXPLICIT_WAIT
                    + "' must be an integer but was '" + raw + "'", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
