package net.flytre.mechanix.block.cell;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.flytre.mechanix.base.EnergyScreen;
import net.flytre.mechanix.base.EnergyScreenHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class EnergyCellScreen extends EnergyScreen<EnergyCellScreenHandler> {

    private final Identifier background;

    public EnergyCellScreen(EnergyCellScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler,inventory,title);
        this.background = new Identifier("mechanix:textures/gui/container/basic.png");
        ++this.backgroundHeight;
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }


    @Override
    @Environment(EnvType.CLIENT)
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.client != null;
        this.client.getTextureManager().bindTexture(this.background);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        super.drawBackground(matrices,delta,mouseX,mouseY);
    }
}
