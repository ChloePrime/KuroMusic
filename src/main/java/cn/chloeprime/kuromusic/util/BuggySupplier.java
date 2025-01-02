package cn.chloeprime.kuromusic.util;

import com.machinezoo.noexception.Exceptions;

@FunctionalInterface
public interface BuggySupplier<T> {
    T get() throws Exception;

    default T getSilently() {
        try {
            return get();
        } catch (Exception ex) {
            throw Exceptions.sneak().handle(ex);
        }
    }

    static <T> T getSilently(BuggySupplier<T> action) {
        return action.getSilently();
    }

    static <T> BuggySupplier<T> memoize(BuggySupplier<T> factory) {
        return new BuggyLazySupplier<>(factory);
    }
}
