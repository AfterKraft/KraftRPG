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
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleType;

public class EditorClassNew extends EditorPrompt {
    private static final List<String> trueValues = ImmutableList.of("y", "yes", "1", "t", "true");
    private static final List<String> falseValues = ImmutableList.of("n", "no", "0", "f", "false");

    @Override
    public String getName(ConversationContext context) {
        return "new";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        EditorPrompt common = commonActions(context, command);
        if (common != null) return common;

        switch (getStage(context)) {
            case CHOOSE_NAME:
                context.setSessionData("class.new.name", command);
                context.setSessionData("class.new.stage", Stage.CHOOSE_TYPE);
                return this;
            case CHOOSE_TYPE:
                RoleType rt = null;
                if (command.equalsIgnoreCase("extra")) {
                    rt = RoleType.ADDITIONAL;
                } else if (command.equalsIgnoreCase("class")) {
                    rt = RoleType.PRIMARY;
                } else if (command.equalsIgnoreCase("profession")) {
                    rt = RoleType.SECONDARY;
                } else {
                    try {
                        rt = RoleType.valueOf(command.toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (rt == null) {
                    sendMessage(context, ChatColor.RED + "Please answer with one of " + ChatColor.YELLOW + "'class', 'profession', or 'extra'.");
                    return null;
                }

                context.setSessionData("class.new.type", rt);

                context.setSessionData("class.new.default", false);
                if (rt == RoleType.PRIMARY && plugin.getRoleManager().getDefaultPrimaryRole() == null) {
                    context.setSessionData("class.new.stage", Stage.MAKE_DEFAULT);
                } else if (rt == RoleType.SECONDARY && plugin.getRoleManager().getDefaultSecondaryRole() == null) {
                    context.setSessionData("class.new.stage", Stage.MAKE_DEFAULT);
                } else if (rt == RoleType.ADDITIONAL) {
                    context.setSessionData("class.new.stage", Stage.MAKE_DEFAULT);
                } else {
                    context.setSessionData("class.new.stage", Stage.PICK_PARENT);
                }
                return this;
            case MAKE_DEFAULT:
                if (trueValues.contains(command.toLowerCase())) {
                    context.setSessionData("class.new.default", true);
                } else if (falseValues.contains(command.toLowerCase())) {
                    context.setSessionData("class.new.default", false);
                } else {
                    sendMessage(context, ChatColor.RED + "Please choose 'true' or 'false'.");
                    return null;
                }
                context.setSessionData("class.new.stage", Stage.PICK_PARENT);
                return this;
            case PICK_PARENT:
                if (trueValues.contains(command.toLowerCase())) {
                    context.setSessionData("class.new.parent", null);
                } else {
                    Role r = plugin.getRoleManager().getRole(command);
                    if (r == null) {
                        sendMessage(context, ChatColor.RED + "Please choose an existing role.");
                        return null;
                    }
                    context.setSessionData("class.new.parent", r);
                }

                return finish(context);
        }
        return null;
    }

    @Override
    public void printBanner(ConversationContext context) {
    }

    @Override
    public String getPrompt(ConversationContext context) {
        switch (getStage(context)) {
            case CHOOSE_NAME:
                return "What do you want to call the new class?";
            case CHOOSE_TYPE:
                return "Is this a class, profession, or extra role?";
            case MAKE_DEFAULT:
                RoleType rt = (RoleType) context.getSessionData("class.new.type");
                if (rt == RoleType.PRIMARY) {
                    return "You don't have a default primary class. Should this become the default class for new users?";
                } else if (rt == RoleType.SECONDARY) {
                    return "You don't have a default profession. Should this become the default profession for new users?";
                } else {
                    return "Should this additional role be included for new users?";
                }
            case PICK_PARENT:
                return "Should this class be immediately available, or require mastery of another?\nAnswer with \"yes\" or the class they need to master.";
        }
        return null;
    }

    private Stage getStage(ConversationContext context) {
        return (Stage) context.getSessionData("class.new.stage");
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return null;
    }

    private EditorPrompt finish(ConversationContext context) {
        String name = (String) context.getSessionData("class.new.name");
        RoleType type = (RoleType) context.getSessionData("class.new.type");
        boolean makeDefault = (Boolean) context.getSessionData("class.new.default");
        Role parent = (Role) context.getSessionData("class.new.parent");

        Map<Object, Object> map = context.getAllSessionData();
        map.remove("class.new.stage");
        map.remove("class.new.name");
        map.remove("class.new.type");
        map.remove("class.new.default");
        map.remove("class.new.parent");

        Role r = new Role(plugin, name, type);
        plugin.getRoleManager().addRole(r);
        if (makeDefault) {
            switch (type) {
                case PRIMARY:
                    plugin.getRoleManager().setDefaultPrimaryRole(r);
                    break;
                case SECONDARY:
                    plugin.getRoleManager().setDefaultSecondaryRole(r);
                    break;
                case ADDITIONAL:
                    throw new UnsupportedOperationException("TODO");
            }
        }

        // TODO apply parent

        EditorState.setSelectedRole(context, r);
        return new EditorClassFocus();
    }

    enum Stage {
        CHOOSE_NAME,
        CHOOSE_TYPE,
        MAKE_DEFAULT,
        PICK_PARENT,
    }
}
