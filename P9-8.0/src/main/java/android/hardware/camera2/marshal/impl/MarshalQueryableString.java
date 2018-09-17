package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MarshalQueryableString implements MarshalQueryable<String> {
    private static final boolean DEBUG = false;
    private static final byte NUL = (byte) 0;
    private static final String TAG = MarshalQueryableString.class.getSimpleName();

    private class MarshalerString extends Marshaler<String> {
        protected MarshalerString(TypeReference<String> typeReference, int nativeType) {
            super(MarshalQueryableString.this, typeReference, nativeType);
        }

        public void marshal(String value, ByteBuffer buffer) {
            buffer.put(value.getBytes(PreloadHolder.UTF8_CHARSET));
            buffer.put((byte) 0);
        }

        public int calculateMarshalSize(String value) {
            return value.getBytes(PreloadHolder.UTF8_CHARSET).length + 1;
        }

        public String unmarshal(ByteBuffer buffer) {
            buffer.mark();
            boolean foundNull = false;
            int stringLength = 0;
            while (buffer.hasRemaining()) {
                if (buffer.get() == (byte) 0) {
                    foundNull = true;
                    break;
                }
                stringLength++;
            }
            if (foundNull) {
                buffer.reset();
                byte[] strBytes = new byte[(stringLength + 1)];
                buffer.get(strBytes, 0, stringLength + 1);
                return new String(strBytes, 0, stringLength, PreloadHolder.UTF8_CHARSET);
            }
            throw new UnsupportedOperationException("Strings must be null-terminated");
        }

        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }
    }

    private static class PreloadHolder {
        public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

        private PreloadHolder() {
        }
    }

    public Marshaler<String> createMarshaler(TypeReference<String> managedType, int nativeType) {
        return new MarshalerString(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<String> managedType, int nativeType) {
        return nativeType == 0 ? String.class.equals(managedType.getType()) : false;
    }
}
