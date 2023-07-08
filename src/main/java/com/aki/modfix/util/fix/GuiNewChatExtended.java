package com.aki.modfix.util.fix;

import net.minecraft.client.gui.ChatLine;

import java.util.List;

public interface GuiNewChatExtended {
    List<ChatLine> getChatLines();

    void SetChatLines(List<ChatLine> ChatLines);

    List<ChatLine> getDrawnChatLines();

    void SetDrawnChatLines(List<ChatLine> DrawnChatLines);
}
