package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;

public interface MarshalQueryable<T> {
    Marshaler<T> createMarshaler(TypeReference<T> typeReference, int i);

    boolean isTypeMappingSupported(TypeReference<T> typeReference, int i);
}
