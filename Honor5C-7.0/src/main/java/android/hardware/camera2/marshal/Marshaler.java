package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;
import com.android.internal.util.Preconditions;
import java.nio.ByteBuffer;

public abstract class Marshaler<T> {
    public static int NATIVE_SIZE_DYNAMIC;
    protected final int mNativeType;
    protected final TypeReference<T> mTypeReference;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.marshal.Marshaler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.marshal.Marshaler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.marshal.Marshaler.<clinit>():void");
    }

    public abstract int getNativeSize();

    public abstract void marshal(T t, ByteBuffer byteBuffer);

    public abstract T unmarshal(ByteBuffer byteBuffer);

    protected Marshaler(MarshalQueryable<T> query, TypeReference<T> typeReference, int nativeType) {
        this.mTypeReference = (TypeReference) Preconditions.checkNotNull(typeReference, "typeReference must not be null");
        this.mNativeType = MarshalHelpers.checkNativeType(nativeType);
        if (!query.isTypeMappingSupported(typeReference, nativeType)) {
            throw new UnsupportedOperationException("Unsupported type marshaling for managed type " + typeReference + " and native type " + MarshalHelpers.toStringNativeType(nativeType));
        }
    }

    public int calculateMarshalSize(T t) {
        int nativeSize = getNativeSize();
        if (nativeSize != NATIVE_SIZE_DYNAMIC) {
            return nativeSize;
        }
        throw new AssertionError("Override this function for dynamically-sized objects");
    }

    public TypeReference<T> getTypeReference() {
        return this.mTypeReference;
    }

    public int getNativeType() {
        return this.mNativeType;
    }
}
