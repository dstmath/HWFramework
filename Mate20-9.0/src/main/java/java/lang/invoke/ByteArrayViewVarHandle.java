package java.lang.invoke;

import java.nio.ByteOrder;

final class ByteArrayViewVarHandle extends VarHandle {
    private boolean nativeByteOrder;

    private ByteArrayViewVarHandle(Class<?> arrayClass, ByteOrder byteOrder) {
        super(arrayClass.getComponentType(), byte[].class, false, byte[].class, Integer.TYPE);
        this.nativeByteOrder = byteOrder.equals(ByteOrder.nativeOrder());
    }

    static ByteArrayViewVarHandle create(Class<?> arrayClass, ByteOrder byteOrder) {
        return new ByteArrayViewVarHandle(arrayClass, byteOrder);
    }
}
