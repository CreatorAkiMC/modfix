package com.aki.modfix.util.fix;

import net.minecraft.client.settings.KeyBinding;

import javax.annotation.Nullable;

public interface GameSettingsExtended {
    /**
     * 右クリックやジャンプなど、パターンに含まないもの
     */
    KeyBinding[] MCKeyBinding();

    @Nullable
    KeyBinding[] ModRegisteredBinding();

    @Nullable
    KeyBindingRegister[] KeyBindingRegister();

    //登録用
    void SetModRegisteredBinding(KeyBinding[] ModCustomKeyBinding);

    /**
     * 0 ~ 8 まで
     */
    int getPatternID();

    void setPatternID(int Id);

    //切り替えはしない。
    //読み取り専用
    @Nullable
    KeyBinding[] getPatternKeyBindings(int id);

    void SetKeyBindingRegister(KeyBindingRegister register, int index);

    void ChangeKeyPatternEvent();

    void ReloadKeyBindingSettings();
}
