package net.mine_diver.macula.gui;

import net.mine_diver.macula.Shaders;
import net.mine_diver.macula.mixin.ScrollableBaseAccessor;
import net.mine_diver.macula.sources.ShaderpackSource;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.renderer.Tesselator;
import java.util.List;

class ScrollableShaders extends AbstractSelectionList {
    private List<String> shadersList;
    private int selectedIndex;
    private final long lastClicked = Long.MIN_VALUE;
    private long lastClickedCached = 0L;
    final ShadersScreen shadersGui;

    public ScrollableShaders(ShadersScreen par1GuiShaders, int width, int height, int top, int bottom, int slotHeight) {
        super(par1GuiShaders.getMc(), width, height, top, bottom, slotHeight);
        this.shadersGui = par1GuiShaders;
        this.updateList();
    }

    public void updateList() {
        var shaderpackSources = Shaders.listOfShaderpacks();
        shadersList = shaderpackSources.stream().map(ShaderpackSource::getName).toList();
        Shaders.closeListOfShaderpacks(shaderpackSources);

        this.selectedIndex = 0;
        int i = 0;

        String currentName = Shaders.getCurrentShaderSource().getName();
        for (int j = this.shadersList.size(); i < j; ++i) {
            if (this.shadersList.get(i).equals(currentName)) {
                this.selectedIndex = i;
                break;
            }
        }
    }

    @Override
    protected int getItemCount() {
        return this.shadersList.size();
    }

    @Override
    protected void method_1267(int index, boolean twice) {
        if (index == this.selectedIndex && this.lastClicked == this.lastClickedCached) return;
        this.selectIndex(index);
    }

    private void selectIndex(int index) {
        this.selectedIndex = index;
        this.lastClickedCached = this.lastClicked;
        Shaders.setShaderPack(this.shadersList.get(index));
        Shaders.loadShaderPack();
        shadersGui.updateButtons();
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return index == this.selectedIndex;
    }

    protected void renderBackground() {
    }

    @Override
    protected void renderEntry(int index, int posX, int posY, int contentY, Tesselator tessellator) {
        String s = this.shadersList.get(index);
        int separator = s.indexOf(ShaderpackSource.TypeSeparator);
        if (separator != -1) {
            s = s.substring(separator + ShaderpackSource.TypeSeparator.length());
        }

        this.shadersGui.drawCenteredString(shadersGui.getTextRenderer(), s, ((ScrollableBaseAccessor) this).macula_getWidth() / 2, posY + 1, 0xe0e0e0);
    }
}
