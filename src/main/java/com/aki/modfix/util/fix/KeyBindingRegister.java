package com.aki.modfix.util.fix;

import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindingRegister {
    public String description = "";
    public int keycode = 0;
    public String Category = "";
    public KeyModifier modifier = null;

    public KeyBindingRegister(String description, int keyCode, String category, KeyModifier modifier) {
        this.description = description;
        this.keycode = keyCode;
        this.Category = category;
        this.modifier = modifier;
    }

    public void SetData(String description, int keyCode, String category, KeyModifier modifier) {
        this.description = description;
        this.keycode = keyCode;
        this.Category = category;
        this.modifier = modifier;
    }

    @Override
    public String toString() {
        return "KeyBindingRegister{" + "description='" + description + ", keycode=" + keycode + ", Category='" + Category + ", modifier=" + modifier + '}';
    }
}
