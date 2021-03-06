package net.flytre.mechanix.block.liquifier;

import net.flytre.mechanix.api.energy.EnergyScreenHandler;
import net.flytre.mechanix.util.MachineRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LiquifierScreenHandler extends EnergyScreenHandler {
    private final Inventory inventory;
    private final World world;

    public LiquifierScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new LiquifierBlockEntity(), new ArrayPropertyDelegate(24));
        this.pos = buf.readBlockPos();
    }

    public LiquifierScreenHandler(int syncId, PlayerInventory playerInventory, LiquifierBlockEntity entity, PropertyDelegate propertyDelegate) {
        super(MachineRegistry.LIQUIFIER.getHandlerType(), syncId, playerInventory, entity, propertyDelegate);

        this.pos = BlockPos.ORIGIN;
        this.addSlot(new Slot(entity, 0, 69, 35));

        int o;
        int n;
        for (o = 0; o < 3; ++o) {
            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, n + o * 9 + 9, 8 + n * 18, 84 + o * 18));
            }
        }

        for (o = 0; o < 9; ++o) {
            this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 142));
        }

        this.inventory = entity;
        this.world = playerInventory.player.world;

    }


    public double operationProgress() {
        return getPropertyDelegate().get(8) / 120.0;
    }


    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.insertItem(itemStack2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onStackChanged(itemStack2, itemStack);
            } else {
                if (!this.insertItem(itemStack2, 0, 2, false))
                    return ItemStack.EMPTY;
                if (index < 28) {
                    if (!this.insertItem(itemStack2, 29, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !this.insertItem(itemStack2, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
