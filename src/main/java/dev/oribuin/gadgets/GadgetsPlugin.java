package dev.oribuin.gadgets;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.command.impl.GiveCommand;
import dev.oribuin.gadgets.config.ConfigLoader;
import dev.oribuin.gadgets.config.impl.Config;
import dev.oribuin.gadgets.config.impl.MySQLConfig;
import dev.oribuin.gadgets.config.impl.PluginMessages;
import dev.oribuin.gadgets.gadgets.GadgetFactory;
import dev.oribuin.gadgets.gui.impl.manual.HologramProjectorGUI;
import dev.oribuin.gadgets.hook.HeadDbProvider;
import dev.oribuin.gadgets.listener.CustomBlockListener;
import dev.oribuin.gadgets.listener.ItemListener;
import dev.oribuin.gadgets.listener.NexoListener;
import dev.oribuin.gadgets.listener.QuickShopListener;
import dev.oribuin.gadgets.manager.CommandManager;
import dev.oribuin.gadgets.manager.DataManager;
import dev.oribuin.gadgets.manager.GadgetManager;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.storage.NodeProvider;
import dev.oribuin.gadgets.util.Registerable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.incendo.cloud.annotations.AnnotationParser;

import java.nio.file.Path;
import java.util.Set;

public class GadgetsPlugin extends JavaPlugin {

    private static GadgetsPlugin instance;
    private ConfigLoader loader;
    private NodeProvider provider;
    private CommandManager commandManager;
    private DataManager dataManager;
    private GadgetManager gadgetManager;

    @Override
    public void onEnable() {
        instance = this;
        
        CustomBlockData.registerListener(this);

        // Load this plugin configs
        this.loader = new ConfigLoader();
        this.loader.loadConfig(Config.class, "settings");
        this.loader.loadConfig(PluginMessages.class, "messages");
        this.loader.loadConfig(MySQLConfig.class, "database");
        
        NodeFactory.init();

        this.commandManager = new CommandManager(this);
        this.dataManager = new DataManager(this);
        this.gadgetManager = new GadgetManager(this);

        provider = new NodeProvider(this);

        // Register listeners
        this.registerEvents(
                new CustomBlockListener(),
                new ItemListener(),
                new NexoListener(),
                new QuickShopListener(),
                new HeadDbProvider()
        );
    }

    @Override
    public void onDisable() {
        this.commandManager.disable(this);
        this.dataManager.disable(this);
        this.gadgetManager.disable(this);
        this.getLoader().close();
    }

    public static GadgetsPlugin get() {
        return instance;
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
    
    public ConfigLoader getLoader() {
        return loader;
    }

    public NodeProvider getProvider() {
        return provider;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public GadgetManager getGadgetManager() {
        return gadgetManager;
    }
}

