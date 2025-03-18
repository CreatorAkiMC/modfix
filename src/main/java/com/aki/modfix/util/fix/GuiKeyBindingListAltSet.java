package com.aki.modfix.util.fix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * [
 * minecraftのパターンに入れない操作
 * <p>
 * GameSettingsの 120 ~ 144 まで
 * ]
 * <p>
 * 1 2 3 4 5 6 7 8 9 <- (パターン)
 * [Alt + num] で変更 (と表示)
 * <p>
 * [Done]の周りがよさそう
 */
public class GuiKeyBindingListAltSet extends GuiListExtended {
    private final GuiControls controlsScreen;
    private final Minecraft mc;
    private GuiListExtended.IGuiListEntry[] VanillaListEntries;
    private final GuiListExtended.IGuiListEntry[][] ModListEntries = new IGuiListEntry[9][];
    private int maxListLabelWidth;
    private boolean IsVanilla = true;//true == Vanilla KeyBinding
    public int Pattern = 0;

    public boolean ButtonGuiControlIn = false; //AltSet内以外のボタンに触れている場合 true

    public GuiButtonAltSet[] SelectButton = new GuiButtonAltSet[9];

    /**
     * KeyBindingに登録されているものを変換
     *
     * Patternで判別できそう
     * 二重配列
     * GuiListExtended.IGuiListEntry[pattern][] listEntries
     * */

    /* Key + KeyCodes     https://shanabrian.com/web/javascript/keycode.php
            1	49
            2	50
            3	51
            4	52
            5	53
            6	54
            7	55
            8	56
            9	57
            0	48
     */

    /**
     * ModKeyの場合は、setPatternで設定してから使用
     */

