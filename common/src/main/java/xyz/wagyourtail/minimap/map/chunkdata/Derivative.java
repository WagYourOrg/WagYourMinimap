package xyz.wagyourtail.minimap.map.chunkdata;

import java.io.Closeable;
import java.io.IOException;

public class Derivative<T> {
    private T contained;
    public boolean old;

    Derivative(boolean old, T contained) {
        this.old = old;
        this.contained = contained;
    }

    public T getContained() {
        return contained;
    }

    public synchronized void setContained(T newval) {
        if (this.contained instanceof Closeable) {
            try {
                ((Closeable) this.contained).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.contained instanceof AutoCloseable) {
            try {
                ((AutoCloseable) this.contained).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.contained = newval;
    }

}
