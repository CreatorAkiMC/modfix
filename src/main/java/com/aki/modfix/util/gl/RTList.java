package com.aki.modfix.util.gl;

import com.aki.mcutils.APICore.Utils.cache.IntObjConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class RTList<T> {
    private int Index;

    private final List<T> Obj = new ArrayList<>();

    private T Select;

    public RTList(int size, int SelectOffset, IntFunction<T> consumer) {
        this.Index = SelectOffset;
        for (int i = 0; i < size; i++)
            Obj.add(i, consumer.apply(i));
        this.Select = this.Obj.get(this.Index);
    }

    public void ToNext() {
        this.Index = this.Index >= (this.Obj.size() - 1) ? 0 : this.Index + 1;
        this.Select = Obj.get(Index);
    }

    public List<T> getList() {
        return this.Obj;
    }

    public final int getIndex() {
        return this.Index;
    }

    public final int Size() {
        return this.Obj.size();
    }

    public T getSelect() {
        return this.Select;
    }

    public void setSelect(T object) {
        this.Obj.set(this.Index, object);
    }

    public T get(int Index) {
        return this.Obj.get(Index);
    }

    public void forEach(IntObjConsumer<T> lamda) {
        for (int i = 0; i < Size(); i++)
            lamda.accept(i, this.Obj.get(i));
    }
}
