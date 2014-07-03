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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.roles.Role;

public class EditorRoleFocusType extends EditorPrompt {
    private static final Map<String, Role.RoleType> roleNameMap;

    static {
        roleNameMap = new HashMap<String, Role.RoleType>();
        for (Role.RoleType elem : Role.RoleType.values()) {
            roleNameMap.put(elem.toString().toLowerCase(), elem);
        }
        roleNameMap.put("class", Role.RoleType.PRIMARY);
        roleNameMap.put("profession", Role.RoleType.SECONDARY);
        roleNameMap.put("extra", Role.RoleType.ADDITIONAL);
    }

    @Override
    public String getName(ConversationContext context) {
        return "type";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.equals("!cancel") || command.equals("stop") || command.equals("quit")) {
            sendMessage(context, "Input cancelled.");
            return returnPrompt(context);
        } else {
            Role.RoleType type = roleNameMap.get(command);
            if (type == null) {
                return null;
            }
            Role role = EditorState.getSelectedRole(context);

            boolean chooseNewDefault = false;
            switch (role.getType()) {
                case PRIMARY:
                    chooseNewDefault = plugin.getRoleManager().getDefaultPrimaryRole() == role;
                    break;
                case SECONDARY:
                    chooseNewDefault = plugin.getRoleManager().getDefaultSecondaryRole() == role;
                    break;
            }

            if (chooseNewDefault) {
                // TODO pick a new default here, instead of error message
                // using PromptGetRole
                sendMessage(context, "You cannot change the type of the currently default role!");
                return null;
            }

            EditorState.setSelectedRole(context, Role.Builder.copyOf(role).setType(type).build());
            return returnPrompt(context);
        }
    }

    @Override
    public void printBanner(ConversationContext context) {
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return "Choose the new type for this role (primary, secondary, extra).";
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.copyOf(roleNameMap.keySet());
    }
}
