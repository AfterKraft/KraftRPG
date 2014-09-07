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
package com.afterkraft.kraftrpg.compat.v1_7_R4;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;

import net.minecraft.server.v1_7_R4.*;

import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.conversations.ConversationTracker;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemFactory;

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
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.events.entity.damage.InsentientDamageEvent;
import com.afterkraft.kraftrpg.api.events.entity.damage.InsentientDamageInsentientEvent;
import com.afterkraft.kraftrpg.api.events.entity.damage.SkillDamageEvent;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.handler.EntityAttributeType;
import com.afterkraft.kraftrpg.api.handler.ItemAttributeType;
import com.afterkraft.kraftrpg.api.skills.ISkill;
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
            instance = entityLiving.getAttributeMap().b(this.iattrMap.get(type)); // should be getAttributeMap().setup()
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
            instance = entityLiving.getAttributeMap().b(this.iattrMap.get(type)); // should be getAttributeMap().setup()
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
        int i = 25 - ((CraftLivingEntity) defender).getHandle().aV();
        float f1 = (float) damage * (float) i;
        return f1 / 25;
    }

    @Override
    public final double getPostArmorDamage(Insentient being, EntityDamageEvent event, double damage) {
        if (being.getEntity() instanceof CraftLivingEntity) {
            int i = 25 - ((CraftLivingEntity) being.getEntity()).getHandle().aV();
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
    public boolean damageEntity(LivingEntity target, Insentient attacker, ISkill skill, double damage, DamageCause cause, boolean knockback) {
        Insentient being = (Insentient) this.plugin.getEntityManager().getEntity(target);
        return damageEntity(being, attacker, skill, damage, cause, knockback);
    }

    @Override
    public boolean damageEntity(final Insentient target, final Insentient attacker, final ISkill skill, double damage, final DamageCause cause, final boolean knockback) {
        Map<InsentientDamageEvent.DamageType, Double> modifiers = new EnumMap<InsentientDamageEvent.DamageType, Double>(ImmutableMap.of(InsentientDamageEvent.DamageType.PHYSICAL, damage));
        return damageEntity(target, attacker, skill, modifiers, cause, knockback);
    }

    @Override
    public boolean damageEntity(final Insentient target, final Insentient attacker, final ISkill skill, final Map<InsentientDamageEvent.DamageType, Double> modifiers, final DamageCause cause, final boolean knockback) {
        Validate.notNull(target, "Cannot attack a null target!");
        Validate.notNull(attacker, "Cannot attack with a null attacker!");
        Validate.notNull(modifiers, "Cannot attack with null modifiers!");
        Validate.noNullElements(modifiers.values(), "Cannot have null values in the modifiers");
        Validate.notNull(cause, "Cannot apply a null DamageCause!");
        LivingEntity targetLivingEntity = target.getEntity();
        LivingEntity casterLivingEntity = attacker.getEntity();
        int originalNoDamageTicks = target.getNoDamageTicks();
        targetLivingEntity.setNoDamageTicks(0);

        EntityDamageByEntityEvent event = createEntityDamageEvent(targetLivingEntity, casterLivingEntity, modifiers, cause);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        InsentientDamageInsentientEvent skillEvent = new InsentientDamageInsentientEvent(attacker, target, event, modifiers, false);
        Bukkit.getPluginManager().callEvent(skillEvent);
        if (event.isCancelled()) {
            return false;
        }
        final double finalSkillDamage = skillEvent.getFinalDamage();
        targetLivingEntity.setLastDamageCause(event);
        final double oldHealth = target.getHealth();
        double newHealth = oldHealth - finalSkillDamage;
        if (newHealth < 0) {
            newHealth = 0;
        }
        final EntityLiving el = ((CraftLivingEntity) target).getHandle();
        el.lastDamage = (float) finalSkillDamage;
        el.aw = (float) oldHealth;
        el.hurtTicks = el.ay = 10;
        el.az = 0.0F;
        if (knockback) {
            knockBack(targetLivingEntity, casterLivingEntity, finalSkillDamage);
        }
        el.world.broadcastEntityEffect(el, (byte) 2);

        el.lastDamager = ((CraftLivingEntity) casterLivingEntity).getHandle();

        // Set last damage by player time via reflection.
        if (casterLivingEntity instanceof Player) {
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
            el.world.makeSound(el, getSoundName(targetLivingEntity.getType()), 1.0f, getSoundStrength(targetLivingEntity));

            if (casterLivingEntity instanceof Player) {
                final EntityPlayer p = ((CraftPlayer) casterLivingEntity).getHandle();
                el.killer = p;
                el.die(DamageSource.playerAttack(p));
            } else {
                final EntityLiving att = ((CraftLivingEntity) casterLivingEntity).getHandle();
                el.die(DamageSource.mobAttack(att));
            }
        } else {
            targetLivingEntity.setNoDamageTicks(originalNoDamageTicks);

            final EntityLiving attackEntity = ((CraftLivingEntity) casterLivingEntity).getHandle();
            if (targetLivingEntity instanceof org.bukkit.entity.Monster) {
                if ((targetLivingEntity instanceof Blaze) || (targetLivingEntity instanceof Enderman) || (targetLivingEntity instanceof Spider) || (targetLivingEntity instanceof Giant) || (targetLivingEntity instanceof Silverfish)) {
                    final EntityMonster em = (EntityMonster) el;
                    final EntityTargetEvent targetEvent = CraftEventFactory.callEntityTargetEvent(em, attackEntity, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY);
                    if (!targetEvent.isCancelled()) {
                        em.setTarget(attackEntity);
                    }
                }
            } else if (targetLivingEntity instanceof IronGolem) {
                final EntityGolem eg = (EntityGolem) el;
                eg.setTarget(((CraftLivingEntity) attacker).getHandle());
            } else if ((targetLivingEntity instanceof Wolf) && (((Wolf) targetLivingEntity).getTarget() == null)) {
                final Wolf wolf = (Wolf) targetLivingEntity;
                wolf.setAngry(true);
                wolf.setTarget(casterLivingEntity);
            }
            if (targetLivingEntity instanceof PigZombie) {
                ((PigZombie) target).setAngry(true);
            }

        }
        return true;
    }

    private EntityDamageByEntityEvent createEntityDamageEvent(LivingEntity defender, LivingEntity attacker, Map<InsentientDamageEvent.DamageType, Double> modifiers, DamageCause cause) {
        double damage = 0;
        for (InsentientDamageEvent.DamageType damageType : InsentientDamageEvent.DamageType.values()) {
            damage += modifiers.get(damageType);
        }
        if (defender instanceof CraftLivingEntity) {
            CraftLivingEntity craftEntity = (CraftLivingEntity) defender;
            final EntityLiving entityLiving = craftEntity.getHandle();

            final boolean human = entityLiving instanceof EntityHuman;
            Function<Double, Double> blocking = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (human) {
                        if (((EntityHuman) entityLiving).isBlocking() && f > 0.0F) {
                            return -(f - ((1.0F + f) * 0.5F));
                        }
                    }
                    return -0.0;
                }
            };
            float blockingModifier = blocking.apply(damage).floatValue();
            damage += blockingModifier;

            Function<Double, Double> armor = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    try {
                        Method applyArmorModifier = entityLiving.getClass().getDeclaredMethod("applyArmorModifier", DamageSource.class, Float.TYPE);
                        applyArmorModifier.setAccessible(true);

                        return -(f - (Double) applyArmorModifier.invoke(DamageSource.GENERIC, f.floatValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0D;
                }
            };
            float armorModifier = armor.apply(damage).floatValue();
            damage += armorModifier;

            Function<Double, Double> resistance = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (entityLiving.hasEffect(MobEffectList.RESISTANCE)) {
                        int i = (entityLiving.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
                        int j = 25 - i;
                        float f1 = f.floatValue() * (float) j;
                        return -(f - (f1 / 25.0F));
                    }
                    return -0.0;
                }
            };
            float resistanceModifier = resistance.apply(damage).floatValue();
            damage += resistanceModifier;

            Function<Double, Double> magic = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    try {
                        Method applyMagicModifier = entityLiving.getClass().getDeclaredMethod("applyMagicModifier", DamageSource.class, Float.TYPE);
                        applyMagicModifier.setAccessible(true);

                        return -(f - (Double) applyMagicModifier.invoke(DamageSource.GENERIC, f));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0D;
                    }
                }
            };
            float magicModifier = magic.apply(damage).floatValue();
            damage += magicModifier;

            Function<Double, Double> absorption = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(Math.max(f - Math.max(f - entityLiving.getAbsorptionHearts(), 0.0F), 0.0F));
                }
            };
            float absorptionModifier = absorption.apply(damage).floatValue();
            Map<EntityDamageEvent.DamageModifier, Double> damageModifierDoubleEnumMap = new EnumMap<EntityDamageEvent.DamageModifier, Double>(EntityDamageEvent.DamageModifier.class);
            Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<EntityDamageEvent.DamageModifier, Function<? super Double, Double>>(EntityDamageEvent.DamageModifier.class);
            damageModifierDoubleEnumMap.put(EntityDamageEvent.DamageModifier.BASE, damage);
            modifierFunctions.put(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0));
            if (entityLiving instanceof EntityHuman) {
                damageModifierDoubleEnumMap.put(EntityDamageEvent.DamageModifier.BLOCKING, (double) blockingModifier);
                modifierFunctions.put(EntityDamageEvent.DamageModifier.BLOCKING, blocking);
            }
            damageModifierDoubleEnumMap.put(EntityDamageEvent.DamageModifier.ARMOR, (double) armorModifier);
            modifierFunctions.put(EntityDamageEvent.DamageModifier.ARMOR, armor);
            damageModifierDoubleEnumMap.put(EntityDamageEvent.DamageModifier.RESISTANCE, (double) resistanceModifier);
            modifierFunctions.put(EntityDamageEvent.DamageModifier.RESISTANCE, resistance);
            damageModifierDoubleEnumMap.put(EntityDamageEvent.DamageModifier.MAGIC, (double) magicModifier);
            modifierFunctions.put(EntityDamageEvent.DamageModifier.MAGIC, magic);
            damageModifierDoubleEnumMap.put(EntityDamageEvent.DamageModifier.ABSORPTION, (double) absorptionModifier);
            modifierFunctions.put(EntityDamageEvent.DamageModifier.ABSORPTION, absorption);
            return new EntityDamageByEntityEvent(attacker, defender, cause, damageModifierDoubleEnumMap, modifierFunctions);
        }
        return new EntityDamageByEntityEvent(attacker, defender, cause, damage);
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

    @Override
    public void hidePlayer(Player player) {
        if (this.plugin.getProperties().useVanishIfAvailable()) {

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