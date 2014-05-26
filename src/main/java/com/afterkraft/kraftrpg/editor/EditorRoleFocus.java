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

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleType;
import com.afterkraft.kraftrpg.api.util.Utilities;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

public class EditorRoleFocus extends EditorPrompt {
    @Override
    public String getName(ConversationContext context) {
        return EditorState.getSelectedRole(context).getName();
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("name", "type", "default", "description", "parents", "hp", "mp", "regen", "skills", "armor", "tools", "delete");
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        EditorPrompt common = commonActions(context, command);
        if (common != null) return common;

        final Role role = EditorState.getSelectedRole(context);

        if (command.equals("description")) {
            return callPrompt(context, new PromptGetString(
                    "Choose a new description. & for color codes. Don't use a semicolon.", false) {
                @Override
                public boolean apply(String input) {
                    role.setDescription(ChatColor.translateAlternateColorCodes('&', input));
                    return true;
                }
            });
        } else if (command.equals("type")) {
            return callPrompt(context, )
        }
        /*
        Name Type Default Description
        Parents hp/mp/regen
        Skills
        Armor/tools
        delete
         */

        return null;
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return getPathString(context) + "name type description parents hp mp regen skills armor tools delete save exit";
    }

    @Override
    public void printBanner(ConversationContext context) {
        sendMessage(context, "%sKraftRPG Configuration Editor: %sRole Detail", ChatColor.DARK_GREEN, ChatColor.BLUE);

        Role r = EditorState.getSelectedRole(context);
        boolean def = false;
        switch (r.getType()) {
            case PRIMARY:
                def = r == plugin.getRoleManager().getDefaultPrimaryRole();
                break;
            case SECONDARY:
                def = r == plugin.getRoleManager().getDefaultSecondaryRole();
                break;
            case ADDITIONAL:
                break;
        }
        sendMessage(context, "%sRole: %s%s%s Type: %s%s%s %s",
                ChatColor.GREEN,
                ChatColor.GOLD, r.getName(), ChatColor.GREEN,
                ChatColor.AQUA, StringUtils.capitalize(r.getType().toString().toLowerCase()), ChatColor.GREEN,
                def ? ChatColor.YELLOW + "(default)" : "");

        StringBuilder sb;
        sb = new StringBuilder(ChatColor.GREEN.toString()).append("Parents: ");
        if (r.getParents().isEmpty()) {
            sb.append(ChatColor.GRAY.toString()).append("(none)");
        }
        for (Role parent : r.getParents()) {
            sb.append(ChatColor.GOLD.toString());
            sb.append(parent.getName());
            sb.append(ChatColor.YELLOW.toString());
            sb.append(" (").append(parent.getAdvancementLevel()).append(") ");
        }
        sendMessage(context, sb.toString());

        sendMessage(context, "%sDesc: %s\"%s%s\"",
                ChatColor.GREEN,
                ChatColor.WHITE, ChatColor.ITALIC, r.getDescription(), ChatColor.WHITE);

        if (r.getType() == RoleType.PRIMARY) {
            // TODO fix maxlevel=50 assumption
            sendMessage(context, "%sHP: %s %sMP: %s %sMP Regen: %s [Level 1-50]",
                    ChatColor.DARK_RED, Utilities.minMaxString(r.getHpAt0(), r.getHp(50), ChatColor.RED),
                    ChatColor.DARK_BLUE, Utilities.minMaxString(r.getMpAt0(), r.getMp(50), ChatColor.BLUE),
                    ChatColor.DARK_AQUA, Utilities.minMaxString(r.getMpRegenAt0(), r.getMpRegen(50), ChatColor.AQUA));
        }

        sendMessage(context, "%s%d%s Skills", ChatColor.YELLOW, r.getAllSkills().size(), ChatColor.GREEN);
    }
}
