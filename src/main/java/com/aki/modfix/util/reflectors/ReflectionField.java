package com.aki.modfix.util.reflectors;

import java.lang.reflect.Field;

public class ReflectionField<T> {

    private final Field field;

    public ReflectionField(Field field) {
        this.field = field;
    }

    public ReflectionField(Class<?> clazz, String obfuscatedName, String deobfuscatedName) {
        Field f = null;
        try {
            f = clazz.getDeclaredField(obfuscatedName);
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                f = clazz.getDeclaredField(deobfuscatedName);
                f.setAccessible(true);
            } catch (NoSuchFieldException e1) {
                // ignore
            }
        }
        this.field = f;
    }

    public ReflectionField(String className, String obfuscatedName, String deobfuscatedName) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            this.field = null;
            return;
        }
        Field f = null;
        try {
            f = clazz.getDeclaredField(obfuscatedName);
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                f = clazz.getDeclaredField(deobfuscatedName);
                f.setAccessible(true);
            } catch (NoSuchFieldException e1) {
                // ignore
            }
        }
        this.field = f;
    }

    public void set(Object obj, T value) {
        try {
            this.field.set(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBoolean(Object obj, boolean value) {
        try {
            this.field.setBoolean(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setByte(Object obj, byte value) {
        try {
            this.field.setByte(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setShort(Object obj, short value) {
        try {
            this.field.setShort(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInt(Object obj, int value) {
        try {
            this.field.setInt(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLong(Object obj, long value) {
        try {
            this.field.setLong(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFloat(Object obj, float value) {
        try {
            this.field.setFloat(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDouble(Object obj, double value) {
        try {
            this.field.setDouble(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setChar(Object obj, char value) {
        try {
            this.field.setChar(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public T get(Object obj) {
        try {
            return (T) this.field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getBoolean(Object obj) {
        try {
            return this.field.getBoolean(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public byte getByte(Object obj) {
        try {
            return this.field.getByte(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public short getShort(Object obj) {
        try {
            return this.field.getShort(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int getInt(Object obj) {
        try {
            return this.field.getInt(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public long getLong(Object obj) {
        try {
            return this.field.getLong(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public float getFloat(Object obj) {
        try {
            return this.field.getFloat(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public double getDouble(Object obj) {
        try {
            return this.field.getDouble(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public char getChar(Object obj) {
        try {
            return this.field.getChar(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPresent() {
        return this.field != null;
    }

}
