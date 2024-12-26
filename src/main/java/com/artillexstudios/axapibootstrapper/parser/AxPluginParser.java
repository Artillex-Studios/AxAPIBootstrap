package com.artillexstudios.axapibootstrapper.parser;

import com.artillexstudios.axapibootstrapper.AxAPIBootstrapper;
import com.artillexstudios.axapibootstrapper.exception.InvalidAxPluginException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.reader.UnicodeReader;
import revxrsal.zapper.Dependency;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;
import revxrsal.zapper.util.ClassLoaderReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AxPluginParser {
    private static final Logger logger = LoggerFactory.getLogger(AxPluginParser.class);
    private static final List<Repository> repositories = new ArrayList<>();
    private static final List<Dependency> dependencies = new ArrayList<>();
    private static final List<Relocation> relocations = new ArrayList<>();
    private static String mainClass;
    private static String axAPIVersion;
    private static String axAPIRelocation;
    private static String librariesFolder;

    public static void loadDependencies() {
        File folder = new File(ClassLoaderReader.getDataFolder(AxAPIBootstrapper.class), librariesFolder);
        DependencyManager manager = new DependencyManager(folder, URLClassLoaderWrapper.wrap((URLClassLoader) AxAPIBootstrapper.class.getClassLoader()));
        manager.repository(Repository.mavenCentral());
        manager.repository(Repository.maven("https://repo.artillex-studios.com/releases/"));
        manager.dependency(new Dependency("com{}artillexstudios{}axapi".replace("{}", "."), "axapi", axAPIVersion));
        manager.relocate(new Relocation("com{}artillexstudios{}axapi".replace("{}", "."), axAPIRelocation));

        repositories.forEach(manager::repository);
        dependencies.forEach(manager::dependency);
        relocations.forEach(manager::relocate);
        manager.load();
    }

    public static void parse() {
        InputStream stream = ClassLoaderReader.getResource("axplugin.yml");
        PluginDescriptionFile description = ClassLoaderReader.getDescription(AxAPIBootstrapper.class);
        if (stream == null) {
            logger.error("Failed to load plugin {} due to no axplugin.yml being present! How could you forget this?", description.getName());
            throw new InvalidAxPluginException();
        }

        YamlConfiguration configuration = new YamlConfiguration();
        try (UnicodeReader reader = new UnicodeReader(stream)) {
            configuration.load(reader);
        } catch (IOException | InvalidConfigurationException exception) {
            logger.error("Unable to load AxPlugin due to exception while parsing axplugin.yml!", exception);
            throw new InvalidAxPluginException();
        }

        mainClass = configuration.getString("main-class");
        if (mainClass == null) {
            logger.error("Unable to load AxPlugin due to main-class not being present in axplugin.yml!");
            throw new InvalidAxPluginException();
        }

        axAPIVersion = configuration.getString("axapi-version");
        if (axAPIVersion == null) {
            logger.error("Unable to load AxPlugin due to axapi-version not being present in axplugin.yml!");
            throw new InvalidAxPluginException();
        }

        axAPIRelocation = configuration.getString("axapi-relocation");
        if (axAPIRelocation == null) {
            logger.error("Unable to load AxPlugin due to axapi-relocation not being present in axplugin.yml!");
            throw new InvalidAxPluginException();
        }

        librariesFolder = configuration.getString("libraries-folder", "libraries");

        for (String repository : configuration.getStringList("repositories")) {
            repositories.add(Repository.maven(repository));
        }

        for (String dependency : configuration.getStringList("dependencies")) {
            String[] split = dependency.split(":");
            dependencies.add(new Dependency(split[0], split[1], split[2]));
        }

        for (Map<?, ?> relocation : configuration.getMapList("relocations")) {
            Map<String, String> castMap = (Map<String, String>) relocation;
            if (!castMap.containsKey("from")) {
                logger.warn("No from found in relocation!");
                continue;
            }

            if (!castMap.containsKey("to")) {
                logger.warn("No to found in relocation!");
                continue;
            }

            relocations.add(new Relocation(castMap.get("from"), castMap.get("to")));
        }
    }

    public static String mainClass() {
        return mainClass;
    }

    public static String axAPIRelocation() {
        return axAPIRelocation;
    }

    public static String axAPIVersion() {
        return axAPIVersion;
    }
}
