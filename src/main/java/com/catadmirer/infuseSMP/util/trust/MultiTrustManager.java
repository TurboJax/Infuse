package com.catadmirer.infuseSMP.util.trust;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.expansions.ExpansionHelper;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NullMarked
public class MultiTrustManager implements TrustManager {
    private final Infuse plugin = Infuse.getInstance();
    private final List<TrustManager> trustManagers = new ArrayList<>();

    public MultiTrustManager() {
        if (ExpansionHelper.canUseBetterTeams() && plugin.getMainConfig().enableBetterTeams()) trustManagers.add(new BetterTeamsTrustManager());
        trustManagers.add(plugin.getDataManager());
    }

    @Override
    public Set<UUID> getTrusted(UUID player) {
        Set<UUID> allTrusted = new HashSet<>();
        trustManagers.forEach(m -> allTrusted.addAll(m.getTrusted(player)));
        return allTrusted;
    }

    @Override
    public void setTrusted(UUID player, Set<UUID> trusted) {
        trustManagers.forEach(m -> {
            try {
                m.setTrusted(player, trusted);
            } catch (UnsupportedOperationException ignored) {}
        });
    }

    @Override
    public void addTrust(UUID player, UUID trusted) {
        trustManagers.forEach(m -> {
            try {
                m.addTrust(player, trusted);
            } catch (UnsupportedOperationException ignored) {}
        });
    }

    @Override
    public void removeTrust(UUID player, UUID trusted) {
        trustManagers.forEach(m -> {
            try {
                m.removeTrust(player, trusted);
            } catch (UnsupportedOperationException ignored) {}
        });
    }
}
