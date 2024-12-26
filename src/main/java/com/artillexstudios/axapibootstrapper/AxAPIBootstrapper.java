package com.artillexstudios.axapibootstrapper;

import com.artillexstudios.axapibootstrapper.parser.AxPluginParser;
import com.artillexstudios.axapibootstrapper.wrapper.AxPluginWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxAPIBootstrapper extends JavaPlugin {
    private static final AxPluginWrapper wrapper;

    static {
        AxPluginParser.parse();
        AxPluginParser.loadDependencies();
        wrapper = AxPluginWrapper.forClass(AxPluginParser.mainClass(), AxPluginParser.axAPIRelocation() + ".AxPlugin");
    }

    public AxAPIBootstrapper() {
        wrapper.createInstance(this);
    }

    @Override
    public void onLoad() {
        wrapper.onLoad();
    }

    @Override
    public void onEnable() {
        wrapper.onEnable();
    }

    @Override
    public void onDisable() {
        wrapper.onDisable();
    }
}