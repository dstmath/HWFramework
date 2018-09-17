package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.params.ReprocessFormatsMap;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;

public class MarshalQueryableReprocessFormatsMap implements MarshalQueryable<ReprocessFormatsMap> {

    private class MarshalerReprocessFormatsMap extends Marshaler<ReprocessFormatsMap> {
        protected MarshalerReprocessFormatsMap(TypeReference<ReprocessFormatsMap> typeReference, int nativeType) {
            super(MarshalQueryableReprocessFormatsMap.this, typeReference, nativeType);
        }

        public void marshal(ReprocessFormatsMap value, ByteBuffer buffer) {
            for (int input : StreamConfigurationMap.imageFormatToInternal(value.getInputs())) {
                buffer.putInt(input);
                int[] outputs = StreamConfigurationMap.imageFormatToInternal(value.getOutputs(input));
                buffer.putInt(outputs.length);
                for (int output : outputs) {
                    buffer.putInt(output);
                }
            }
        }

        public ReprocessFormatsMap unmarshal(ByteBuffer buffer) {
            int len = buffer.remaining() / 4;
            if (buffer.remaining() % 4 != 0) {
                throw new AssertionError("ReprocessFormatsMap was not TYPE_INT32");
            }
            int[] entries = new int[len];
            buffer.asIntBuffer().get(entries);
            return new ReprocessFormatsMap(entries);
        }

        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }

        public int calculateMarshalSize(ReprocessFormatsMap value) {
            int length = 0;
            for (int input : value.getInputs()) {
                length = ((length + 1) + 1) + value.getOutputs(input).length;
            }
            return length * 4;
        }
    }

    public Marshaler<ReprocessFormatsMap> createMarshaler(TypeReference<ReprocessFormatsMap> managedType, int nativeType) {
        return new MarshalerReprocessFormatsMap(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<ReprocessFormatsMap> managedType, int nativeType) {
        return nativeType == 1 ? managedType.getType().equals(ReprocessFormatsMap.class) : false;
    }
}
