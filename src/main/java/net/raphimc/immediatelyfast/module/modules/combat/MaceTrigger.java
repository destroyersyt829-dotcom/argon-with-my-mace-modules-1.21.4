package net.raphimc.immediatelyfast.module.modules.combat;

import net.raphimc.immediatelyfast.event.events.TickListener;
import net.raphimc.immediatelyfast.module.Category;
import net.raphimc.immediatelyfast.module.Module;
import net.raphimc.immediatelyfast.module.setting.BooleanSetting;
import net.raphimc.immediatelyfast.module.setting.NumberSetting;
import net.raphimc.immediatelyfast.utils.EncryptedString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class MaceTrigger extends Module implements TickListener {

    private final NumberSetting attackDelay = new NumberSetting(EncryptedString.of("Attack Delay"), 0, 500, 450, 10)
            .setDescription(EncryptedString.of("Delay in ms between attacks"));
    private final NumberSetting minFallDistance = new NumberSetting(EncryptedString.of("Min Fall Distance"), 0, 3, 1, 0.5)
            .setDescription(EncryptedString.of("Minimum fall distance to trigger attack"));
    private final BooleanSetting onlyPlayers = new BooleanSetting(EncryptedString.of("Only Players"), true)
            .setDescription(EncryptedString.of("Only trigger on players, not mobs"));

    private long lastAttackTime = 0;

    public MaceTrigger() {
        super(EncryptedString.of("Mace Trigger"),
                EncryptedString.of("Auto attacks with Mace when crosshair is on entity while falling"),
                -1,
                Category.COMBAT);
        addSettings(attackDelay, minFallDistance, onlyPlayers);
    }

    @Override
    public void onEnable() {
        eventManager.add(TickListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(TickListener.class, this);
        super.onDisable();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check if holding Mace
        if (mc.player.getMainHandStack().getItem() != Items.MACE) return;

        // Check if falling (negative Y velocity)
        if (mc.player.getVelocity().y >= 0) return;

        // Check minimum fall distance
        if (mc.player.fallDistance < minFallDistance.getValue()) return;

        // Check crosshair target
        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) return;

        Entity target = ((EntityHitResult) hitResult).getEntity();

        // Only players check
        if (onlyPlayers.getValue() && !(target instanceof PlayerEntity)) return;

        // Must be living entity and alive
        if (!(target instanceof LivingEntity living) || living.isDead()) return;

        // Attack delay check
        long now = System.currentTimeMillis();
        if (now - lastAttackTime < attackDelay.getValue()) return;

        // Attack!
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttackTime = now;
    }
          }
