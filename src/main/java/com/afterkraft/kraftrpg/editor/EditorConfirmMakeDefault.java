package com.afterkraft.kraftrpg.editor;

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleType;
import com.google.common.collect.ImmutableList;
import org.bukkit.conversations.ConversationContext;

import java.util.List;

public class EditorConfirmMakeDefault extends EditorPrompt {
    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("confirm", "cancel");
    }

    @Override
    public String getName(ConversationContext context) {
        return "default";
    }

    @Override
    public String getPrompt(ConversationContext context) {
        Role newDefault = EditorState.getSelectedRole(context);
        boolean primary = newDefault.getType() == RoleType.PRIMARY;
        return null;
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        return null;
    }

    @Override
    public void printBanner(ConversationContext context) {

    }
}
