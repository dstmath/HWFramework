package java.lang.invoke;

final class ArrayElementVarHandle extends VarHandle {
    private ArrayElementVarHandle(Class<?> arrayClass) {
        super(arrayClass.getComponentType(), arrayClass, false, arrayClass, Integer.TYPE);
    }

    static ArrayElementVarHandle create(Class<?> arrayClass) {
        return new ArrayElementVarHandle(arrayClass);
    }
}
