package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.RecommendedStreamConfiguration;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableRecommendedStreamConfiguration implements MarshalQueryable<RecommendedStreamConfiguration> {
    private static final int SIZE = 20;

    private class MarshalerRecommendedStreamConfiguration extends Marshaler<RecommendedStreamConfiguration> {
        protected MarshalerRecommendedStreamConfiguration(TypeReference<RecommendedStreamConfiguration> typeReference, int nativeType) {
            super(MarshalQueryableRecommendedStreamConfiguration.this, typeReference, nativeType);
        }

        public void marshal(RecommendedStreamConfiguration value, ByteBuffer buffer) {
            buffer.putInt(value.getWidth());
            buffer.putInt(value.getHeight());
            buffer.putInt(value.getFormat());
            buffer.putInt(value.isInput() ? 1 : 0);
            buffer.putInt(value.getUsecaseBitmap());
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public RecommendedStreamConfiguration unmarshal(ByteBuffer buffer) {
            return new RecommendedStreamConfiguration(buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt() != 0, buffer.getInt());
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return 20;
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<RecommendedStreamConfiguration> createMarshaler(TypeReference<RecommendedStreamConfiguration> managedType, int nativeType) {
        return new MarshalerRecommendedStreamConfiguration(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<RecommendedStreamConfiguration> managedType, int nativeType) {
        if (nativeType != 1 || !managedType.getType().equals(RecommendedStreamConfiguration.class)) {
            return false;
        }
        return true;
    }
}
