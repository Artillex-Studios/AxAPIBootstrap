package com.artillexstudios.axapibootstrapper.wrapper;

import com.artillexstudios.axapibootstrapper.exception.InvalidAxPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class AxPluginWrapper {
    private static final Logger logger = LoggerFactory.getLogger(AxPluginWrapper.class);
    private final Constructor<?> constructor;
    private final MethodHandle onLoad;
    private final MethodHandle onEnable;
    private final MethodHandle onDisable;
    private Object instance;

    public AxPluginWrapper(Constructor<?> constructor, MethodHandle onLoad, MethodHandle onEnable, MethodHandle onDisable) {
        this.constructor = constructor;
        this.onLoad = onLoad;
        this.onEnable = onEnable;
        this.onDisable = onDisable;
    }

    public Object createInstance(JavaPlugin plugin) {
        if (this.instance != null) {
            throw new IllegalStateException("Attempted to create same plugin instance twice!");
        }

        try {
            this.instance = constructor.newInstance(plugin);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            logger.error("Failed to create instance of the plugin!");
            throw new InvalidAxPluginException();
        }
        return this.instance;
    }

    public void onLoad() {
        try {
            this.onLoad.invoke();
        } catch (Throwable exception) {
            logger.error("Failed to call onLoad!", exception);
        }
    }

    public void onEnable() {
        try {
            this.onEnable.invoke();
        } catch (Throwable exception) {
            logger.error("Failed to call onEnable!", exception);
        }
    }

    public void onDisable() {
        try {
            this.onDisable.invoke();
        } catch (Throwable exception) {
            logger.error("Failed to call onDisable!", exception);
        }
    }

    public static AxPluginWrapper forClass(String className, String relocatedAxPluginClass) {
        try {
            Class<?> axPluginClass = Class.forName(relocatedAxPluginClass);
            Constructor<?> constructor = Class.forName(className).getDeclaredConstructor(JavaPlugin.class);
            MethodHandle onLoad = MethodHandles.publicLookup().unreflect(axPluginClass.getDeclaredMethod("onLoad"));
            MethodHandle onEnable = MethodHandles.publicLookup().unreflect(axPluginClass.getDeclaredMethod("onEnable"));
            MethodHandle onDisable = MethodHandles.publicLookup().unreflect(axPluginClass.getDeclaredMethod("onDisable"));
            return new AxPluginWrapper(constructor, onLoad, onEnable, onDisable);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException exception) {
            logger.error("Failed to find constructor with JavaPlugin parameter for class {}!", className);
            throw new InvalidAxPluginException();
        }
    }
}
