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
package com.afterkraft.kraftrpg.compat.v1_7_R3;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.v1_7_R3.AttributeInstance;
import net.minecraft.server.v1_7_R3.AttributeRanged;
import net.minecraft.server.v1_7_R3.DamageSource;
import net.minecraft.server.v1_7_R3.EntityGolem;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.EntityMonster;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.IAttribute;
import net.minecraft.server.v1_7_R3.MobEffect;
import net.minecraft.server.v1_7_R3.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_7_R3.PacketPlayOutRemoveEntityEffect;
import net.minecraft.server.v1_7_R3.PacketPlayOutWorldParticles;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.conversations.ConversationTracker;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemFactory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Giant;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.handler.EntityAttributeType;
import com.afterkraft.kraftrpg.api.handler.ItemAttributeType;
import com.afterkraft.kraftrpg.api.util.FixedPoint;


public class RPGHandler extends CraftBukkitHandler {
    private static final Map<String, PotionEffectType> otherPotionEffectNames = new HashMap<String, PotionEffectType>();
    static {
        otherPotionEffectNames.put("nausea", PotionEffectType.CONFUSION);
        otherPotionEffectNames.put("resistance", PotionEffectType.DAMAGE_RESISTANCE);
        otherPotionEffectNames.put("haste", PotionEffectType.FAST_DIGGING);
        otherPotionEffectNames.put("instant_damage", PotionEffectType.HARM);
        otherPotionEffectNames.put("instant_health", PotionEffectType.HEAL);
        otherPotionEffectNames.put("strength", PotionEffectType.INCREASE_DAMAGE);
        otherPotionEffectNames.put("jump_boost", PotionEffectType.JUMP);
        otherPotionEffectNames.put("slowness", PotionEffectType.SLOW);
        otherPotionEffectNames.put("fatigue", PotionEffectType.SLOW_DIGGING);
        otherPotionEffectNames.put("mining_fatigue", PotionEffectType.SLOW_DIGGING);
    }
    private Field ldbpt;
    private Field conversationTracker, conversationQueue, currentPrompt;
    private Field modifiersField;
    private Random random;
    private boolean listenersLoaded = false;
    private EnumMap<EntityAttributeType, IAttribute> iattrMap;

