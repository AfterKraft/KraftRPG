package com.afterkraft.kraftrpg.editor;

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleType;
import com.google.common.collect.ImmutableList;
import org.bukkit.conversations.ConversationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorRoleFocusType extends EditorPrompt {
    private static final Map<String, RoleType> roleNameMap;

    static {
        roleNameMap = new HashMap<String, RoleType>();
        for (RoleType elem : RoleType.values()) {
            roleNameMap.put(elem.toString().toLowerCase(), elem);
        }
        roleNameMap.put("class", RoleType.PRIMARY);
        roleNameMap.put("profession", RoleType.SECONDARY);
        roleNameMap.put("extra", RoleType.ADDITIONAL);
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.copyOf(roleNameMap.keySet());
    }

    @Override
    public String getName(ConversationContext context) {
        return "type";
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return "Choose the new type for this role (primary, secondary, extra).";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.equals("!cancel") || command.equals("stop") || command.equals("quit")) {
            sendMessage(context, "Input cancelled.");
            return returnPrompt(context);
        } else {
            RoleType type = roleNameMap.get(command);
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

            role.setType(type);
            return returnPrompt(context);
        }
    }

    @Override
    public void printBanner(ConversationContext context) {
    }
}
