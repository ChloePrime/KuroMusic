package cn.chloeprime.kuromusic.util;

final class BuggyLazySupplier<T> implements BuggySupplier<T> {
    final BuggySupplier<T> delegate;
    transient volatile boolean initialized;
    transient T value;

    BuggyLazySupplier(BuggySupplier<T> delegate) {
        this.delegate = delegate;
    }

    public T get() throws Exception {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    T t = delegate.get();
                    value = t;
                    initialized = true;
                    return t;
                }
            }
        }
        return value;
    }
}
