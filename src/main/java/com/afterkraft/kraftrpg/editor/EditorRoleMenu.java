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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleType;

public class EditorRoleMenu extends EditorPrompt {

    @Override
    public String getName(ConversationContext context) {
        return "roles";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        EditorPrompt common = commonActions(context, command);
        if (common != null) return common;

        for (Map.Entry<String, Role> entry : plugin.getRoleManager().getRolesByType(getFilter(context)).entrySet()) {
            if (command.equalsIgnoreCase(entry.getKey())) {
                EditorState.setSelectedRole(context, entry.getValue());
                return callPrompt(context, new EditorRoleFocus());
            }
        }

        if (command.equals("0") || command.equals("new")) {
            context.setSessionData("role.new.stage", EditorRoleNew.Stage.CHOOSE_NAME);
            return callPrompt(context, new EditorRoleNew());
        }
        if (command.equals("1") || command.equals("none")) {
            setFilter(context, null);
            return this;
        }
        if (command.equals("2") || command.equals("primary")) {
            setFilter(context, RoleType.PRIMARY);
            return this;
        }
        if (command.equals("3") || command.equals("secondary")) {
            setFilter(context, RoleType.SECONDARY);
            return this;
        }
        if (command.equals("4") || command.equals("extra")) {
            setFilter(context, RoleType.ADDITIONAL);
            return this;
        }

        sendMessage(context, ChatColor.RED + "Not a role or command. Please pick a role.");
        return null;
    }

    @Override
    public void printBanner(ConversationContext context) {
        sendMessage(context, ChatColor.DARK_GREEN + "KraftRPG Configuration Editor: Select Role");
        StringBuilder sb;
        sb = new StringBuilder(ChatColor.GREEN.toString());
        sb.append(getFilter(context) == null ? "All" : StringUtils.capitalize(getFilter(context).toString().toLowerCase()));
        sb.append(" Roles:");
        sendMessage(context, sb.toString());
        sb = new StringBuilder(ChatColor.AQUA.toString());
        for (String roleName : plugin.getRoleManager().getRolesByType(getFilter(context)).keySet()) {
            sb.append(roleName).append(" ");
        }
        sendMessage(context, sb.toString());
        sendMessage(context, ChatColor.AQUA + "[0]" + ChatColor.DARK_GREEN + " Create new role");
        sendMessage(context, ChatColor.AQUA + "[1,2,3,4]" + ChatColor.DARK_GREEN + " Filter (None, Primary, Secondary, Additional)");
        if (EditorState.isDirty(context)) {
            sendMessage(context, ChatColor.GOLD + "* You have unsaved changes.");
        }
    }

    @Override
    public String getPrompt(ConversationContext context) {
        StringBuilder sb = new StringBuilder(getPathString(context));
        sb.append("[role] new exit ");
        RoleType filter = getFilter(context);
        if (filter != null) {
            sb.append("none ");
        }
        if (filter != RoleType.PRIMARY) {
            sb.append("primary ");
        }
        if (filter != RoleType.SECONDARY) {
            sb.append("secondary ");
        }
        if (filter != RoleType.ADDITIONAL) {
            sb.append("extra ");
        }
        return sb.toString();
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return new ArrayList<String>(KraftRPGPlugin.getInstance().getRoleManager().getRoles().keySet());
    }

    private RoleType getFilter(ConversationContext context) {
        return (RoleType) context.getSessionData("role.rolefilter");
    }

    private void setFilter(ConversationContext context, RoleType type) {
        context.setSessionData("role.rolefilter", type);
    }
}
