package net.raphimc.immediatelyfast.module.modules.combat;

import net.raphimc.immediatelyfast.event.events.TickListener;
import net.raphimc.immediatelyfast.module.Category;
import net.raphimc.immediatelyfast.module.Module;
import net.raphimc.immediatelyfast.module.setting.BooleanSetting;
import net.raphimc.immediatelyfast.module.setting.NumberSetting;
import net.raphimc.immediatelyfast.utils.EncryptedString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public final class AutoMace extends Module implements TickListener {

    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 6.0, 4.0, 0.1)
            .setDescription(EncryptedString.of("Attack range"));
    private final NumberSetting fov = new NumberSetting(EncryptedString.of("FOV"), 30, 180, 90, 5)
            .setDescription(EncryptedString.of("Field of view to detect enemies (degrees)"));
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0, 600, 100, 1)
            .setDescription(EncryptedString.of("Delay between attacks in ms (0 = instant)"));
    private final NumberSetting minFallDistance = new NumberSetting(EncryptedString.of("Min Fall Dist"), 0, 10, 1.5, 0.5)
            .setDescription(EncryptedString.of("Minimum fall distance before attacking"));
    private final BooleanSetting onlyPlayers = new BooleanSetting(EncryptedString.of("Only Players"), true)
            .setDescription(EncryptedString.of("Only target players"));
    private final BooleanSetting requireFalling = new BooleanSetting(EncryptedString.of("Require Falling"), true)
            .setDescription(EncryptedString.of("Only attack while falling for mace bonus damage"));

    private long lastAttackTime = 0;

    public AutoMace() {
        super(EncryptedString.of("Auto Mace"),
                EncryptedString.of("Automatically attacks with Mace when enemy is in FOV and falling"),
                -1,
                Category.COMBAT);
        addSettings(range, fov, delay, minFallDistance, onlyPlayers, requireFalling);
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

        if (mc.player.getMainHandStack().getItem() != Items.MACE) return;

        if (requireFalling.getValue() && mc.player.getVelocity().y >= 0) return;

        if (mc.player.fallDistance < minFallDistance.getValue()) return;

        long now = System.currentTimeMillis();
        if (now - lastAttackTime < delay.getValue()) return;

        LivingEntity target = getTargetInFOV(mc);
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttackTime = now;
    }

    private LivingEntity getTargetInFOV(MinecraftClient mc) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d lookDir = mc.player.getRotationVec(1.0f);
        double halfFov = fov.getValue() / 2.0;
        double rangeSq = range.getValue() * range.getValue();

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        List<LivingEntity> entities = mc.world.getEntitiesByClass(
                LivingEntity.class,
                mc.player.getBoundingBox().expand(range.getValue()),
                e -> true
        );

        for (LivingEntity entity : entities) {
            if (entity == mc.player) continue;
            if (entity.isDead()) continue;
            if (onlyPlayers.getValue() && !(entity instanceof PlayerEntity)) continue;

            double distSq = eyePos.squaredDistanceTo(entity.getPos());
            if (distSq > rangeSq) continue;

            Vec3d toEntity = entity.getEyePos().subtract(eyePos).normalize();
            double angle = Math.toDegrees(Math.acos(MathHelper.clamp(lookDir.dotProduct(toEntity), -1.0, 1.0)));
            if (angle > halfFov) continue;

            if (distSq < closestDist) {
                closestDist = distSq;
                closest = entity;
            }
        }

        return closest;
    }
    }
