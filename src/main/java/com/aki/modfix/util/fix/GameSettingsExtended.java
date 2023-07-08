package com.aki.modfix.util.fix;

import net.minecraft.client.settings.KeyBinding;

public interface GameSettingsExtended {
    /**
     * 右クリックやジャンプなど、パターンに含まないもの
     * */
    KeyBinding[] MCKeyBinding();

    KeyBinding[] ModRegisteredBinding();

    KeyBindingRegister[] KeyBindingRegister();

    //登録用
    void SetModRegisteredBinding(KeyBinding[] ModCustomKeyBinding);

    /**
     * 1 ~ 9 まで
     * */
    int getPatternID();

    void setPatternID(int Id);

    void SetKeyBindingRegister(KeyBindingRegister register, int index);

    void ChangeKeyPatternEvent();
}
