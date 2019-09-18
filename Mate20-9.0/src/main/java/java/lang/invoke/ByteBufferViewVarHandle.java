package java.lang.invoke;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class ByteBufferViewVarHandle extends VarHandle {
    private boolean nativeByteOrder;

    private ByteBufferViewVarHandle(Class<?> arrayClass, ByteOrder byteOrder) {
        super(arrayClass.getComponentType(), byte[].class, false, ByteBuffer.class, Integer.TYPE);
        this.nativeByteOrder = byteOrder.equals(ByteOrder.nativeOrder());
    }

    static ByteBufferViewVarHandle create(Class<?> arrayClass, ByteOrder byteOrder) {
        return new ByteBufferViewVarHandle(arrayClass, byteOrder);
    }
}
