package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.SizeF;
import java.nio.ByteBuffer;

public class MarshalQueryableSizeF implements MarshalQueryable<SizeF> {
    private static final int SIZE = 8;

    private class MarshalerSizeF extends Marshaler<SizeF> {
        protected MarshalerSizeF(TypeReference<SizeF> typeReference, int nativeType) {
            super(MarshalQueryableSizeF.this, typeReference, nativeType);
        }

        public void marshal(SizeF value, ByteBuffer buffer) {
            buffer.putFloat(value.getWidth());
            buffer.putFloat(value.getHeight());
        }

        public SizeF unmarshal(ByteBuffer buffer) {
            return new SizeF(buffer.getFloat(), buffer.getFloat());
        }

        public int getNativeSize() {
            return 8;
        }
    }

    public Marshaler<SizeF> createMarshaler(TypeReference<SizeF> managedType, int nativeType) {
        return new MarshalerSizeF(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<SizeF> managedType, int nativeType) {
        return nativeType == 2 && SizeF.class.equals(managedType.getType());
    }
}
