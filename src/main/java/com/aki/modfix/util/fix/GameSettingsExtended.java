package com.aki.modfix.util.fix;

import net.minecraft.client.settings.KeyBinding;

public interface GameSettingsExtended {
    /**
     * 右クリックやジャンプなど、パターンに含まないもの
     */
    KeyBinding[] MCKeyBinding();

    KeyBinding[] ModRegisteredBinding();

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
    KeyBinding[] getPatternKeyBindings(int id);

    void SetKeyBindingRegister(KeyBindingRegister register, int index);

    void ChangeKeyPatternEvent();

    void ReloadKeyBindingSettings();
}
