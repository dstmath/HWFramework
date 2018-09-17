package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableBoolean implements MarshalQueryable<Boolean> {

    private class MarshalerBoolean extends Marshaler<Boolean> {
        protected MarshalerBoolean(TypeReference<Boolean> typeReference, int nativeType) {
            super(MarshalQueryableBoolean.this, typeReference, nativeType);
        }

        public void marshal(Boolean value, ByteBuffer buffer) {
            buffer.put((byte) (value.booleanValue() ? 1 : 0));
        }

        public Boolean unmarshal(ByteBuffer buffer) {
            boolean z = false;
            if (buffer.get() != (byte) 0) {
                z = true;
            }
            return Boolean.valueOf(z);
        }

        public int getNativeSize() {
            return 1;
        }
    }

    public Marshaler<Boolean> createMarshaler(TypeReference<Boolean> managedType, int nativeType) {
        return new MarshalerBoolean(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<Boolean> managedType, int nativeType) {
        if ((Boolean.class.equals(managedType.getType()) || Boolean.TYPE.equals(managedType.getType())) && nativeType == 0) {
            return true;
        }
        return false;
    }
}
