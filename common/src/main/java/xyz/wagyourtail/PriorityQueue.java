package xyz.wagyourtail;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * minimal priority queue impl
 *
 * @param <E>
 */
public class PriorityQueue<E> {
    private final List<List<E>> priorityMap = new ArrayList<>();
    private int count = 0;

    public synchronized void put(E element, int priority) {
        while (priorityMap.size() <= priority) {
            priorityMap.add(new LinkedList<>());
        }
        priorityMap.get(priority).add(element);
        ++count;
        this.notifyAll();
    }

    public synchronized E take() throws InterruptedException {
        while (count == 0) {
            this.wait();
        }
        for (List<E> es : priorityMap) {
            if (es.size() > 0) {
                --count;
                return es.remove(0);
            }
        }
        throw new NullPointerException("WHAT!");
    }

    public synchronized E peek() {
        for (List<E> es : priorityMap) {
            if (es.size() > 0) return es.get(0);
        }
        return null;
    }

    public synchronized int size() {
        return count;
    }

    public synchronized void waitForEmpty() throws InterruptedException {
        while (count != 0) this.wait();
    }

}
