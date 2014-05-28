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

import com.google.common.collect.ImmutableList;
import org.bukkit.conversations.ConversationContext;

import java.util.List;

public abstract class PromptGetString extends EditorPrompt {
    private final String prompt;
    private final boolean emptyAllowed;

    public PromptGetString(String prompt, boolean allowEmpty) {
        this.prompt = prompt;
        this.emptyAllowed = allowEmpty;
    }

    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of();
    }

    public String getName(ConversationContext context) {
        return "input";
    }

    public String getPrompt(ConversationContext context) {
        return prompt;
    }

    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.trim().isEmpty()) {
            if (emptyAllowed) {
                if (apply(null)) {
                    return returnPrompt(context);
                } else {
                    return null;
                }
            } else {
                // Syntax error
                return null;
            }
        } else if (command.equals("!cancel")) {
            sendMessage(context, "Input cancelled.");
            return returnPrompt(context);
        } else {
            if (apply(command)) {
                return returnPrompt(context);
            } else {
                return null;
            }
        }
    }

    public void printBanner(ConversationContext context) {
        sendMessage(context, "To cancel, type '!cancel'.");
    }

    /**
     * Apply the gotten string.
     *
     * @param input user input string, or null if empty allowed and empty given
     * @return True if input OK, false if retry needed
     */
    public abstract boolean apply(String input);
}
