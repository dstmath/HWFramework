package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;
import com.android.internal.util.Preconditions;
import java.nio.ByteBuffer;

public abstract class Marshaler<T> {
    public static int NATIVE_SIZE_DYNAMIC = -1;
    protected final int mNativeType;
    protected final TypeReference<T> mTypeReference;

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
