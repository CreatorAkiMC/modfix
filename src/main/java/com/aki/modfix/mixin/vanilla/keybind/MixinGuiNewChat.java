package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.GuiNewChatExtended;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GuiNewChat.class, priority = Modfix.ModPriority)
public class MixinGuiNewChat implements GuiNewChatExtended {
    @Mutable
    @Shadow @Final private List<ChatLine> drawnChatLines;

    @Mutable
    @Shadow @Final private List<ChatLine> chatLines;

    @Override
    public List<ChatLine> getChatLines() {
        return this.chatLines;
    }

    @Override
    public void SetChatLines(List<ChatLine> ChatLines) {
        this.chatLines = ChatLines;
    }

    @Override
    public List<ChatLine> getDrawnChatLines() {
        return this.drawnChatLines;
    }

    @Override
    public void SetDrawnChatLines(List<ChatLine> DrawnChatLines) {
        this.drawnChatLines = DrawnChatLines;
    }
}
