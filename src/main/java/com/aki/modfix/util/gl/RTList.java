package com.aki.modfix.util.gl;

import java.util.ArrayList;
import java.util.List;

public class RTList<T> {
    private int Index = 0;

    private List<T> Obj = new ArrayList<>();

    private T Select = null;

    public RTList(int Offset, List<T> list) {
        this.Index = Offset;
        this.Obj = list;
        this.Select = list.get(this.Index);
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
}
