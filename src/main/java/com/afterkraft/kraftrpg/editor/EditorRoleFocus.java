/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.editor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.util.Utilities;

public class EditorRoleFocus extends EditorPrompt {
    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("type", "default", "description", "parents", "hp", "mp", "regen", "skills", "armor", "tools", "delete");
    }

    @Override
    public String getName(ConversationContext context) {
        return EditorState.getSelectedRole(context).getName();
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return getPathString(context) + "type default description parents hp mp regen skills armor tools delete save exit";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        EditorPrompt common = commonActions(context, command);
        if (common != null) return common;

        Role role = EditorState.getSelectedRole(context);
        final Role.Builder builder = Role.builder(plugin).copyOf(role);

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
                final boolean primary = role.getType() == Role.RoleType.PRIMARY;

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
                if (role.getType() == Role.RoleType.ADDITIONAL) {
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

        if (role.getType() == Role.RoleType.PRIMARY) {
            // TODO fix maxlevel=50 assumption
            sendMessage(context, "%sHP: %s %sMP: %s %sMP Regen: %s [Level 1-50]",
                    ChatColor.DARK_RED, Utilities.minMaxString(role.getMaxHealthAtZero(), role.getMaxHealthAtLevel(50), ChatColor.RED),
                    ChatColor.DARK_BLUE, Utilities.minMaxString(role.getMaxManaAtZero(), role.getMaxManaAtLevel(50), ChatColor.BLUE),
                    ChatColor.DARK_AQUA, Utilities.minMaxString(role.getManaRegenAtZero(), role.getManaRegenAtLevel(50), ChatColor.AQUA));
        }

        sendMessage(context, "%s%d%s Skills", ChatColor.YELLOW, role.getAllSkills().size(), ChatColor.GREEN);
    }
}
