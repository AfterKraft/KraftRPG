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
package com.afterkraft.kraftrpg.compat.v1_7_R3.attributes;

import java.util.UUID;

import net.minecraft.server.v1_7_R3.AttributeModifier;

import com.afterkraft.kraftrpg.api.handler.EntityAttributeModifier;

public class RPGEntityAttributeModifier extends AttributeModifier implements EntityAttributeModifier {

    private double val;

    public RPGEntityAttributeModifier(UUID id, String name) {
        super(id, name, 0, 0);
    }

    public double getValue() {
        return d();
    }

    @Override
    public double d() {
        return val;
    }

    public void setValue(double val) {
        this.val = val;
    }

}