    public GuiKeyBindingListAltSet(GuiControls controls, Minecraft mcIn) {
        super(mcIn, controls.width + 45, controls.height, 63, controls.height - 32, 20);
        this.controlsScreen = controls;
        this.mc = mcIn;
        KeyBinding[] VanillaKeybinding = ((GameSettingsExtended) mcIn.gameSettings).MCKeyBinding();
        this.VanillaListEntries = new GuiListExtended.IGuiListEntry[VanillaKeybinding.length];
        Arrays.sort(VanillaKeybinding);
        int i = 0;
        String s = null;

        for (KeyBinding keybinding : VanillaKeybinding) {
            String s1 = keybinding.getKeyCategory();

            if (!s1.equals(s)) {
                s = s1;
                if ((int) Arrays.stream(this.VanillaListEntries).filter(Objects::nonNull).count() >= this.VanillaListEntries.length) {
                    this.VanillaListEntries = ArrayUtils.add(this.VanillaListEntries, new GuiKeyBindingListAltSet.CategoryEntry(s1));
                } else {
                    this.VanillaListEntries[i] = new GuiKeyBindingListAltSet.CategoryEntry(s1);
                }
                i++;
            }

            int j = mcIn.fontRenderer.getStringWidth(I18n.format(keybinding.getKeyDescription()));

            if (j > this.maxListLabelWidth) {
                this.maxListLabelWidth = j;
            }

            if ((int) Arrays.stream(this.VanillaListEntries).filter(Objects::nonNull).count() >= this.VanillaListEntries.length) {
                this.VanillaListEntries = ArrayUtils.add(this.VanillaListEntries, new GuiKeyBindingListAltSet.KeyEntry(keybinding, 0, false));
            } else {
                this.VanillaListEntries[i] = new GuiKeyBindingListAltSet.KeyEntry(keybinding, 0, false);
            }
            i++;
        }

        GameSettingsExtended extended = ((GameSettingsExtended) mcIn.gameSettings);

        /**
         *
         * */
        int idx = extended.getPatternID();

        for (int pattern1 = 0; pattern1 < 9; pattern1++) {

            //ボタン生成
            SelectButton[pattern1] = new GuiButtonAltSet(pattern1, 0, 0, 20, 20, String.valueOf(pattern1 + 1));
            SelectButton[pattern1].visible = true;// 念のため

            extended.setPatternID(pattern1);//Patternの設定

            //KeyBinding を追加するmodがないとModRegisteredBindingがnullになる。
            KeyBinding[] ModKeybinding = ArrayUtils.clone(extended.ModRegisteredBinding());
            this.ModListEntries[pattern1] = new GuiListExtended.IGuiListEntry[(ModKeybinding != null ? ModKeybinding.length : 0) + 1];

            //ボタン追加
            this.ModListEntries[pattern1][0] = new SelectedButtonEntry();

            TreeMap<KeyBinding, Integer> map = new TreeMap<>(Comparator.naturalOrder());

            Arrays.stream(ModKeybinding).forEach((o) -> {
                map.put(o, map.size());//Index 0 ~ X
            });

            //Iterator<KeyBinding> bindingSet = map.keySet().iterator();

            //IntStream.range(0, map.size()).forEach((index) -> ModKeybinding[index] = bindingSet.next());

            //map.clear();

            //Arrays.sort((Object[]) ModKeybinding);

            i = 1;
            s = null;

            while (!map.isEmpty()) {//ループ
                Map.Entry<KeyBinding, Integer> entry = map.pollFirstEntry();
                KeyBinding keybinding = entry.getKey();
                String s1 = keybinding.getKeyCategory();

                if (!s1.equals(s)) {
                    s = s1;
                    if ((int) Arrays.stream(this.ModListEntries[pattern1]).filter(Objects::nonNull).count() >= this.ModListEntries[pattern1].length) {
                        this.ModListEntries[pattern1] = ArrayUtils.add(this.ModListEntries[pattern1], new GuiKeyBindingListAltSet.CategoryEntry(s1));
                    } else {
                        this.ModListEntries[pattern1][i] = new GuiKeyBindingListAltSet.CategoryEntry(s1);
                    }
                    i++;
                }

                int j = mcIn.fontRenderer.getStringWidth(I18n.format(keybinding.getKeyDescription()));

                if (j > this.maxListLabelWidth) {
                    this.maxListLabelWidth = j;
                }

                if ((int) Arrays.stream(this.ModListEntries[pattern1]).filter(Objects::nonNull).count() >= this.ModListEntries[pattern1].length) {
                    this.ModListEntries[pattern1] = ArrayUtils.add(this.ModListEntries[pattern1], new GuiKeyBindingListAltSet.KeyEntry(keybinding, entry.getValue(), true));
                } else {
                    this.ModListEntries[pattern1][i] = new GuiKeyBindingListAltSet.KeyEntry(keybinding, entry.getValue(), true);
                }

                i++;
            }
        }

        extended.setPatternID(idx);

        SelectButton[0].enabled = false;
        GuiKeyBindingListAltSet.this.setPattern(0);

        /**
         * Index保証   Null削除
         **/

        this.VanillaListEntries = Arrays.stream(this.VanillaListEntries).filter(Objects::nonNull).toArray(IGuiListEntry[]::new);
        IntStream.range(0, this.ModListEntries.length).forEach((x) -> this.ModListEntries[x] = Arrays.stream(this.ModListEntries[x]).filter(Objects::nonNull).toArray(IGuiListEntry[]::new));
    }

    //true == Vanilla Key Bindings
    @Override
    protected int getSize() {
        return this.IsVanilla ? (int) Arrays.stream(this.VanillaListEntries).filter(Objects::nonNull).count() : (int) Arrays.stream(this.ModListEntries[Pattern]).filter(Objects::nonNull).count();
    }

    /**
     * Scroll Bar 用
     */
    @Override
    protected int getContentHeight() {
        //this.getSize を置き換えて、正しく動作するようにした。
        return (this.VanillaListEntries.length + this.ModListEntries[0].length + 2) * this.slotHeight + this.headerPadding;
    }

    /**
     * index 注意が必要
     */
    @Override
    public GuiListExtended.IGuiListEntry getListEntry(int index) {
        return this.IsVanilla ? this.VanillaListEntries[index] : this.ModListEntries[Pattern][index];
    }

