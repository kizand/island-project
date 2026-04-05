package com.javarush.island.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class Settings {

    private static final String SETTINGS_PATH = "kizand/settings.yaml";
    private static final Settings settings = load();
    private ApplicationConfig applicationConfig;
    private ConsoleConfig consoleConfig;
    private IslandConfig islandConfig;
    private GlobalOrganismConfig globalOrganismConfig;
    private Map<String, OrganismConfig> organisms;

    public static Settings get() {
        return settings;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public ConsoleConfig getConsoleConfig() {
        return consoleConfig;
    }

    public IslandConfig getIslandConfig() {
        return islandConfig;
    }

    public GlobalOrganismConfig getGlobalOrganismConfig() {
        return globalOrganismConfig;
    }

    public Map<String, OrganismConfig> getOrganisms() {
        return organisms;
    }

    private static Settings load() {
        URL resource = Settings.class.getClassLoader().getResource(SETTINGS_PATH);
        if (resource == null) {
            throw new RuntimeException("Could not locate " + SETTINGS_PATH);
        }
        ObjectMapper mapperYAML = new ObjectMapper(new YAMLFactory());
        try {
            Settings settings = mapperYAML.readValue(resource, Settings.class);
            return Objects.requireNonNull(settings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Settings() {}
}
