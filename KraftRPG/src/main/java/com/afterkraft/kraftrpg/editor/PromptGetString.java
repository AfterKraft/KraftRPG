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

import org.bukkit.conversations.ConversationContext;

import com.google.common.collect.ImmutableList;

/**
 * TODO Add documentation
 */
public abstract class PromptGetString extends EditorPrompt {
    private final String prompt;
    private final boolean emptyAllowed;

    protected PromptGetString(String prompt, boolean allowEmpty) {
        this.prompt = prompt;
        this.emptyAllowed = allowEmpty;
    }

    @Override
    public String getName(ConversationContext context) {
        return "input";
    }

    @Override
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

    @Override
    public void printBanner(ConversationContext context) {
        sendMessage(context, "To cancel, type '!cancel'.");
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return prompt;
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of();
    }

    /**
     * Apply the gotten string.
     *
     * @param input user input string, or null if empty allowed and empty given
     *
     * @return True if input OK, false if retry needed
     */
    public abstract boolean apply(String input);
}
