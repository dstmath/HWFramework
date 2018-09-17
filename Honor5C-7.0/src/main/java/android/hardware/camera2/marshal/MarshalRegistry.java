package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;
import java.util.HashMap;
import java.util.List;

public class MarshalRegistry {
    private static final Object sMarshalLock = null;
    private static final HashMap<MarshalToken<?>, Marshaler<?>> sMarshalerMap = null;
    private static final List<MarshalQueryable<?>> sRegisteredMarshalQueryables = null;

    private static class MarshalToken<T> {
        private final int hash;
        final int nativeType;
        final TypeReference<T> typeReference;

        public MarshalToken(TypeReference<T> typeReference, int nativeType) {
            this.typeReference = typeReference;
            this.nativeType = nativeType;
            this.hash = typeReference.hashCode() ^ nativeType;
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (!(other instanceof MarshalToken)) {
                return false;
            }
            MarshalToken<?> otherToken = (MarshalToken) other;
            if (this.typeReference.equals(otherToken.typeReference) && this.nativeType == otherToken.nativeType) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.hash;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.marshal.MarshalRegistry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.marshal.MarshalRegistry.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.marshal.MarshalRegistry.<clinit>():void");
    }

    public static <T> void registerMarshalQueryable(MarshalQueryable<T> queryable) {
        synchronized (sMarshalLock) {
            sRegisteredMarshalQueryables.add(queryable);
        }
    }

    public static <T> Marshaler<T> getMarshaler(TypeReference<T> typeToken, int nativeType) {
        Marshaler<T> marshaler;
        synchronized (sMarshalLock) {
            MarshalToken<T> marshalToken = new MarshalToken(typeToken, nativeType);
            marshaler = (Marshaler) sMarshalerMap.get(marshalToken);
            if (marshaler == null) {
                if (sRegisteredMarshalQueryables.size() == 0) {
                    throw new AssertionError("No available query marshalers registered");
                }
                for (MarshalQueryable<?> potentialMarshaler : sRegisteredMarshalQueryables) {
                    MarshalQueryable<T> castedPotential = potentialMarshaler;
                    if (potentialMarshaler.isTypeMappingSupported(typeToken, nativeType)) {
                        marshaler = potentialMarshaler.createMarshaler(typeToken, nativeType);
                        break;
                    }
                }
                if (marshaler == null) {
                    throw new UnsupportedOperationException("Could not find marshaler that matches the requested combination of type reference " + typeToken + " and native type " + MarshalHelpers.toStringNativeType(nativeType));
                }
                sMarshalerMap.put(marshalToken, marshaler);
            }
        }
        return marshaler;
    }

    private MarshalRegistry() {
        throw new AssertionError();
    }
}
