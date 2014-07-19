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
package com.afterkraft.kraftrpg.entity.party;

import org.apache.commons.lang.Validate;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.PartyMember;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.entity.party.PartyManager;

public class RPGPartyManager implements PartyManager {

    private final RPGPlugin plugin;

    public RPGPartyManager(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public Party createParty(PartyMember partyLeader, PartyMember... members) {
        Validate.notNull(partyLeader, "Cannot create a party with a null leader!");
        return null;
    }

    @Override
    public boolean isFriendly(PartyMember a, PartyMember b) {
        if (!a.hasParty() || !b.hasParty()) return false;

        return false;
    }

    @Override
    public boolean isEnemy(PartyMember a, PartyMember b) {
        return false;
    }
}
