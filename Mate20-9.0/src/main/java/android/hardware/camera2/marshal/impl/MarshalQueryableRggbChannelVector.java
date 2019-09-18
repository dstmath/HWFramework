package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableRggbChannelVector implements MarshalQueryable<RggbChannelVector> {
    private static final int SIZE = 16;

    private class MarshalerRggbChannelVector extends Marshaler<RggbChannelVector> {
        protected MarshalerRggbChannelVector(TypeReference<RggbChannelVector> typeReference, int nativeType) {
            super(MarshalQueryableRggbChannelVector.this, typeReference, nativeType);
        }

        public void marshal(RggbChannelVector value, ByteBuffer buffer) {
            for (int i = 0; i < 4; i++) {
                buffer.putFloat(value.getComponent(i));
            }
        }

        public RggbChannelVector unmarshal(ByteBuffer buffer) {
            return new RggbChannelVector(buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        }

        public int getNativeSize() {
            return 16;
        }
    }

    public Marshaler<RggbChannelVector> createMarshaler(TypeReference<RggbChannelVector> managedType, int nativeType) {
        return new MarshalerRggbChannelVector(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<RggbChannelVector> managedType, int nativeType) {
        return nativeType == 2 && RggbChannelVector.class.equals(managedType.getType());
    }
}
