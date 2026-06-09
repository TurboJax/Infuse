package com.catadmirer.infuseSMP.playerdata;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.h2.jdbcx.JdbcDataSource;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;

@NullMarked
public class H2DataManager implements DataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Infuse_Storage");
    private final DataSource dataSource;

    public H2DataManager(Infuse plugin) {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException err) {
            LOGGER.error("Could not load the H2 driver", err);
        }

        // Creating the JDBC DataSource
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:./" + plugin.getDataFolder().getPath() + "/data/playerdata");
        dataSource.setUser("infuse");
        dataSource.setPassword("");
        dataSource.setDescription("Infuse PlayerData Storage");

        this.dataSource = dataSource;
    }

    @Override
    public void load() {
        final String createPlayerDataTable = "CREATE TABLE IF NOT EXISTS player_data(player UUID PRIMARY KEY, slot_1 INTEGER NOT NULL, slot_2 INTEGER NOT NULL, offhand_control BOOLEAN NOT NULL);";
        final String createTrustTable = "CREATE TABLE IF NOT EXISTS trusts(truster UUID NOT NULL, trusted UUID NOT NULL);";
        final String createCraftedTable = "CREATE TABLE IF NOT EXISTS crafted_effects(effect INTEGER NOT NULL, count INTEGER NOT NULL);";

        try (Connection conn = dataSource.getConnection()) {
            // Creating the tables if they don't exist
            Statement stmt = conn.createStatement();
            stmt.execute(createPlayerDataTable);
            stmt.execute(createTrustTable);
            stmt.execute(createCraftedTable);
            stmt.close();

            // Commiting any changes
            conn.commit();
        } catch (SQLException err) {
            LOGGER.error("Could not open connection to H2 database", err);
        }
    }

    @Override
    public void save() {}

    @Override
    public int getExistingCount(InfuseEffect effect) {
        return 0;
    }

    @Override
    public void setExistingCount(InfuseEffect effect, int count) {}

    @Override
    public Set<OfflinePlayer> getTrusted(OfflinePlayer player) {
        String selectStr = "SELECT trusted FROM trusts WHERE truster = ?";
        UUID trusterUUID = player.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(selectStr);
            stmt.setObject(1, trusterUUID);

            Set<UUID> trustedUUIDs = new HashSet<>();
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                try {
                    trustedUUIDs.add(result.getObject(1, UUID.class));
                } catch (SQLException err) {
                    LOGGER.warn("Invalid UUID in SQL results.  Skipping value.");
                }
            }

            return trustedUUIDs.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
        } catch (SQLException err) {
            LOGGER.info("Failed to connect to database.", err);
        }
        return Set.of();
    }

    @Override
    public void setTrusted(OfflinePlayer truster, Set<OfflinePlayer> trusted) {
        throw new UnsupportedOperationException("Unimplemented method 'setTrusted'");
    }

    @Override
    public void addTrust(OfflinePlayer truster, OfflinePlayer toTrust) {
        String insertElem = """
                            INSERT INTO trusts (truster, trusted)
                            SELECT ?, ?
                            WHERE NOT EXISTS (
                                SELECT * FROM trusts WHERE truster = ? AND trusted = ?
                            );""";

        UUID trusterUUID = truster.getUniqueId();
        UUID toTrustUUID = toTrust.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(insertElem)) {
                stmt.setObject(1, trusterUUID);
                stmt.setObject(2, toTrustUUID);
                stmt.setObject(3, trusterUUID);
                stmt.setObject(4, toTrustUUID);
                stmt.execute();
            } catch (SQLException err) {
                LOGGER.error("Failed to insert data into database", err);
            }
        } catch (SQLException err) {
            LOGGER.info("Failed to connect to database.", err);
        }
    }

    @Override
    public void removeTrust(OfflinePlayer truster, OfflinePlayer toRemove) {
        String deleteElem = "DELETE FROM trusts WHERE truster = ? AND trusted = ?";

        UUID trusterUUID = truster.getUniqueId();
        UUID trustedUUID = toRemove.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(deleteElem)) {
                stmt.setObject(1, trusterUUID);
                stmt.setObject(2, trustedUUID);
                stmt.execute();
            } catch (SQLException err) {
                LOGGER.error("Failed to remove data from the database", err);
            }
        } catch (SQLException err) {
            LOGGER.info("Failed to connect to database.", err);
        }
    }

    @Override
    public boolean isTrusted(OfflinePlayer caster, OfflinePlayer trusted) {
        String selectStr = "SELECT trusted FROM trusts WHERE truster = ? AND trusted = ?";

        UUID trusterUUID = caster.getUniqueId();
        UUID trustedUUID = trusted.getUniqueId();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(selectStr);
            stmt.setObject(1, trusterUUID);
            stmt.setObject(2, trustedUUID);

            return stmt.executeQuery().next();
        } catch (SQLException err) {
            LOGGER.info("Failed to connect to database.", err);
        }

        return false;
    }

    @Override
    public void setEffect(UUID owner, String slot, @Nullable InfuseEffect effect) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEffect'");
    }

    @Nullable
    @Override
    public InfuseEffect getEffect(UUID owner, String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEffect'");
    }

    @Override
    public boolean hasEffect(OfflinePlayer player, InfuseEffect effect, boolean differentiateAugmented, String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasEffect'");
    }

    @Override
    public void removeEffect(UUID playerUUID, String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeEffect'");
    }

    @Override
    public void setControlMode(UUID playerUUID, String defaultMode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setControlMode'");
    }

    @Override
    public String getControlMode(UUID playerUUID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getControlMode'");
    }

    @Override
    public void applyUpdates() {}
}
