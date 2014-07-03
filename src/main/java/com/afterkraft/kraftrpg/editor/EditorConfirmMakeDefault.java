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

import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.api.roles.Role;

public class EditorConfirmMakeDefault extends EditorPrompt {
    @Override
    public String getName(ConversationContext context) {
        return "default";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        return null;
    }

    @Override
    public void printBanner(ConversationContext context) {

    }

    @Override
    public String getPrompt(ConversationContext context) {
        Role newDefault = EditorState.getSelectedRole(context);
        boolean primary = newDefault.getType() == Role.RoleType.PRIMARY;
        return null;
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("confirm", "cancel");
    }
}
