package java.lang;

public final class JavaLangAccess {
    private JavaLangAccess() {
    }

    public static <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> klass) {
        return (Enum[]) klass.getEnumConstantsShared();
    }
}
