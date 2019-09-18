package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableStreamConfigurationDuration implements MarshalQueryable<StreamConfigurationDuration> {
    private static final long MASK_UNSIGNED_INT = 4294967295L;
    private static final int SIZE = 32;

    private class MarshalerStreamConfigurationDuration extends Marshaler<StreamConfigurationDuration> {
        protected MarshalerStreamConfigurationDuration(TypeReference<StreamConfigurationDuration> typeReference, int nativeType) {
            super(MarshalQueryableStreamConfigurationDuration.this, typeReference, nativeType);
        }

        public void marshal(StreamConfigurationDuration value, ByteBuffer buffer) {
            buffer.putLong(((long) value.getFormat()) & MarshalQueryableStreamConfigurationDuration.MASK_UNSIGNED_INT);
            buffer.putLong((long) value.getWidth());
            buffer.putLong((long) value.getHeight());
            buffer.putLong(value.getDuration());
        }

        public StreamConfigurationDuration unmarshal(ByteBuffer buffer) {
            StreamConfigurationDuration streamConfigurationDuration = new StreamConfigurationDuration((int) buffer.getLong(), (int) buffer.getLong(), (int) buffer.getLong(), buffer.getLong());
            return streamConfigurationDuration;
        }

        public int getNativeSize() {
            return 32;
        }
    }

    public Marshaler<StreamConfigurationDuration> createMarshaler(TypeReference<StreamConfigurationDuration> managedType, int nativeType) {
        return new MarshalerStreamConfigurationDuration(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<StreamConfigurationDuration> managedType, int nativeType) {
        return nativeType == 3 && StreamConfigurationDuration.class.equals(managedType.getType());
    }
}
