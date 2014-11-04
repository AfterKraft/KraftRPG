/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Gabriel Harris-Rouquette
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.afterkraft.kraftrpg.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.KraftRPGPlugin;

/**
 * Utility for handling messages. All messages handled by the plugin should technically have a
 * language specific translation provided, unless the message originates from a skill that is using
 * hard coded messages.
 */
public class Messaging {

    private static ResourceBundle messages;

    public static void send(CommandSender sender, String message, Object... args) {
        sender.sendMessage(parametrizeMessage(message, args));
    }

    public static String parametrizeMessage(String msg, Object... params) {
        msg = ChatColor.GRAY + msg;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                msg = msg.replace("$" + (i + 1),
                                  ChatColor.WHITE + params[i].toString() + ChatColor.GRAY);
            }
        }
        return msg;
    }

    public static String getEntityName(Entity entity) {
        EntityType entityType = entity.getType();
        if (entityType == org.bukkit.entity.EntityType.DROPPED_ITEM) {
            return getMessage("entity-name-dropped-item");
        } else if (entityType == org.bukkit.entity.EntityType.EXPERIENCE_ORB) {
            return getMessage("entity-name-experience-orb");
        } else if (entityType == org.bukkit.entity.EntityType.LEASH_HITCH) {
            return getMessage("entity-name-leash");
        } else if (entityType == org.bukkit.entity.EntityType.PAINTING) {
            return getMessage("entity-name-painting");
        } else if (entityType == org.bukkit.entity.EntityType.ARROW) {
            return getMessage("entity-name-arrow");
        } else if (entityType == org.bukkit.entity.EntityType.SNOWBALL) {
            return getMessage("entity-name-snowball");
        } else if (entityType == org.bukkit.entity.EntityType.FIREBALL) {
            return getMessage("entity-name-fireball");
        } else if (entityType == org.bukkit.entity.EntityType.SMALL_FIREBALL) {
            return getMessage("entity-name-small-fireball");
        } else if (entityType == org.bukkit.entity.EntityType.ENDER_PEARL) {
            return getMessage("entity-name-ender-pearl");
        } else if (entityType == org.bukkit.entity.EntityType.ENDER_SIGNAL) {
            return getMessage("entity-name-eye-of-ender");
        } else if (entityType == org.bukkit.entity.EntityType.THROWN_EXP_BOTTLE) {
            return getMessage("entity-name-experience-bottle");
        } else if (entityType == org.bukkit.entity.EntityType.ITEM_FRAME) {
            return getMessage("entity-name-item-frame");
        } else if (entityType == org.bukkit.entity.EntityType.WITHER_SKULL) {
            return getMessage("entity-name-wither-skull");
        } else if (entityType == org.bukkit.entity.EntityType.PRIMED_TNT) {
            return getMessage("entity-name-tnt");
        } else if (entityType == org.bukkit.entity.EntityType.FALLING_BLOCK) {
            return getMessage("entity-name-falling-block");
        } else if (entityType == org.bukkit.entity.EntityType.FIREWORK) {
            return getMessage("entity-name-firework");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART_COMMAND) {
            return getMessage("entity-name-minecart-commandblock");
        } else if (entityType == org.bukkit.entity.EntityType.BOAT) {
            return getMessage("entity-name-boat");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART) {
            return getMessage("entity-name-minecart");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART_CHEST) {
            return getMessage("entity-name-minecart-chest");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART_FURNACE) {
            return getMessage("entity-name-minecart-furnace");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART_TNT) {
            return getMessage("entity-name-minecart-tnt");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART_HOPPER) {
            return getMessage("entity-name-minecart-hopper");
        } else if (entityType == org.bukkit.entity.EntityType.MINECART_MOB_SPAWNER) {
            return getMessage("entity-name-minecart-mobspawner");
        } else if (entityType == org.bukkit.entity.EntityType.CREEPER) {
            return getMessage("entity-name-creeper");
        } else if (entityType == org.bukkit.entity.EntityType.SKELETON) {
            return getMessage("entity-name-skeleton");
        } else if (entityType == org.bukkit.entity.EntityType.SPIDER) {
            return getMessage("entity-name-spider");
        } else if (entityType == org.bukkit.entity.EntityType.GIANT) {
            return getMessage("entity-name-giant");
        } else if (entityType == org.bukkit.entity.EntityType.ZOMBIE) {
            return getMessage("entity-name-zombie");
        } else if (entityType == org.bukkit.entity.EntityType.SLIME) {
            return getMessage("entity-name-slime");
        } else if (entityType == org.bukkit.entity.EntityType.GHAST) {
            return getMessage("entity-name-ghast");
        } else if (entityType == org.bukkit.entity.EntityType.PIG_ZOMBIE) {
            return getMessage("entity-name-pig-zombie");
        } else if (entityType == org.bukkit.entity.EntityType.ENDERMAN) {
            return getMessage("entity-name-enderman");
        } else if (entityType == org.bukkit.entity.EntityType.CAVE_SPIDER) {
            return getMessage("entity-name-cavespider");
        } else if (entityType == org.bukkit.entity.EntityType.SILVERFISH) {
            return getMessage("entity-name-silverfish");
        } else if (entityType == org.bukkit.entity.EntityType.BLAZE) {
            return getMessage("entity-name-blaze");
        } else if (entityType == org.bukkit.entity.EntityType.MAGMA_CUBE) {
            return getMessage("entity-name-magma-cube");
        } else if (entityType == org.bukkit.entity.EntityType.ENDER_DRAGON) {
            return getMessage("entity-name-ender-dragon");
        } else if (entityType == org.bukkit.entity.EntityType.WITHER) {
            return getMessage("entity-name-wither");
        } else if (entityType == org.bukkit.entity.EntityType.BAT) {
            return getMessage("entity-name-bat");
        } else if (entityType == org.bukkit.entity.EntityType.WITCH) {
            return getMessage("entity-name-witch");
        } else if (entityType == org.bukkit.entity.EntityType.PIG) {
            return getMessage("entity-name-pig");
        } else if (entityType == org.bukkit.entity.EntityType.SHEEP) {
            return getMessage("entity-name-sheep");
        } else if (entityType == org.bukkit.entity.EntityType.COW) {
            return getMessage("entity-name-cow");
        } else if (entityType == org.bukkit.entity.EntityType.CHICKEN) {
            return getMessage("entity-name-chicken");
        } else if (entityType == org.bukkit.entity.EntityType.SQUID) {
            return getMessage("entity-name-squid");
        } else if (entityType == org.bukkit.entity.EntityType.WOLF) {
            return getMessage("entity-name-wolf");
        } else if (entityType == org.bukkit.entity.EntityType.MUSHROOM_COW) {
            return getMessage("entity-name-mushroom-cow");
        } else if (entityType == org.bukkit.entity.EntityType.SNOWMAN) {
            return getMessage("entity-name-snowman");
        } else if (entityType == org.bukkit.entity.EntityType.OCELOT) {
            return getMessage("entity-name-ocelot");
        } else if (entityType == org.bukkit.entity.EntityType.IRON_GOLEM) {
            return getMessage("entity-name-iron-golem");
        } else if (entityType == org.bukkit.entity.EntityType.HORSE) {
            return getMessage("entity-name-horse");
        } else if (entityType == org.bukkit.entity.EntityType.VILLAGER) {
            return getMessage("entity-name-villager");
        } else if (entityType == org.bukkit.entity.EntityType.ENDER_CRYSTAL) {
            return getMessage("entity-name-ender-crystal");
        } else if (entityType == org.bukkit.entity.EntityType.SPLASH_POTION) {
            return getMessage("entity-name-splash-potion");
        } else if (entityType == org.bukkit.entity.EntityType.EGG) {
            return getMessage("entity-name-egg");
        } else if (entityType == org.bukkit.entity.EntityType.FISHING_HOOK) {
            return getMessage("entity-name-fishing-hook");
        } else if (entityType == org.bukkit.entity.EntityType.LIGHTNING) {
            return getMessage("entity-name-lightning");
        } else if (entityType == org.bukkit.entity.EntityType.PLAYER) {
            return ((Player) entity).getName();
        } else {
            return getMessage("entity-name-unknown");
        }
    }

    /**
     * A safe method to retrieve a message string based on Locale of the plugin. It will
     * automatically colorize the configured messages and if the message is not loaded in the
     * language pack, it will return an empty string and send a warning to log.
     *
     * @param key of the message to fetch
     *
     * @return the localized message for the key
     */
    public static String getMessage(String key) {
        try {
            return getMessage1(key);
        } catch (MessageNotFoundException e) {
            KraftRPGPlugin.getInstance()
                    .log(Level.SEVERE, "Messages.properties is missing: " + key);
            return "";
        }
    }

    /**
     * Retrieves the localized message for Townships
     *
     * @param key - Message key to be obtained
     *
     * @return - Message that is localized to the configured Language
     * @throws MessageNotFoundException When the message is not found
     */
    private static String getMessage1(String key) throws MessageNotFoundException {
        String msg = messages.getString(key);
        if (msg == null) {
            throw new MessageNotFoundException();
        } else {
            msg = colorize(msg);
            return msg;
        }
    }

    /**
     * Colorizes the message prior to being sent out. Eliminates the use of ChatColor and characters
     * that could break if character format is changed.
     *
     * @param message The message to colorize
     *
     * @return A colorized message with the correct unicode character
     */
    public static String colorize(String message) {
        return message.replaceAll("(?i)&([a-fklmno0-9])", "\u00A7$1");
    }

    /**
     * Defines the current Language for the plugin, if the localization is available.
     *
     * @param locale - Locale to be used
     *
     * @throws ClassNotFoundException When the bundle is not found
     */
    public static void setLocale(Locale locale) throws ClassNotFoundException {
        messages = ResourceBundle.getBundle("resources.Messages.Messages", locale);
        if (messages == null) {
            throw new ClassNotFoundException("resources.Messages");
        }
    }
}
