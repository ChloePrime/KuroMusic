package cn.chloeprime.kuromusic.util;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Queue<T> bag = new ConcurrentLinkedDeque<>();

    public ObjectPool(Supplier<T> constructor) {
        this.constructor = constructor;
    }

    private final Supplier<T> constructor;

    public T poll() {
        return Objects.requireNonNullElseGet(bag.poll(), constructor);
    }

    public void offer(T instance) {
        if (instance != null) {
            bag.offer(instance);
        }
    }
}
