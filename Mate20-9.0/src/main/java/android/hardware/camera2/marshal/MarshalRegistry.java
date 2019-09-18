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

    private static class MarshalToken<T> {
        private final int hash;
        final int nativeType;
        final TypeReference<T> typeReference;

        public MarshalToken(TypeReference<T> typeReference2, int nativeType2) {
            this.typeReference = typeReference2;
            this.nativeType = nativeType2;
            this.hash = typeReference2.hashCode() ^ nativeType2;
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
            MarshalToken<T> marshalToken = new MarshalToken<>(typeToken, nativeType);
            marshaler = sMarshalerMap.get(marshalToken);
            if (marshaler == null) {
                if (sRegisteredMarshalQueryables.size() != 0) {
                    Iterator<MarshalQueryable<?>> it = sRegisteredMarshalQueryables.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        MarshalQueryable<T> castedPotential = it.next();
                        if (castedPotential.isTypeMappingSupported(typeToken, nativeType)) {
                            marshaler = castedPotential.createMarshaler(typeToken, nativeType);
                            break;
                        }
                    }
                    if (marshaler != null) {
                        sMarshalerMap.put(marshalToken, marshaler);
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

    private MarshalRegistry() {
        throw new AssertionError();
    }
}
