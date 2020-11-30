package net.flytre.mechanix.block.hydrator;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.flytre.mechanix.base.Formatter;
import net.flytre.mechanix.base.MachineBlock;
import net.flytre.mechanix.base.fluid.FluidInventory;
import net.flytre.mechanix.base.fluid.FluidStack;
import net.flytre.mechanix.util.MachineRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class HydratorBlockEntity extends BlockEntity implements Tickable, FluidInventory, ExtendedScreenHandlerFactory {
    public HashMap<Direction, Boolean> ioMode; //true = output, false = input
    private final DefaultedList<FluidStack> inventory;
    private final PropertyDelegate properties;


    public HydratorBlockEntity() {
        super(MachineRegistry.HYDRATOR.getEntityType());
        ioMode = new HashMap<>();
        setFluidMode(true, true, true, true, true, true);
        inventory = DefaultedList.ofSize(1,FluidStack.EMPTY);
        this.properties = new ArrayPropertyDelegate(4);
    }

    public void setFluidMode(boolean up, boolean down, boolean north, boolean east, boolean south, boolean west) {
        ioMode.put(Direction.UP, up);
        ioMode.put(Direction.DOWN, down);
        ioMode.put(Direction.NORTH, north);
        ioMode.put(Direction.EAST, east);
        ioMode.put(Direction.SOUTH, south);
        ioMode.put(Direction.WEST, west);
    }

    private int getAmount() {
        return inventory.get(0).getAmount();
    }

    public void updateDelegate() {
        if (world != null && world.isClient)
            return;
        int[] splitFluid = Formatter.splitInt(getAmount());
        properties.set(0, splitFluid[0]);
        properties.set(1, splitFluid[1]);
        properties.set(2, Formatter.hashToInt(ioMode));
        properties.set(3,1);
    }

    @Override
    public void tick() {

        if(world == null || world.isClient)
            return;

        MachineBlock.fixBlockState(ioMode,getPos(),world);
        updateDelegate();

        boolean currActivated = world.getBlockState(getPos()).get(HydratorBlock.ACTIVATED);
        boolean shouldBeActivated = false;
        int counter = 0;
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                BlockPos pos = new BlockPos(getPos().getX() + i, getPos().getY(), getPos().getZ() + j);
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if(block == Blocks.WATER)
                    counter++;
            }
        }
        if(counter >= 8) {
            shouldBeActivated = true;
        }
        if(shouldBeActivated) {
            if(canAdd(new FluidStack(Fluids.WATER,100)))
                add(new FluidStack(Fluids.WATER,100));
        }

        if(shouldBeActivated != currActivated) {
            world.setBlockState(getPos(),world.getBlockState(pos).with(HydratorBlock.ACTIVATED,shouldBeActivated));
        }

    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        FluidInventory.fromTag(tag,inventory);
        ioMode = Formatter.intToHash(tag.getInt("IOMode"));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        FluidInventory.toTag(tag,inventory);
        tag.putInt("IOMode", Formatter.hashToInt(ioMode));
        return super.toTag(tag);
    }


    public PropertyDelegate getProperties() {
        return properties;
    }

    @Override
    public HashMap<Direction, Boolean> getFluidIO() {
        return ioMode;
    }

    @Override
    public DefaultedList<FluidStack> getFluids() {
        return inventory;
    }

    @Override
    public int capacity() {
        return 8000;
    }

    @Override
    public boolean isValid(int slot, FluidStack stack) {
        return false;
    }

    public FluidStack getStack() {
        return inventory.get(0);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(pos);

    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.mechanix.hydrator");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new HydratorScreenHandler(syncId,inv,this,this.getProperties());
    }
}
