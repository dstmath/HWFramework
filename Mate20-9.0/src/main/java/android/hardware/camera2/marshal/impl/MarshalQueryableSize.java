package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Size;
import java.nio.ByteBuffer;

public class MarshalQueryableSize implements MarshalQueryable<Size> {
    private static final int SIZE = 8;

    private class MarshalerSize extends Marshaler<Size> {
        protected MarshalerSize(TypeReference<Size> typeReference, int nativeType) {
            super(MarshalQueryableSize.this, typeReference, nativeType);
        }

        public void marshal(Size value, ByteBuffer buffer) {
            buffer.putInt(value.getWidth());
            buffer.putInt(value.getHeight());
        }

        public Size unmarshal(ByteBuffer buffer) {
            return new Size(buffer.getInt(), buffer.getInt());
        }

        public int getNativeSize() {
            return 8;
        }
    }

    public Marshaler<Size> createMarshaler(TypeReference<Size> managedType, int nativeType) {
        return new MarshalerSize(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<Size> managedType, int nativeType) {
        return nativeType == 1 && Size.class.equals(managedType.getType());
    }
}
