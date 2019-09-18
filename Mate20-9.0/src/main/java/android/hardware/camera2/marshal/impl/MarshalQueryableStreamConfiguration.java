package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableStreamConfiguration implements MarshalQueryable<StreamConfiguration> {
    private static final int SIZE = 16;

    private class MarshalerStreamConfiguration extends Marshaler<StreamConfiguration> {
        protected MarshalerStreamConfiguration(TypeReference<StreamConfiguration> typeReference, int nativeType) {
            super(MarshalQueryableStreamConfiguration.this, typeReference, nativeType);
        }

        public void marshal(StreamConfiguration value, ByteBuffer buffer) {
            buffer.putInt(value.getFormat());
            buffer.putInt(value.getWidth());
            buffer.putInt(value.getHeight());
            buffer.putInt(value.isInput() ? 1 : 0);
        }

        public StreamConfiguration unmarshal(ByteBuffer buffer) {
            return new StreamConfiguration(buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt() != 0);
        }

        public int getNativeSize() {
            return 16;
        }
    }

    public Marshaler<StreamConfiguration> createMarshaler(TypeReference<StreamConfiguration> managedType, int nativeType) {
        return new MarshalerStreamConfiguration(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<StreamConfiguration> managedType, int nativeType) {
        return nativeType == 1 && managedType.getType().equals(StreamConfiguration.class);
    }
}
