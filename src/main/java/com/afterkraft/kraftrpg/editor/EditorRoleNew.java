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

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;

public class EditorRoleNew extends EditorPrompt {
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
                context.setSessionData("role.new.name", command);
                context.setSessionData("role.new.stage", Stage.CHOOSE_TYPE);
                return this;
            case CHOOSE_TYPE:
                Role.RoleType rt = null;
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

                context.setSessionData("role.new.type", rt);

                context.setSessionData("role.new.default", false);
                if (rt == RoleType.PRIMARY && plugin.getRoleManager().getDefaultPrimaryRole() == null) {
                    context.setSessionData("role.new.stage", Stage.MAKE_DEFAULT);
                } else if (rt == RoleType.SECONDARY && plugin.getRoleManager().getDefaultSecondaryRole() == null) {
                    context.setSessionData("role.new.stage", Stage.MAKE_DEFAULT);
                    // } else if (rt == RoleType.ADDITIONAL) {
                    //     context.setSessionData("role.new.stage", Stage.MAKE_DEFAULT);
                } else {
                    context.setSessionData("role.new.stage", Stage.PICK_PARENT);
                }
                return this;
            case MAKE_DEFAULT:
                if (trueValues.contains(command.toLowerCase())) {
                    context.setSessionData("role.new.default", true);
                } else if (falseValues.contains(command.toLowerCase())) {
                    context.setSessionData("role.new.default", false);
                } else {
                    sendMessage(context, ChatColor.RED + "Please choose 'true' or 'false'.");
                    return null;
                }
                context.setSessionData("role.new.stage", Stage.PICK_PARENT);
                return this;
            case PICK_PARENT:
                if (trueValues.contains(command.toLowerCase())) {
                    context.setSessionData("role.new.parent", null);
                } else {
                    Role r = plugin.getRoleManager().getRole(command);
                    if (r == null) {
                        sendMessage(context, ChatColor.RED + "Please choose an existing role.");
                        return null;
                    }
                    context.setSessionData("role.new.parent", r);
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
                return "What do you want to call the new role?";
            case CHOOSE_TYPE:
                return "Is this a class, profession, or extra role?";
            case MAKE_DEFAULT:
                Role.RoleType rt = (Role.RoleType) context.getSessionData("role.new.type");
                if (rt == RoleType.PRIMARY) {
                    return "You don't have a default primary class. Should this become the default class for new users?";
                } else if (rt == RoleType.SECONDARY) {
                    return "You don't have a default profession. Should this become the default profession for new users?";
                } else {
                    throw new UnsupportedOperationException();
                    // return "Should this additional role be included for new users?";
                }
            case PICK_PARENT:
                return "Should this role be immediately available, or require mastery of another?\nAnswer with \"yes\" or the role they need to master.";
        }
        return null;
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return null;
    }

    private Stage getStage(ConversationContext context) {
        return (Stage) context.getSessionData("role.new.stage");
    }

    private EditorPrompt finish(ConversationContext context) {
        String name = (String) context.getSessionData("role.new.name");
        final Role.RoleType type = (Role.RoleType) context.getSessionData("role.new.type");
        final boolean makeDefault = (Boolean) context.getSessionData("role.new.default");
        final Role parent = (Role) context.getSessionData("role.new.parent");
        context.setSessionData("role.new.stage", null);
        context.setSessionData("role.new.name", null);
        context.setSessionData("role.new.type", null);
        context.setSessionData("role.new.default", null);
        context.setSessionData("role.new.parent", null);

        final Role r = Role.builder(plugin)
                .setName(name)
                .setType(type)
                .addParent(parent)
                .build();

        // Implicit commit
        EditorState.commit(context);

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
                    throw new UnsupportedOperationException();
            }
        }
        if (parent != null) {
            plugin.getRoleManager().addRoleDependency(parent, r);
        }

        EditorState.setSelectedRole(context, r);
        return new EditorRoleFocus();
    }

    enum Stage {
        CHOOSE_NAME,
        CHOOSE_TYPE,
        MAKE_DEFAULT,
        PICK_PARENT,
    }
}
