package com.aki.modfix.util.gl;

import com.aki.modfix.util.cache.IntObjConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class RTList<T> {
    private int Index = 0;

    private List<T> Obj = new ArrayList<>();

    private T Select = null;

    public RTList(int size, int SelectOffset, IntFunction<T> consumer) {
        this.Index = SelectOffset;
        for(int i = 0; i < size; i++)
            Obj.add(i, consumer.apply(i));
        this.Select = this.Obj.get(this.Index);
    }

    public T ToNext() {
        this.Index = this.Index >= (this.Obj.size() - 1) ? 0 : this.Index + 1;
        this.Select = Obj.get(Index);
        return this.Select;
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
