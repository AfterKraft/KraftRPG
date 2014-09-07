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
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.KraftRPGPlugin;

public class Messaging {

    private static ResourceBundle messages;

    public static void send(CommandSender sender, String message, Object... args) {
        sender.sendMessage(parametrizeMessage(message, args));
    }

    public static String parametrizeMessage(String msg, Object... params) {
        msg = ChatColor.GRAY + msg;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                msg = msg.replace("$" + (i + 1), ChatColor.WHITE + params[i].toString() + ChatColor.GRAY);
            }
        }
        return msg;
    }

    public static String getEntityName(Entity entity) {
        switch (entity.getType()) {
            case DROPPED_ITEM:
                return getMessage("entity-name-dropped-item");
            case EXPERIENCE_ORB:
                return getMessage("entity-name-experience-orb");
            case LEASH_HITCH:
                return getMessage("entity-name-leash");
            case PAINTING:
                return getMessage("entity-name-painting");
            case ARROW:
                return getMessage("entity-name-arrow");
            case SNOWBALL:
                return getMessage("entity-name-snowball");
            case FIREBALL:
                return getMessage("entity-name-fireball");
            case SMALL_FIREBALL:
                return getMessage("entity-name-small-fireball");
            case ENDER_PEARL:
                return getMessage("entity-name-ender-pearl");
            case ENDER_SIGNAL:
                return getMessage("entity-name-eye-of-ender");
            case THROWN_EXP_BOTTLE:
                return getMessage("entity-name-experience-bottle");
            case ITEM_FRAME:
                return getMessage("entity-name-item-frame");
            case WITHER_SKULL:
                return getMessage("entity-name-wither-skull");
            case PRIMED_TNT:
                return getMessage("entity-name-tnt");
            case FALLING_BLOCK:
                return getMessage("entity-name-falling-block");
            case FIREWORK:
                return getMessage("entity-name-firework");
            case MINECART_COMMAND:
                return getMessage("entity-name-minecart-commandblock");
            case BOAT:
                return getMessage("entity-name-boat");
            case MINECART:
                return getMessage("entity-name-minecart");
            case MINECART_CHEST:
                return getMessage("entity-name-minecart-chest");
            case MINECART_FURNACE:
                return getMessage("entity-name-minecart-furnace");
            case MINECART_TNT:
                return getMessage("entity-name-minecart-tnt");
            case MINECART_HOPPER:
                return getMessage("entity-name-minecart-hopper");
            case MINECART_MOB_SPAWNER:
                return getMessage("entity-name-minecart-mobspawner");
            case CREEPER:
                return getMessage("entity-name-creeper");
            case SKELETON:
                return getMessage("entity-name-skeleton");
            case SPIDER:
                return getMessage("entity-name-spider");
            case GIANT:
                return getMessage("entity-name-giant");
            case ZOMBIE:
                return getMessage("entity-name-zombie");
            case SLIME:
                return getMessage("entity-name-slime");
            case GHAST:
                return getMessage("entity-name-ghast");
            case PIG_ZOMBIE:
                return getMessage("entity-name-pig-zombie");
            case ENDERMAN:
                return getMessage("entity-name-enderman");
            case CAVE_SPIDER:
                return getMessage("entity-name-cavespider");
            case SILVERFISH:
                return getMessage("entity-name-silverfish");
            case BLAZE:
                return getMessage("entity-name-blaze");
            case MAGMA_CUBE:
                return getMessage("entity-name-magma-cube");
            case ENDER_DRAGON:
                return getMessage("entity-name-ender-dragon");
            case WITHER:
                return getMessage("entity-name-wither");
            case BAT:
                return getMessage("entity-name-bat");
            case WITCH:
                return getMessage("entity-name-witch");
            case PIG:
                return getMessage("entity-name-pig");
            case SHEEP:
                return getMessage("entity-name-sheep");
            case COW:
                return getMessage("entity-name-cow");
            case CHICKEN:
                return getMessage("entity-name-chicken");
            case SQUID:
                return getMessage("entity-name-squid");
            case WOLF:
                return getMessage("entity-name-wolf");
            case MUSHROOM_COW:
                return getMessage("entity-name-mushroom-cow");
            case SNOWMAN:
                return getMessage("entity-name-snowman");
            case OCELOT:
                return getMessage("entity-name-ocelot");
            case IRON_GOLEM:
                return getMessage("entity-name-iron-golem");
            case HORSE:
                return getMessage("entity-name-horse");
            case VILLAGER:
                return getMessage("entity-name-villager");
            case ENDER_CRYSTAL:
                return getMessage("entity-name-ender-crystal");
            case SPLASH_POTION:
                return getMessage("entity-name-splash-potion");
            case EGG:
                return getMessage("entity-name-egg");
            case FISHING_HOOK:
                return getMessage("entity-name-fishing-hook");
            case LIGHTNING:
                return getMessage("entity-name-lightning");
            case PLAYER:
                return ((Player) entity).getName();
            case UNKNOWN:
            default:
                return getMessage("entity-name-unknown");
        }
    }

    /**
     * A safe method to retrieve a message string based on Locale of the
     * plugin. It will automatically colorize the configured messages and if
     * the message is not loaded in the language pack, it will return an empty
     * string and send a warning to log.
     * 
     * @param key of the message to fetch
     * @return the localized message for the key
     */
    public static String getMessage(String key) {
        try {
            return getMessage1(key);
        } catch (MessageNotFoundException e) {
            KraftRPGPlugin.getInstance().log(Level.SEVERE, "Messages.properties is missing: " + key);
            return "";
        }
    }

    /**
     * Retrieves the localized message for Townships
     * 
     * @param key - Message key to be obtained
     * @return - Message that is localized to the configured Language
     * @throws MessageNotFoundException
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
     * Colorizes the message prior to being sent out. Eliminates the use of
     * ChatColor and characters that could break if character format is
     * changed.
     */
    public static String colorize(String message) {
        return message.replaceAll("(?i)&([a-fklmno0-9])", "\u00A7$1");
    }

    /**
     * Defines the current Language for the plugin, if the localization is
     * available.
     * 
     * @param locale - Locale to be used
     * @throws ClassNotFoundException
     */
    public static void setLocale(Locale locale) throws ClassNotFoundException {
        messages = ResourceBundle.getBundle("resources.Messages.Messages", locale);
        if (messages == null) {
            throw new ClassNotFoundException("resources.Messages");
        }
    }
}
