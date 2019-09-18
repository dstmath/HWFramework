package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableMeteringRectangle implements MarshalQueryable<MeteringRectangle> {
    private static final int SIZE = 20;

    private class MarshalerMeteringRectangle extends Marshaler<MeteringRectangle> {
        protected MarshalerMeteringRectangle(TypeReference<MeteringRectangle> typeReference, int nativeType) {
            super(MarshalQueryableMeteringRectangle.this, typeReference, nativeType);
        }

        public void marshal(MeteringRectangle value, ByteBuffer buffer) {
            int xMin = value.getX();
            int yMin = value.getY();
            int weight = value.getMeteringWeight();
            buffer.putInt(xMin);
            buffer.putInt(yMin);
            buffer.putInt(value.getWidth() + xMin);
            buffer.putInt(value.getHeight() + yMin);
            buffer.putInt(weight);
        }

        public MeteringRectangle unmarshal(ByteBuffer buffer) {
            int xMin = buffer.getInt();
            int yMin = buffer.getInt();
            MeteringRectangle meteringRectangle = new MeteringRectangle(xMin, yMin, buffer.getInt() - xMin, buffer.getInt() - yMin, buffer.getInt());
            return meteringRectangle;
        }

        public int getNativeSize() {
            return 20;
        }
    }

    public Marshaler<MeteringRectangle> createMarshaler(TypeReference<MeteringRectangle> managedType, int nativeType) {
        return new MarshalerMeteringRectangle(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<MeteringRectangle> managedType, int nativeType) {
        return nativeType == 1 && MeteringRectangle.class.equals(managedType.getType());
    }
}
