package xyz.wagyourtail.minimap.map.chunkdata;

public class Derivative<T> {
    public T contained;
    public boolean old;

    Derivative(boolean old, T contained) {
        this.old = old;
        this.contained = contained;
    }

}
