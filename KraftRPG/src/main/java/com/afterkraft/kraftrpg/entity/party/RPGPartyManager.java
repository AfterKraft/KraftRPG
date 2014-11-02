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
package com.afterkraft.kraftrpg.entity.party;

import org.apache.commons.lang.Validate;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.PartyMember;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.entity.party.PartyManager;

/**
 * Default implementation of PartyManager
 */
public class RPGPartyManager implements PartyManager {

    private final KraftRPGPlugin plugin;

    public RPGPartyManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {

        // TODO Implement
    }

    @Override
    public void shutdown() {
        // TODO Implement

    }

    @Override
    public Party createParty(PartyMember partyLeader, PartyMember... members) {
        Validate.notNull(partyLeader, "Cannot create a party with a null leader!");
        // TODO Implement
        return null;
    }

    @Override
    public boolean isFriendly(PartyMember a, PartyMember b) {
        if (!a.hasParty() || !b.hasParty()) {
            return false;
        }
        // TODO Implement

        return false;
    }

    @Override
    public boolean isEnemy(PartyMember a, PartyMember b) {
        // TODO Implement
        return false;
    }
}