    @Override
    protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
        this.setIsVanilla(true);
        int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        /**
         * 間の調整
         * */
        int i2 = i + 1;

        for (int j = 0; j < i; ++j) {
            int k = insideTop + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;

            if (k > this.bottom || k + l < this.top) {
                this.updateItemPos(j, insideLeft, k, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(j)) {
                int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
                int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(i1, k + l + 2, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(j1, k + l + 2, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(j1, k - 2, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(i1, k - 2, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(i1 + 1, k + l + 1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(j1 - 1, k + l + 1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(j1 - 1, k - 1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(i1 + 1, k - 1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn, partialTicks);
        }


        /**
         * Mod用
         * */

        this.setIsVanilla(false);
        i = this.getSize();
        tessellator = Tessellator.getInstance();
        bufferbuilder = tessellator.getBuffer();

        for (int j = 0; j < i; ++j) {
            int k = insideTop + (j + i2) * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;

            if (k > this.bottom || k + l < this.top) {
                this.updateItemPos(j, insideLeft, k, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(j)) {
                int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
                int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(i1, k + l + 2, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(j1, k + l + 2, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(j1, k - 2, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(i1, k - 2, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(i1 + 1, k + l + 1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(j1 - 1, k + l + 1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(j1 - 1, k - 1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(i1 + 1, k - 1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn, partialTicks);
        }
    }

    /**
     * Index修正
     */

    //Slot 420から参照
    @Override
    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        ButtonGuiControlIn = ((GuiControlsGetter) this.controlsScreen).getButton().stream().anyMatch((i) -> mouseXIn >= i.x && mouseYIn >= i.y && mouseXIn < i.x + i.width && mouseYIn < i.y + i.height);
        this.getListEntry(slotIndex).drawEntry(slotIndex, xPos, yPos, this.getListWidth(), heightIn, mouseXIn, mouseYIn, this.isMouseYWithinSlotBounds(mouseYIn) && this.getSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == slotIndex, partialTicks);
    }

    @Override
    protected void updateItemPos(int entryID, int insideLeft, int yPos, float partialTicks) {
        this.getListEntry(entryID).updatePosition(entryID, insideLeft, yPos, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {

        ButtonGuiControlIn = ((GuiControlsGetter) this.controlsScreen).getButton().stream().anyMatch((i) -> mouseX >= i.x && mouseY >= i.y && mouseX < i.x + i.width && mouseY < i.y + i.height);
        if (this.isMouseYWithinSlotBounds(mouseY) && !ButtonGuiControlIn) {
            boolean isV = this.IsVanilla;

            this.setIsVanilla(true);
            for (int i = 0; i < this.getSize(); ++i) {
                int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
                int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
                int l = mouseX - j;
                int i1 = mouseY - k;

                if (this.getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, l, i1)) {
                    this.setEnabled(false);
                    this.setIsVanilla(isV);
                    return true;
                }
            }

            this.setIsVanilla(false);
            for (int i = 0; i < this.getSize(); ++i) {
                int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
                int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
                int l = mouseX - j;
                int i1 = mouseY - k;

                if (this.getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, l, i1)) {
                    this.setEnabled(false);
                    this.setIsVanilla(isV);
                    return true;
                }
            }

            this.setIsVanilla(isV);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(int x, int y, int mouseEvent) {
        boolean isV = this.IsVanilla;

        this.setIsVanilla(true);
        for (int i = 0; i < this.getSize(); ++i) {
            int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
            int l = x - j;
            int i1 = y - k;
            this.getListEntry(i).mouseReleased(i, x, y, mouseEvent, l, i1);
            this.setIsVanilla(isV);
        }

        this.setIsVanilla(false);
        for (int i = 0; i < this.getSize(); ++i) {
            int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
            int l = x - j;
            int i1 = y - k;
            this.getListEntry(i).mouseReleased(i, x, y, mouseEvent, l, i1);
            this.setIsVanilla(isV);
        }

        this.setIsVanilla(isV);

        this.setEnabled(true);
        return false;
    }

    public void setIsVanilla(boolean IsVanilla) {
        this.IsVanilla = IsVanilla;

    }

    public boolean getIsVanilla() {
        return this.IsVanilla;
    }

    public void setPattern(int patternId) {
        this.Pattern = patternId;
        ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).setPatternID(patternId);

        /**
         * KeyBinding の修正
         * */
        KeyBindingRegister[] registers = ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).KeyBindingRegister();
        KeyBinding[] keyBinding = ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).ModRegisteredBinding();
        for (int i = 0; i < registers.length; i++) {
            keyBinding[i].setKeyModifierAndCode(registers[i].modifier, registers[i].keycode);
        }
    }

    public int getPattern() {
        return this.Pattern;
    }

    protected int getScrollBarX() {
        return super.getScrollBarX() + 35;
    }

    public int getListWidth() {
        return super.getListWidth() + 32;
    }

    @SideOnly(Side.CLIENT)
    public class CategoryEntry implements GuiListExtended.IGuiListEntry {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.format(name);
            this.labelWidth = GuiKeyBindingListAltSet.this.mc.fontRenderer.getStringWidth(this.labelText);
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            GuiKeyBindingListAltSet.this.mc.fontRenderer.drawString(this.labelText, GuiKeyBindingListAltSet.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiKeyBindingListAltSet.this.mc.fontRenderer.FONT_HEIGHT - 1, 16777215);
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }
    }

    /**
     * このままだと Pattern ごとに入れないといけない -> 複雑化
     */
    @SideOnly(Side.CLIENT)
    public class SelectedButtonEntry implements GuiListExtended.IGuiListEntry {
        public int labelWidth = 0;
        public String Text = "";

        public SelectedButtonEntry() {
            this.Text = "Choose Pattern(Alt + Number 1 ~ 9)";
            this.labelWidth = GuiKeyBindingListAltSet.this.mc.fontRenderer.getStringWidth(Text);
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            GuiKeyBindingListAltSet.this.mc.fontRenderer.drawString(this.Text, GuiKeyBindingListAltSet.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiKeyBindingListAltSet.this.mc.fontRenderer.FONT_HEIGHT - 20, 16777215);

            //
            if (GuiKeyBindingListAltSet.this.Pattern != ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).getPatternID()) {
                ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).setPatternID(GuiKeyBindingListAltSet.this.Pattern);
            }

            int i = 0;
            for (GuiButtonAltSet button : GuiKeyBindingListAltSet.this.SelectButton) {
                button.x = x + (30 * i);//調整が必要
                button.y = y;

                button.drawButtonFix(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY, partialTicks, ButtonGuiControlIn);

                //GuiKeyBindingListAltSet.this.mc.fontRenderer.drawString(button.displayString, button.x + button.width / 2, button.y + (button.height - 8) / 2/*GuiKeyBindingListAltSet.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiKeyBindingListAltSet.this.mc.fontRenderer.FONT_HEIGHT - 1*/, 16777215);
                i++;
            }
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            boolean b = Arrays.stream(GuiKeyBindingListAltSet.this.SelectButton).anyMatch((i) -> i.mousePressed(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY));

            if (b) {
                for (GuiButtonAltSet button : GuiKeyBindingListAltSet.this.SelectButton)
                    button.enabled = true;


                for (GuiButtonAltSet button : GuiKeyBindingListAltSet.this.SelectButton) {
                    if (button.mousePressed(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY)) {

                        button.playPressSound(mc.getSoundHandler());

                        button.enabled = false;

                        GuiKeyBindingListAltSet.this.setPattern(button.id);//ID が Pattern と同じなため

                        return true;
                    }
                }
            }
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }
    }

    @SideOnly(Side.CLIENT)
    public class KeyEntry implements GuiListExtended.IGuiListEntry {
        private final KeyBinding keybinding;
        private final String keyDesc;
        private final GuiButtonAltSet btnChangeKeyBinding;
        private final GuiButtonAltSet btnReset;
        public boolean IsModKeyBind = false;
        public int Index = 0;

        private KeyEntry(KeyBinding name, int index, boolean IsKeyModBind) {
            this.keybinding = name;
            this.Index = index;
            this.keyDesc = I18n.format(name.getKeyDescription());
            this.btnChangeKeyBinding = new GuiButtonAltSet(0, 0, 0, 95, 20, I18n.format(name.getKeyDescription()));
            this.btnReset = new GuiButtonAltSet(0, 0, 0, 50, 20, I18n.format("controls.reset"));
            this.IsModKeyBind = IsKeyModBind;
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            boolean flag = GuiKeyBindingListAltSet.this.controlsScreen.buttonId == this.keybinding;
            GuiKeyBindingListAltSet.this.mc.fontRenderer.drawString(this.keyDesc, x + 90 - GuiKeyBindingListAltSet.this.maxListLabelWidth, y + slotHeight / 2 - GuiKeyBindingListAltSet.this.mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
            this.btnReset.x = x + 210;
            this.btnReset.y = y;
            this.btnReset.enabled = !this.keybinding.isSetToDefaultValue();
            this.btnReset.drawButtonFix(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY, partialTicks, ButtonGuiControlIn);
            this.btnChangeKeyBinding.x = x + 105;
            this.btnChangeKeyBinding.y = y;
            this.btnChangeKeyBinding.displayString = this.keybinding.getDisplayName();
            boolean flag1 = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G

            if (this.keybinding.getKeyCode() != 0) {
                if (IsModKeyBind) {
                    for (KeyBinding keybinding : ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).ModRegisteredBinding()) {
                        if (keybinding != this.keybinding && keybinding.conflicts(this.keybinding)) {
                            flag1 = true;
                            keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.keybinding);
                        }
                    }
                } else {
                    for (KeyBinding keybinding : ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).MCKeyBinding()) {
                        if (keybinding != this.keybinding && keybinding.conflicts(this.keybinding)) {
                            flag1 = true;
                            keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.keybinding);
                        }
                    }
                }
            }

            if (flag) {
                this.btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
            } else if (flag1) {
                this.btnChangeKeyBinding.displayString = (keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED) + this.btnChangeKeyBinding.displayString;
            }

            this.btnChangeKeyBinding.drawButtonFix(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY, partialTicks, ButtonGuiControlIn);
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (this.btnChangeKeyBinding.mousePressed(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY)) {
                this.btnChangeKeyBinding.playPressSound(mc.getSoundHandler());
                if (this.IsModKeyBind)
                    ((GuiControlsGetter) GuiKeyBindingListAltSet.this.controlsScreen).SetIndex(this.Index);
                GuiKeyBindingListAltSet.this.controlsScreen.buttonId = this.keybinding;
                return true;
            } else if (this.btnReset.mousePressed(GuiKeyBindingListAltSet.this.mc, mouseX, mouseY)) {
                this.btnReset.playPressSound(mc.getSoundHandler());
                this.keybinding.setToDefault();
                GuiKeyBindingListAltSet.this.mc.gameSettings.setOptionKeyBinding(this.keybinding, this.keybinding.getKeyCodeDefault());

                if (!GuiKeyBindingListAltSet.this.getIsVanilla()) {
                    int D = ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).getPatternID();
                    ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).setPatternID(GuiKeyBindingListAltSet.this.Pattern);
                    ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).SetKeyBindingRegister(new KeyBindingRegister(this.keybinding.getKeyDescription(), this.keybinding.getKeyCode(), this.keybinding.getKeyCategory(), this.keybinding.getKeyModifier()), this.Index);
                    ((GameSettingsExtended) GuiKeyBindingListAltSet.this.mc.gameSettings).setPatternID(D);
                }

                KeyBinding.resetKeyBindingArrayAndHash();
                return true;
            } else {
                return false;
            }
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnChangeKeyBinding.mouseReleased(x, y);
            this.btnReset.mouseReleased(x, y);
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }
    }
}