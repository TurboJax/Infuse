package com.catadmirer.infuseSMP.bukkit.util;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.catadmirer.infuseSMP.bukkit.BukkitEffectRegistry;
import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.bukkit.effects.BukkitEffect;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BukkitEffectArgumentType implements CustomArgumentType<BukkitEffect,String> {
    private static final DynamicCommandExceptionType ERROR_NO_EFFECT_FOUND = new DynamicCommandExceptionType(s -> new LiteralMessage("\"" + s + "\" is not a registered effect key."));
    private final BukkitEffectRegistry effectRegistry = InfusePlugin.getInstance().getEffectRegistry();

    @Override
    public BukkitEffect parse(StringReader reader) throws CommandSyntaxException {
        String key = reader.readUnquotedString();

        if (!effectRegistry.isRegistered(key)) {
            throw ERROR_NO_EFFECT_FOUND.create(key);
        }

        BukkitEffect effect = effectRegistry.fromKey(key);
        if (effect == null) throw ERROR_NO_EFFECT_FOUND.create(key);
        return effect;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        effectRegistry.getRegisteredEffects()
            .stream()
            .flatMap(e -> Stream.of(e.getRegularVersion(), e.getAugmentedVersion()))
            .map(InfuseEffect::toString)
            .filter(s -> s.contains(builder.getRemaining()))
            .sorted()
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}