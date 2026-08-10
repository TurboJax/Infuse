package com.catadmirer.infuseSMP;

import java.awt.Color;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;

public class EffectConstants {
    public enum Id {
        EMERALD,
        ENDER,
        FEATHER,
        FIRE,
        FROST,
        HASTE,
        HEART,
        INVIS,
        OCEAN,
        REGEN,
        SPEED,
        STRENGTH,
        THUNDER,
        APOPHIS,
        THIEF;

        public int value() {
            return ordinal();
        }
    }

    public enum BackgroundColor {
        EMERALD(Key.key("lime_stained_glass_pane")),
        ENDER(Key.key("purple_stained_glass_pane")),
        FEATHER(Key.key("white_stained_glass_pane")),
        FIRE(Key.key("orange_stained_glass_pane")),
        FROST(Key.key("light_blue_stained_glass_pane")),
        HASTE(Key.key("orange_stained_glass_pane")),
        HEART(Key.key("red_stained_glass_pane")),
        INVIS(Key.key("light_gray_stained_glass_pane")),
        OCEAN(Key.key("blue_stained_glass_pane")),
        REGEN(Key.key("red_stained_glass_pane")),
        SPEED(Key.key("light_blue_stained_glass_pane")),
        STRENGTH(Key.key("red_stained_glass_pane")),
        THUNDER(Key.key("yellow_stained_glass_pane")),
        APOPHIS(Key.key("magenta_stained_glass_pane")),
        THIEF(Key.key("red_stained_glass_pane"));

        private final Key key;

        BackgroundColor(Key key) {
            this.key = key;
        }

        public Key value() {
            return key;
        }
    }

    public enum PotionColor {
        EMERALD(Color.GREEN),
        ENDER(new Color(0x800080)),
        FEATHER(new Color(0xBEA3CA)),
        FIRE(new Color(0xEE5522)),
        FROST(new Color(0x55FFFF)),
        HASTE(new Color(0xFFCC33)),
        HEART(Color.RED),
        INVIS(new Color(0xAA00AA)),
        OCEAN(new Color(0x0066FF)),
        REGEN(new Color(0xFF5555)),
        SPEED(new Color(0xEEBB77)),
        STRENGTH(new Color(0x800000)),
        THUNDER(Color.YELLOW),
        APOPHIS(new Color(0x440044)),
        THIEF(new Color(0xAA0000));

        private final Color color;

        PotionColor(Color color) {
            this.color = color;
        }

        public Color value() {
            return color;
        }
    }

    public enum RitualColor {
        EMERALD(BossBar.Color.GREEN),
        ENDER(BossBar.Color.PURPLE),
        FEATHER(BossBar.Color.WHITE),
        FIRE(BossBar.Color.RED),
        FROST(BossBar.Color.BLUE),
        HASTE(BossBar.Color.YELLOW),
        HEART(BossBar.Color.RED),
        INVIS(BossBar.Color.PURPLE),
        OCEAN(BossBar.Color.BLUE),
        REGEN(BossBar.Color.PINK),
        SPEED(BossBar.Color.YELLOW),
        STRENGTH(BossBar.Color.RED),
        THUNDER(BossBar.Color.RED),
        APOPHIS(BossBar.Color.PURPLE),
        THIEF(BossBar.Color.YELLOW);

        private final BossBar.Color color;

        RitualColor(BossBar.Color color) {
            this.color = color;
        }

        public BossBar.Color value() {
            return color;
        }
    }
}
