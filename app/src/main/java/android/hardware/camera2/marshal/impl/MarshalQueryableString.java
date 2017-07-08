package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MarshalQueryableString implements MarshalQueryable<String> {
    private static final boolean DEBUG = false;
    private static final byte NUL = (byte) 0;
    private static final String TAG = null;
    private static final Charset UTF8_CHARSET = null;

    private class MarshalerString extends Marshaler<String> {
        protected MarshalerString(TypeReference<String> typeReference, int nativeType) {
            super(MarshalQueryableString.this, typeReference, nativeType);
        }

        public void marshal(String value, ByteBuffer buffer) {
            buffer.put(value.getBytes(MarshalQueryableString.UTF8_CHARSET));
            buffer.put((byte) 0);
        }

        public int calculateMarshalSize(String value) {
            return value.getBytes(MarshalQueryableString.UTF8_CHARSET).length + 1;
        }

        public String unmarshal(ByteBuffer buffer) {
            buffer.mark();
            boolean foundNull = false;
            int stringLength = 0;
            while (buffer.hasRemaining()) {
                if (buffer.get() == null) {
                    foundNull = true;
                    break;
                }
                stringLength++;
            }
            if (foundNull) {
                buffer.reset();
                byte[] strBytes = new byte[(stringLength + 1)];
                buffer.get(strBytes, 0, stringLength + 1);
                return new String(strBytes, 0, stringLength, MarshalQueryableString.UTF8_CHARSET);
            }
            throw new UnsupportedOperationException("Strings must be null-terminated");
        }

        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.marshal.impl.MarshalQueryableString.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.marshal.impl.MarshalQueryableString.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.marshal.impl.MarshalQueryableString.<clinit>():void");
    }

    public Marshaler<String> createMarshaler(TypeReference<String> managedType, int nativeType) {
        return new MarshalerString(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<String> managedType, int nativeType) {
        return nativeType == 0 ? String.class.equals(managedType.getType()) : false;
    }
}
