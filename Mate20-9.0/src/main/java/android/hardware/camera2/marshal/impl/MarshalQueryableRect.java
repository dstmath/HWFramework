package android.hardware.camera2.marshal.impl;

import android.graphics.Rect;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableRect implements MarshalQueryable<Rect> {
    private static final int SIZE = 16;

    private class MarshalerRect extends Marshaler<Rect> {
        protected MarshalerRect(TypeReference<Rect> typeReference, int nativeType) {
            super(MarshalQueryableRect.this, typeReference, nativeType);
        }

        public void marshal(Rect value, ByteBuffer buffer) {
            buffer.putInt(value.left);
            buffer.putInt(value.top);
            buffer.putInt(value.width());
            buffer.putInt(value.height());
        }

        public Rect unmarshal(ByteBuffer buffer) {
            int left = buffer.getInt();
            int top = buffer.getInt();
            return new Rect(left, top, left + buffer.getInt(), top + buffer.getInt());
        }

        public int getNativeSize() {
            return 16;
        }
    }

    public Marshaler<Rect> createMarshaler(TypeReference<Rect> managedType, int nativeType) {
        return new MarshalerRect(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<Rect> managedType, int nativeType) {
        return nativeType == 1 && Rect.class.equals(managedType.getType());
    }
}
