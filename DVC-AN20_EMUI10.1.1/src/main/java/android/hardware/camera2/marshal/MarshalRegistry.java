package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MarshalRegistry {
    private static final Object sMarshalLock = new Object();
    private static final HashMap<MarshalToken<?>, Marshaler<?>> sMarshalerMap = new HashMap<>();
    private static final List<MarshalQueryable<?>> sRegisteredMarshalQueryables = new ArrayList();

    public static <T> void registerMarshalQueryable(MarshalQueryable<T> queryable) {
        synchronized (sMarshalLock) {
            sRegisteredMarshalQueryables.add(queryable);
        }
    }

    public static <T> Marshaler<T> getMarshaler(TypeReference<T> typeToken, int nativeType) {
        Marshaler<T> marshaler;
        synchronized (sMarshalLock) {
            MarshalToken<?> marshalToken = new MarshalToken<>(typeToken, nativeType);
            Marshaler<T> marshaler2 = (Marshaler<T>) sMarshalerMap.get(marshalToken);
            if (marshaler2 == null) {
                if (sRegisteredMarshalQueryables.size() != 0) {
                    Iterator<MarshalQueryable<?>> it = sRegisteredMarshalQueryables.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        MarshalQueryable<?> potentialMarshaler = it.next();
                        if (potentialMarshaler.isTypeMappingSupported(typeToken, nativeType)) {
                            marshaler2 = (Marshaler<T>) potentialMarshaler.createMarshaler(typeToken, nativeType);
                            break;
                        }
                    }
                    if (marshaler2 != null) {
                        sMarshalerMap.put(marshalToken, marshaler2);
                    } else {
                        throw new UnsupportedOperationException("Could not find marshaler that matches the requested combination of type reference " + typeToken + " and native type " + MarshalHelpers.toStringNativeType(nativeType));
                    }
                } else {
                    throw new AssertionError("No available query marshalers registered");
                }
            }
        }
        return marshaler;
    }

    /* access modifiers changed from: private */
    public static class MarshalToken<T> {
        private final int hash;
        final int nativeType;
        final TypeReference<T> typeReference;

        public MarshalToken(TypeReference<T> typeReference2, int nativeType2) {
            this.typeReference = typeReference2;
            this.nativeType = nativeType2;
            this.hash = typeReference2.hashCode() ^ nativeType2;
        }

        public boolean equals(Object other) {
            if (!(other instanceof MarshalToken)) {
                return false;
            }
            MarshalToken<?> otherToken = (MarshalToken) other;
            if (!this.typeReference.equals(otherToken.typeReference) || this.nativeType != otherToken.nativeType) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.hash;
        }
    }

    private MarshalRegistry() {
        throw new AssertionError();
    }
}
