package java.util.function;

@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {
    static <T> UnaryOperator<T> identity() {
        return $$Lambda$UnaryOperator$p7kKvUH5OpW1KFw_KNJNdNw8HUE.INSTANCE;
    }

    static /* synthetic */ Object lambda$identity$0(Object t) {
        return t;
    }
}
