package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableColorSpaceTransform implements MarshalQueryable<ColorSpaceTransform> {
    private static final int ELEMENTS_INT32 = 18;
    private static final int SIZE = 72;

    private class MarshalerColorSpaceTransform extends Marshaler<ColorSpaceTransform> {
        protected MarshalerColorSpaceTransform(TypeReference<ColorSpaceTransform> typeReference, int nativeType) {
            super(MarshalQueryableColorSpaceTransform.this, typeReference, nativeType);
        }

        public void marshal(ColorSpaceTransform value, ByteBuffer buffer) {
            int[] transformAsArray = new int[18];
            value.copyElements(transformAsArray, 0);
            for (int i = 0; i < 18; i++) {
                buffer.putInt(transformAsArray[i]);
            }
        }

        public ColorSpaceTransform unmarshal(ByteBuffer buffer) {
            int[] transformAsArray = new int[18];
            for (int i = 0; i < 18; i++) {
                transformAsArray[i] = buffer.getInt();
            }
            return new ColorSpaceTransform(transformAsArray);
        }

        public int getNativeSize() {
            return 72;
        }
    }

    public Marshaler<ColorSpaceTransform> createMarshaler(TypeReference<ColorSpaceTransform> managedType, int nativeType) {
        return new MarshalerColorSpaceTransform(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<ColorSpaceTransform> managedType, int nativeType) {
        return nativeType == 5 && ColorSpaceTransform.class.equals(managedType.getType());
    }
}
