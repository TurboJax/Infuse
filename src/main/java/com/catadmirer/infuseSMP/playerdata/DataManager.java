package com.catadmirer.infuseSMP.playerdata;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

@NullMarked
public interface DataManager {
    /**
     * Reloads the player data.
     */
    void load();

    /**
     * Writes the config to the file.
     */
    void save();

    /**
     * Gets the number of effects that exist.
     * 
     * @param effect The effect to count
     */
    int getExistingCount(InfuseEffect effect);

    /**
     * Sets the number of effects that exists.
     * 
     * @param effect The effect to set the count of
     * @param count The number of this effect that exists
     */
    void setExistingCount(InfuseEffect effect, int count);

    /**
     * Gets a set of the players that the player trusts.
     *
     * @return The set of players trusted by the player.
     */
    Set<OfflinePlayer> getTrusted(OfflinePlayer player);

    /**
     * Sets the players that the truster trusts.
     * 
     * @param truster The player to modify
     * @param trusted The set of players the truster now trusts
     */
    void setTrusted(OfflinePlayer truster, Set<OfflinePlayer> trusted);

    /**
     * Adds a player to the list of trusted people.
     * 
     * @param truster The person whose trusted list to modify.
     * @param toTrust The person the truster now trusts.
     */
    default void addTrust(OfflinePlayer truster, OfflinePlayer toTrust) {
        Set<OfflinePlayer> trusted = getTrusted(truster);
        trusted.add(toTrust);
        setTrusted(truster, trusted);
    }

    /**
     * Removes a player from another player's list of trusted people.
     * 
     * @param truster The player whose trusted list to modify.
     * @param toRemove The person to remove from the truster's trust.
     */
    default void removeTrust(OfflinePlayer truster, OfflinePlayer toRemove) {
        Set<OfflinePlayer> trusted = getTrusted(truster);
        trusted.remove(toRemove);
        setTrusted(truster, trusted);
    }

    /**
     * Checks if a player is trusted by another player.
     * 
     * @param caster The player whose trusted list to check.
     * @param trusted The player to check if truster trusts.
     * 
     * @return True if the truster trusts the toCheck player, false otherwise
     */
    boolean isTrusted(OfflinePlayer caster, OfflinePlayer trusted);

    /**
     * Sets the infuse effect in a specific slot for a player.
     * 
     * @param owner The UUID of the player.
     * @param slot The slot to equip the effect in.
     * @param effect The {@link InfuseEffect} for the infuse effect.
     */
    void setEffect(UUID owner, String slot, @Nullable InfuseEffect effect);

    /**
     * Gets the infuse effect a player has in a specific slot.
     * 
     * @param owner The UUID of the player.
     * @param slot The slot to get the effect from.
     * 
     * @return null if there is not an effect equipped there or if the InfuseEffect could not be deserialized.  Otherwise, it returns the deserialized InfuseEffect.
     */
    @Nullable InfuseEffect getEffect(UUID owner, String slot);

    /**
     * Checks if the player has the infuse effect.
     * It checks both slots and doesn't differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    default boolean hasEffect(OfflinePlayer player, InfuseEffect effect) {
        return hasEffect(player, effect, false);
    }

    /**
     * Checks if the player has the infuse effect.
     * It checks both slots and doesn't differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    default boolean hasEffect(OfflinePlayer player, InfuseEffect effect, boolean differentiateAugmented) {
        return hasEffect(player, effect, differentiateAugmented, "1") || hasEffect(player, effect, differentiateAugmented, "2");        
    }

    /**
     * Checks if the player has the infuse effect.
     * It checks both slots and doesn't differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    default boolean hasEffect(OfflinePlayer player, InfuseEffect effect, String slot) {
        return hasEffect(player, effect, false, slot);
    }

    /**
     * Checks if the player has the infuse effect equipped in a specific slot.
     *
     * @param differentiateAugmented Whether the search should differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    boolean hasEffect(OfflinePlayer player, InfuseEffect effect, boolean differentiateAugmented, String slot);

    /**
     * Removes an infuse effect from a specific slot for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to remove an effect from.
     */
    void removeEffect(UUID playerUUID, String slot);

    /**
     * Sets the control mode for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param defaultMode The new control mode to use.
     */
    void setControlMode(UUID playerUUID, String defaultMode);

    /**
     * Gets the control mode of a player.
     * 
     * @param playerUUID The UUID of the player.
     * 
     * @return Either "command" or "offhand".  Defaults to "offhand"
     */
    String getControlMode(UUID playerUUID);

    /**
     * Modifies the config to make any necessary changes to make old versions compatible with this new one.
     */
    void applyUpdates();
}