package com.aki.modfix.util.fix;

import net.minecraft.client.gui.GuiButton;

import java.util.List;

public interface GuiControlsGetter {
    List<GuiButton> getButton();

    void SetIndex(int Index);
}
