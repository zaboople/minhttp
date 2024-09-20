package org.blorp;

/** This is a utility for dealing with java lambdas that don't cooperate with Exceptions */
public class Except {
    public static interface ExceptionalRunnable {
        public void run() throws Exception;
    }
    public static interface ExceptionalSupplier<T> {
        public T get() throws Exception;
    }
    @FunctionalInterface
    public static interface ExceptionalConsumer<T> {
        public void accept(T t) throws Exception;
    }
    public static <T> T get(ExceptionalSupplier<T> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void run(ExceptionalRunnable er) {
        try {
            er.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void runExcept(ExceptionalRunnable er) throws Exception {
        Exception caught=null;
        try {
            er.run();
        } catch (Exception e) {
            caught=e;
        }
        if (caught!=null) throw caught;
    }
}