    public RPGHandler(ServerType type) {
        super(type);
        serverType = type;
        this.plugin = KraftRPGPlugin.getInstance();
        try {
            try {
                this.ldbpt = EntityLiving.class.getDeclaredField("lastDamageByPlayerTime");
                this.ldbpt.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                this.conversationTracker = CraftPlayer.class.getDeclaredField("conversationTracker");
                this.conversationTracker.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                this.conversationQueue = ConversationTracker.class.getDeclaredField("conversationQueue");
                this.conversationQueue.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                this.currentPrompt = Conversation.class.getDeclaredField("currentPrompt");
                this.currentPrompt.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                this.modifiersField = Field.class.getDeclaredField("modifiers");
                this.modifiersField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } catch (SecurityException ignored) {
            // do nothing
        }

        this.random = new Random();

        this.iattrMap = new EnumMap<EntityAttributeType, IAttribute>(EntityAttributeType.class);

        for (EntityAttributeType eat : EntityAttributeType.values()) {
            this.iattrMap.put(eat, new AttributeRanged(eat.getIdentifier(), UNSET_VALUE, eat.getMin(), eat.getMax()));
        }
    }

    @Override
    public void loadExtraListeners() {
        if (!this.listenersLoaded) {
            switch (serverType) {
                case TWEAKKIT:
                    this.plugin.getListenerManager().addListener(new TweakkitListener(this.plugin));
                case SPIGOT:
                    this.plugin.getListenerManager().addListener(new SpigotListener(this.plugin));
                    break;
                case BUKKIT:
                    break;
            }
            this.listenersLoaded = true;
        }
    }

    @Override
    public Location getSpawnLocation(LivingEntity entity) {
        double spawnx = entity.getLocation().getX();
        double spawny = entity.getLocation().getY();
        double spawnz = entity.getLocation().getZ();
        switch (serverType) {
            case BUKKIT:
            case SPIGOT:
                spawnx = getOrSetAttribute(entity, EntityAttributeType.SPAWNX, spawnx);
                spawny = getOrSetAttribute(entity, EntityAttributeType.SPAWNY, spawny);
                spawnz = getOrSetAttribute(entity, EntityAttributeType.SPAWNZ, spawnz);
                break;
            case TWEAKKIT:
                spawnx = TweakkitHelper.getEntityData(this.plugin, entity, SPAWNX_STRING, spawnx);
                spawny = TweakkitHelper.getEntityData(this.plugin, entity, SPAWNY_STRING, spawny);
                spawnz = TweakkitHelper.getEntityData(this.plugin, entity, SPAWNZ_STRING, spawnz);
                break;

        }

        return new Location(entity.getWorld(), spawnx, spawny, spawnz);
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getSpawnReason(LivingEntity entity, SpawnReason provided) {
        int ordinal = 3;
        if (provided != null) {
            ordinal = provided.ordinal();
        }
        switch (serverType) {
            case BUKKIT:
            case SPIGOT:
                ordinal = (int) getOrSetAttribute(entity, EntityAttributeType.SPAWNREASON, ordinal);
                break;
            case TWEAKKIT:
                ordinal = TweakkitHelper.getEntityData(this.plugin, entity, SPAWNREASON_STRING, ordinal);
                break;
        }
        CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason.CHUNK_GEN;
        try {
            reason = CreatureSpawnEvent.SpawnReason.values()[ordinal];
        } catch (Exception e) {
            // TODO: surface this better?
            this.plugin.debugLog(Level.WARNING, "There was an issue with loading a Monster's spawn reason! Please report this to the developer!");
        }
        return reason;
    }

    @Override
    public FixedPoint getMonsterExperience(LivingEntity entity, FixedPoint value) {
        switch (serverType) {
            case TWEAKKIT:
                return FixedPoint.valueOf(TweakkitHelper.getEntityData(this.plugin, entity, EXPERIENCE_STRING, value.doubleValue()));
            default:
            case BUKKIT:
            case SPIGOT:
                return FixedPoint.valueOf(getOrSetAttribute(entity, EntityAttributeType.EXPERIENCE, value.doubleValue()));
        }
    }

    @Override
    public void setMonsterExperience(LivingEntity entity, final FixedPoint experience) {
        if (entity == null) {
            return;
        }
        switch (serverType) {
            case BUKKIT:
            case SPIGOT:
                setAttribute(entity, EntityAttributeType.EXPERIENCE, experience.doubleValue());
                break;
            case TWEAKKIT:
                TweakkitHelper.getEntityData(this.plugin, entity, EXPERIENCE_STRING, experience.doubleValue());
                break;
        }
    }

    @Override
    public double getEntityDamage(LivingEntity entity, double calculated) {
        double value = 0;
        switch (serverType) {
            case BUKKIT:
            case SPIGOT:
                value = getOrSetAttribute(entity, EntityAttributeType.DAMAGE, calculated);
                break;
            case TWEAKKIT:
                value = TweakkitHelper.getEntityData(this.plugin, entity, DAMAGE_STRING, calculated);
                break;
        }
        return value;
    }

    @Override
    public boolean isAttributeSet(LivingEntity entity, EntityAttributeType type) {
        EntityLiving entityLiving = ((CraftLivingEntity) entity).getHandle();
        AttributeInstance instance = entityLiving.getAttributeInstance(this.iattrMap.get(type));

        return instance != null && instance.getValue() != UNSET_VALUE;
    }

    @Override
    public double getAttribute(LivingEntity entity, EntityAttributeType type, double defaultValue) {
        EntityLiving entityLiving = ((CraftLivingEntity) entity).getHandle();
        AttributeInstance instance = entityLiving.getAttributeInstance(this.iattrMap.get(type));

        if (instance == null || instance.getValue() == UNSET_VALUE) {
            return defaultValue;
        }

        return instance.getValue();
    }

    @Override
    public double setAttribute(LivingEntity entity, EntityAttributeType type, double newValue) {
        EntityLiving entityLiving = ((CraftLivingEntity) entity).getHandle();
        AttributeInstance instance = entityLiving.getAttributeInstance(this.iattrMap.get(type));

        if (instance == null) {
            instance = entityLiving.bb().b(this.iattrMap.get(type)); // should be getAttributeMap().setup()
            instance.setValue(newValue);
            return UNSET_VALUE;
        } else {
            double old = instance.getValue();
            instance.setValue(newValue);
            return old;
        }
    }

    @Override
    public double getOrSetAttribute(LivingEntity entity, EntityAttributeType type, double valueIfEmpty) {
        EntityLiving entityLiving = ((CraftLivingEntity) entity).getHandle();
        AttributeInstance instance = entityLiving.getAttributeInstance(this.iattrMap.get(type));

        if (instance == null) {
            instance = entityLiving.bb().b(this.iattrMap.get(type)); // should be getAttributeMap().setup()
            instance.setValue(valueIfEmpty);
            return valueIfEmpty;
        } else if (instance.getValue() == UNSET_VALUE) {
            instance.setValue(valueIfEmpty);
            return valueIfEmpty;
        } else {
            return instance.getValue();
        }
    }

    @Override
    public final double getPostArmorDamage(LivingEntity defender, double damage) {
        int i = 25 - ((CraftLivingEntity) defender).getHandle().aU();
        float f1 = (float) damage * (float) i;
        return f1 / 25;
    }

    @Override
    public final double getPostArmorDamage(Insentient being, EntityDamageEvent event, double damage) {
        if (being.getEntity() instanceof CraftLivingEntity) {
            int i = 25 - ((CraftLivingEntity) being.getEntity()).getHandle().aU();
            float f1 = (float) damage * (float) i;
            return f1 / 25;
        }
        return damage;
    }

    @Override
    public void setPlayerExpZero(Player player) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.exp = 0;
        entityPlayer.expTotal = 0;
        entityPlayer.expLevel = 0;
    }

    @Override
    public void modifyArrowDamage(Arrow arrow, double damage) {
        switch (serverType) {
            case BUKKIT:
                ((CraftArrow) arrow).getHandle().b(damage);
                break;
            case TWEAKKIT:
            case SPIGOT:
                arrow.spigot().setDamage(damage);
                break;
        }
    }

    @Override
    public boolean damageEntity(LivingEntity target, LivingEntity attacker, double damage, DamageCause cause, boolean knockback) {
        if (target == null || attacker == null) { // We have to consider that Insentient.getEntity() may return null
            return false;
        }
        if (target.isDead() || (target.getHealth() <= 0)) {
            return false;
        }
        //Do it ourselves cause bukkit is stubborn
        int originalNoDamageTicks = target.getNoDamageTicks();
        target.setNoDamageTicks(0);


        final EntityDamageByEntityEvent edbe = new EntityDamageByEntityEvent(attacker, target, cause, damage);
        Bukkit.getServer().getPluginManager().callEvent(edbe);
        if (edbe.isCancelled()) {
            return false;
        }

        target.setLastDamageCause(edbe);
        final double oldHealth = target.getHealth();
        double newHealth = oldHealth - edbe.getDamage();
        if (newHealth < 0) {
            newHealth = 0;
        }
        final EntityLiving el = ((CraftLivingEntity) target).getHandle();
        el.lastDamage = (float) edbe.getDamage();
        el.aw = (float) oldHealth;
        el.hurtTicks = el.ay = 10;
        el.az = 0.0F;
        if (knockback) {
            knockBack(target, attacker, edbe.getDamage());
        }
        el.world.broadcastEntityEffect(el, (byte) 2);

        el.lastDamager = ((CraftLivingEntity) attacker).getHandle();

        // Set last damage by player time via reflection.
        if (attacker instanceof Player) {
            try {
                this.ldbpt.set(el, 60);
            } catch (final IllegalArgumentException e) {
                // do nothing
            } catch (final IllegalAccessException e) {
                // do nothing
            }
        }
        el.setHealth((float) newHealth);
        //If the target would die, kill it!
        if (newHealth <= 0) {
            el.world.makeSound(el, getSoundName(target.getType()), 1.0f, getSoundStrength(target));

            if (attacker instanceof Player) {
                final EntityPlayer p = ((CraftPlayer) attacker).getHandle();
                el.killer = p;
                el.die(DamageSource.playerAttack(p));
            } else {
                final EntityLiving att = ((CraftLivingEntity) attacker).getHandle();
                el.die(DamageSource.mobAttack(att));
            }
        } else {
            target.setNoDamageTicks(originalNoDamageTicks);

            final EntityLiving attackEntity = ((CraftLivingEntity) attacker).getHandle();
            if (target instanceof org.bukkit.entity.Monster) {
                if ((target instanceof Blaze) || (target instanceof Enderman) || (target instanceof Spider) || (target instanceof Giant) || (target instanceof Silverfish)) {
                    final EntityMonster em = (EntityMonster) el;
                    final EntityTargetEvent event = CraftEventFactory.callEntityTargetEvent(em, attackEntity, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY);
                    if (!event.isCancelled()) {
                        em.setTarget(attackEntity);
                    }
                }
            } else if (target instanceof IronGolem) {
                final EntityGolem eg = (EntityGolem) el;
                eg.setTarget(((CraftLivingEntity) attacker).getHandle());
            } else if ((target instanceof Wolf) && (((Wolf) target).getTarget() == null)) {
                final Wolf wolf = (Wolf) target;
                wolf.setAngry(true);
                wolf.setTarget(attacker);
            }
            if (target instanceof PigZombie) {
                ((PigZombie) target).setAngry(true);
            }

        }
        return true;
    }

    @Override
    public void knockBack(LivingEntity target, LivingEntity attacker, double damage) {
        final EntityLiving el = ((CraftLivingEntity) target).getHandle();
        final EntityLiving aEL = ((CraftLivingEntity) attacker).getHandle();
        el.velocityChanged = true;
        double d0 = aEL.locX - el.locX;
        double d1;

        for (d1 = aEL.locZ - el.locZ; ((d0 * d0) + (d1 * d1)) < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
            d0 = (Math.random() - Math.random()) * 0.01D;
        }

        el.az = (float) ((Math.atan2(d1, d0) * 180.0D) / 3.1415927410125732D) - el.yaw;
        el.a(aEL, (float) damage, d0, d1);
    }

    @Override
    public void refreshLastPlayerDamageTime(LivingEntity entity) {
        try {
            this.ldbpt.set(((CraftLivingEntity) entity).getHandle(), 60);
        } catch (final IllegalArgumentException e) {
            // do nothing
        } catch (final IllegalAccessException e) {
            // do nothing
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendFakePotionEffectPacket(PotionEffect effect, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
        ePlayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendFakePotionEffectPackets(Set<PotionEffect> effects, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
        for (PotionEffect effect : effects) {
            ePlayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFakePotionEffectPacket(PotionEffect effect, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
        ePlayer.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFakePotionEffectPackets(Set<PotionEffect> effects, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
        for (PotionEffect effect : effects) {
            ePlayer.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
        }
    }

    @Override
    public void setArrowDamage(Arrow arrow, double damage) {
        ((CraftArrow) arrow).getHandle().b(damage);
    }

    @Override
    protected float getSoundStrength(LivingEntity entity) {
        EntityLiving el = ((CraftLivingEntity) entity).getHandle();
        return el.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F) + 1.5F : ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F) + 1.0F;
    }

    @Override
    public void playClientEffect(Player player, Location startLocation, String particle, Vector offset, float speed, int count, boolean sendToAll) {
        if (!(player instanceof CraftPlayer)) {
            throw new IllegalArgumentException("The provided player is NOT a CraftPlayer!");
        }
        try {
            PacketPlayOutWorldParticles clientEffectPacket = new PacketPlayOutWorldParticles(particle, (float) startLocation.getX(), (float) (startLocation.getY() + 0.5), (float) startLocation.getZ(), (float) (offset.getX() + 0.5), (float) (offset.getY() + 0.3), (float) (offset.getZ() + 0.5), speed, count);

            if (sendToAll) {
                ((CraftWorld) startLocation.getWorld()).getHandle().getTracker().sendPacketToEntity(((CraftPlayer) player).getHandle(), clientEffectPacket);
            } else {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(clientEffectPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Conversation getCurrentConversation(Player player) {
        if (!CraftPlayer.class.isInstance(player)) {
            return null;
        }

        try {
            ConversationTracker tracker = (ConversationTracker) this.conversationTracker.get(player);
            LinkedList<?> list = (LinkedList) this.conversationQueue.get(tracker);
            return (Conversation) list.getFirst();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Prompt getCurrentPrompt(Conversation conversation) {
        try {
            return (Prompt) this.currentPrompt.get(conversation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addNBTAttributes() {
        try {
            Field f = CraftItemFactory.class.getDeclaredField("KNOWN_NBT_ATTRIBUTE_NAMES");
            f.setAccessible(true);
            this.modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            Set<?> oldset = (Set<?>) f.get(null);
            HashSet<Object> newset = new HashSet<Object>(oldset);
            for (ItemAttributeType type : ItemAttributeType.values()) {
                newset.add(type.getAttributeName());
            }

            f.set(null, newset);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getArmorIndexHelmet() {
        return 3;
    }

    @Override
    public int getArmorIndexChestPlate() {
        return 2;
    }

    @Override
    public int getArmorIndexLeggings() {
        return 1;
    }

    @Override
    public int getArmorIndexBoots() {
        return 0;
    }

    @Override
    public Map<String, PotionEffectType> getAlternatePotionEffectNames() {
        return ImmutableMap.copyOf(otherPotionEffectNames);
    }

}
