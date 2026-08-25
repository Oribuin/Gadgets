package dev.oribuin.gadgets.manager;

import com.google.gson.Gson;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.impl.MySQLConfig;
import dev.oribuin.gadgets.database.connector.DatabaseConnector;
import dev.oribuin.gadgets.database.connector.MySQLConnector;
import dev.oribuin.gadgets.database.connector.SQLiteConnector;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.scheduler.PluginScheduler;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.block.Block;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DataManager implements Manager {

    private static final Gson GSON = new Gson();

    private final GadgetsPlugin plugin;
    private DatabaseConnector connector;

    public DataManager(GadgetsPlugin plugin) {
        this.plugin = plugin;
        this.reload(plugin);
    }

    /**
     * The task that runs when the plugin is loaded/reloaded
     *
     * @param plugin The plugin reloading
     */
    public void reload(GadgetsPlugin plugin) {
        this.disable(plugin);

        MySQLConfig sqlConfig = MySQLConfig.get();
        if (sqlConfig.isEnabled()) {
            String hostname = sqlConfig.getHostname();
            int port = sqlConfig.getPort();
            String database = sqlConfig.getDatabaseName();
            String username = sqlConfig.getUsername();
            String password = sqlConfig.getPassword();
            boolean useSSL = sqlConfig.useSSL();
            int poolSize = sqlConfig.getConnectionPoolSize();

            this.connector = new MySQLConnector(this.plugin, hostname, port, database, username, password, useSSL, poolSize);
            this.plugin.getLogger().info("Data manager connected using MySQL.");
        } else {
            this.connector = new SQLiteConnector(this.plugin);
            this.connector.cleanup();
            this.plugin.getLogger().info("Data manager connected using SQLite.");
        }

        // Create the initial table for the plugin
        this.connector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.addBatch(CREATE_TABLE);
                statement.executeBatch();
            }
        });
    }

    /**
     * The task that runs when the plugin is disabled, usually takes priority over {@link Manager#reload(GadgetsPlugin)}
     *
     * @param plugin The plugin being disabled
     */
    public void disable(GadgetsPlugin plugin) {
        if (this.connector != null) {
            // Wait for all connections to finish
            long now = System.currentTimeMillis();
            long deadline = now + 5000;
            synchronized (this.connector.getLock()) {
                while (!this.connector.isFinished() && now < deadline) {
                    try {
                        this.connector.getLock().wait(deadline - now);
                        now = System.currentTimeMillis();
                    } catch (InterruptedException ex) {
                        this.plugin.getLogger().severe("Interrupted error occurred: " + ex.getMessage());
                    }
                }
            }

            this.connector.closeConnection();
        }
    }

    public CompletableFuture<Map<FinePosition, NodeType<?>>> loadPositions() {
        return CompletableFuture.supplyAsync(() -> {
            Map<FinePosition, NodeType<?>> nodeTypes = new HashMap<>();
            try (
                    Connection connection = this.connector.connect();
                    PreparedStatement statement = connection.prepareStatement(SELECT_ALL)
            ) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String world = resultSet.getString("world");
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");
                    NodeType<?> type = NodeFactory.from(resultSet.getString("type"));
                    if (type == null) continue;

                    FinePosition position = new FinePosition(world, x, y, z);
                    nodeTypes.put(position, type);
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().severe("Failed to load node positions due to: " + ex.getMessage());
            }

            this.plugin.getLogger().info("Loaded " + nodeTypes.size() + " positions");
            return nodeTypes;
        });
    }

    public void storePosition(Node node) {
        Block block = node.getBlock();
        if (block == null) return;

        this.async(() -> {
            try (
                    Connection connection = this.connector.connect();
                    PreparedStatement statement = connection.prepareStatement(INSERT)
            ) {
                statement.setString(1, node.getIdentifier().get());
                statement.setString(2, block.getWorld().getName());
                statement.setInt(3, block.getX());
                statement.setInt(4, block.getY());
                statement.setInt(5, block.getZ());
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().severe("Failed to store a node position due to: " + ex.getMessage());
            }
        });
    }


    public void deletePosition(Node node) {
        Block block = node.getBlock();
        if (block == null) return;

        this.async(() -> {
            try (
                    Connection connection = this.connector.connect();
                    PreparedStatement statement = connection.prepareStatement(DELETE)
            ) {
                statement.setString(1, block.getWorld().getName());
                statement.setInt(2, block.getX());
                statement.setInt(3, block.getY());
                statement.setInt(4, block.getZ());
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().severe("Failed to store a node position due to: " + ex.getMessage());
            }
        });
    }

    /**
     * Run a task asynchronously
     *
     * @param runnable The task to run
     */
    public void async(Runnable runnable) {
        PluginScheduler.get().runTaskAsync(runnable);
    }

    // region SQL Queries
    @Language("MariaDB")
    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS gadgets_node_positions(
                `type` VARCHAR(64) NOT NULL,
                world VARCHAR(36) NOT NULL,
                x INT NOT NULL,
                y INT NOT NULL,
                z INT NOT NULL
            );
            """;

    @Language("MariaDB")
    private static final String SELECT_ALL = "SELECT * FROM gadgets_node_positions;";

    @Language("MariaDB")
    private static final String INSERT = """
            INSERT INTO gadgets_node_positions(`type`, `world`, `x`, `y`, `z`)
            VALUES (?, ?, ?, ?, ?);
            """;

    @Language("MariaDB")
    private static final String DELETE = "DELETE FROM gadgets_node_positions WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?;";
    // endregion
}
