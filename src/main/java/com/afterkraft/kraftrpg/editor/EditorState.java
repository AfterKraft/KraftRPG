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

import org.bukkit.conversations.ConversationContext;

/**
 * This class provides a nicer interface into the key-value store of a
 * ConversationContext.
 *
 * Does not actually contain state.
 *
 * Instead, all of the methods take a ConversationContext.
 */
public final class EditorState {
    public static boolean isDirty(ConversationContext context) {
        return (Boolean) context.getSessionData("dirty");
    }

    public static void setDirty(ConversationContext context, boolean dirty) {
        context.setSessionData("dirty", dirty);
    }
}
