package net.raphimc.immediatelyfast.module.modules.combat;

import net.raphimc.immediatelyfast.event.events.TickListener;
import net.raphimc.immediatelyfast.module.Category;
import net.raphimc.immediatelyfast.module.Module;
import net.raphimc.immediatelyfast.module.setting.NumberSetting;
import net.raphimc.immediatelyfast.utils.EncryptedString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class MaceSwap extends Module implements TickListener {

    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0, 600, 100, 1)
            .setDescription(EncryptedString.of("Delay before swapping to Mace (ms)"));

    private long swapScheduledAt = -1;
    private int originalSlot = -1;
    private boolean waitingToSwap = false;
    private boolean swapped = false;

    public MaceSwap() {
        super(EncryptedString.of("Mace Swap"),
                EncryptedString.of("Swaps to Mace when you hit an enemy in air with a sword"),
                -1,
                Category.COMBAT);
        addSettings(delay);
    }

    @Override
    public void onEnable() {
        eventManager.add(TickListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(TickListener.class, this);
        waitingToSwap = false;
        swapped = false;
        originalSlot = -1;
        super.onDisable();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        PlayerEntity player = mc.player;

        // If swapped, swap back when on ground
        if (swapped && player.isOnGround()) {
            if (originalSlot != -1) {
                player.getInventory().selectedSlot = originalSlot;
            }
            swapped = false;
            waitingToSwap = false;
            originalSlot = -1;
            swapScheduledAt = -1;
            return;
        }

        // If waiting to swap, check delay
        if (waitingToSwap) {
            long now = System.currentTimeMillis();
            if (now - swapScheduledAt >= delay.getValue()) {
                int maceSlot = findMaceSlot(player);
                if (maceSlot != -1) {
                    originalSlot = player.getInventory().selectedSlot;
                    player.getInventory().selectedSlot = maceSlot;
                    swapped = true;
                }
                waitingToSwap = false;
            }
            return;
        }

        // Check: player is in air
        if (player.isOnGround()) return;

        // Check: holding sword
        Item heldItem = player.getMainHandStack().getItem();
        if (!(heldItem instanceof SwordItem)) return;

        // Check: crosshair is on a living entity
        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) return;
        if (!(((EntityHitResult) hitResult).getEntity() instanceof LivingEntity target)) return;
        if (target.isDead()) return;

        // Check mace exists in hotbar
        if (findMaceSlot(player) == -1) return;

        // Schedule swap
        waitingToSwap = true;
        swapScheduledAt = System.currentTimeMillis();
    }

    private int findMaceSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }
            }
