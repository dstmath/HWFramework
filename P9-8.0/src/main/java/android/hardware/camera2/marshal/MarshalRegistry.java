package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MarshalRegistry {
    private static final Object sMarshalLock = new Object();
    private static final HashMap<MarshalToken<?>, Marshaler<?>> sMarshalerMap = new HashMap();
    private static final List<MarshalQueryable<?>> sRegisteredMarshalQueryables = new ArrayList();

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
