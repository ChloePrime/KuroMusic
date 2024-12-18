package cn.chloeprime.kuromusic.util;

@FunctionalInterface
public interface BuggySupplier<T> {
    T get() throws Exception;

    static <T> BuggySupplier<T> memoize(BuggySupplier<T> factory) {
        return new BuggyLazySupplier<>(factory);
    }
}
