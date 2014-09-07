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
package com.afterkraft.kraftrpg.editor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;
import com.afterkraft.kraftrpg.api.util.Utilities;

public class EditorRoleFocus extends EditorPrompt {
    @Override
    public String getName(ConversationContext context) {
        return EditorState.getSelectedRole(context).getName();
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        EditorPrompt common = commonActions(context, command);
        if (common != null) return common;

        Role role = EditorState.getSelectedRole(context);
        final Role.Builder builder = Role.copyOf(role);

        if (command.equals("name")) {
            sendMessage(context, "Unfortunately, changing the name of a role is not supported.");
            return this;
        } else if (command.equals("description")) {
            return callPrompt(context, new PromptGetString(
                    "Choose a new description. & for color codes. Don't use a semicolon.", false) {
                @Override
                public boolean apply(String input) {
                    builder.setDescription(ChatColor.translateAlternateColorCodes('&', input));
                    return true;
                }
            });
        } else if (command.equals("type")) {
            return callPrompt(context, new EditorRoleFocusType());
        } else if (command.equals("default")) {
            // If already default, choose a new default.
            // If not default, confirm to make default.
            if (role.isDefault()) {
                final boolean primary = role.getType() == RoleType.PRIMARY;

                return callPrompt(context, new PromptGetRole(
                        "Please choose the new default " + (primary ? "class" : "profession") + ".") {
                    @Override
                    public boolean apply(Role r) {
                        if (primary) {
                            plugin.getRoleManager().setDefaultPrimaryRole(r);
                        } else {
                            plugin.getRoleManager().setDefaultSecondaryRole(r);
                        }
                        return true;
                    }
                });
            } else {
                if (role.getType() == RoleType.ADDITIONAL) {
                    sendMessage(context, "Extra roles cannot be defaults");
                    return null;
                }
                return callPrompt(context, new EditorConfirmMakeDefault());
            }
        } else if (command.equals("parents")) {
        } else if (command.equals("hp") || command.equals("mp") || command.equals("regen") || command.equals("mp regen")) {

        } else if (command.equals("armor") || command.equals("tools")) {

        } else if (command.equals("delete")) {

        }
        /*
         * Name Type Default Description Parents hp/mp/regen Skills
         * Armor/tools delete
         */

        return null;
    }

    @Override
    public void printBanner(ConversationContext context) {
        sendMessage(context, "%sKraftRPG Configuration Editor: %sRole Detail", ChatColor.DARK_GREEN, ChatColor.BLUE);

        Role role = EditorState.getSelectedRole(context);

        sendMessage(context, "%sRole: %s%s%s Type: %s%s%s %s",
                ChatColor.GREEN,
                ChatColor.GOLD, role.getName(), ChatColor.GREEN,
                ChatColor.AQUA, StringUtils.capitalize(role.getType().toString().toLowerCase()), ChatColor.GREEN,
                role.isDefault() ? ChatColor.YELLOW + "(default)" : "");

        StringBuilder sb;
        sb = new StringBuilder(ChatColor.GREEN.toString()).append("Parents: ");
        if (role.getParents().isEmpty()) {
            sb.append(ChatColor.GRAY.toString()).append("(none)");
        }
        for (Role parent : role.getParents()) {
            sb.append(ChatColor.GOLD.toString());
            sb.append(parent.getName());
            sb.append(ChatColor.YELLOW.toString());
            sb.append(" (").append(parent.getAdvancementLevel()).append(") ");
        }
        if (!role.isChoosable()) {
            sb.append(ChatColor.RED.toString());
            sb.append("(Not Choosable)");
        }
        sendMessage(context, sb.toString());

        sendMessage(context, "%sDesc: %s\"%s%s\"",
                ChatColor.GREEN,
                ChatColor.WHITE, ChatColor.ITALIC, role.getDescription(), ChatColor.WHITE);

        if (role.getType() == RoleType.PRIMARY) {
            // TODO fix maxlevel=50 assumption
            sendMessage(context, "%sHP: %s %sMP: %s %sMP Regen: %s [Level 1-50]",
                    ChatColor.DARK_RED, Utilities.minMaxString(role.getMaxHealthAtZero(), role.getMaxHealthAtLevel(50), ChatColor.RED),
                    ChatColor.DARK_BLUE, Utilities.minMaxString(role.getMaxManaAtZero(), role.getMaxManaAtLevel(50), ChatColor.BLUE),
                    ChatColor.DARK_AQUA, Utilities.minMaxString(role.getManaRegenAtZero(), role.getManaRegenAtLevel(50), ChatColor.AQUA));
        }

        sendMessage(context, "%s%d%s Skills", ChatColor.YELLOW, role.getAllSkills().size(), ChatColor.GREEN);
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return getPathString(context) + "type default description parents hp mp regen skills armor tools delete save exit";
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("type", "default", "description", "parents", "hp", "mp", "regen", "skills", "armor", "tools", "delete");
    }
}
