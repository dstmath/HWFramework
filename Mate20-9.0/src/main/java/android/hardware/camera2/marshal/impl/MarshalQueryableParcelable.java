package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.os.Parcel;
import android.os.Parcelable;
import java.nio.ByteBuffer;

public class MarshalQueryableParcelable<T extends Parcelable> implements MarshalQueryable<T> {
    private static final boolean DEBUG = false;
    private static final String FIELD_CREATOR = "CREATOR";
    private static final String TAG = "MarshalParcelable";

    private class MarshalerParcelable extends Marshaler<T> {
        private final Class<T> mClass;
        private final Parcelable.Creator<T> mCreator;

        protected MarshalerParcelable(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableParcelable.this, typeReference, nativeType);
            this.mClass = typeReference.getRawType();
            try {
                try {
                    this.mCreator = (Parcelable.Creator) this.mClass.getDeclaredField(MarshalQueryableParcelable.FIELD_CREATOR).get(null);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                } catch (IllegalArgumentException e2) {
                    throw new AssertionError(e2);
                }
            } catch (NoSuchFieldException e3) {
                throw new AssertionError(e3);
            }
        }

        /* JADX INFO: finally extract failed */
        public void marshal(T value, ByteBuffer buffer) {
            Parcel parcel = Parcel.obtain();
            try {
                value.writeToParcel(parcel, 0);
                if (!parcel.hasFileDescriptors()) {
                    byte[] parcelContents = parcel.marshall();
                    parcel.recycle();
                    if (parcelContents.length != 0) {
                        buffer.put(parcelContents);
                        return;
                    }
                    throw new AssertionError("No data marshaled for " + value);
                }
                throw new UnsupportedOperationException("Parcelable " + value + " must not have file descriptors");
            } catch (Throwable th) {
                parcel.recycle();
                throw th;
            }
        }

        public T unmarshal(ByteBuffer buffer) {
            buffer.mark();
            Parcel parcel = Parcel.obtain();
            try {
                int maxLength = buffer.remaining();
                byte[] remaining = new byte[maxLength];
                buffer.get(remaining);
                parcel.unmarshall(remaining, 0, maxLength);
                parcel.setDataPosition(0);
                T value = (Parcelable) this.mCreator.createFromParcel(parcel);
                int actualLength = parcel.dataPosition();
                if (actualLength != 0) {
                    buffer.reset();
                    buffer.position(buffer.position() + actualLength);
                    return (Parcelable) this.mClass.cast(value);
                }
                throw new AssertionError("No data marshaled for " + value);
            } finally {
                parcel.recycle();
            }
        }

        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }

        public int calculateMarshalSize(T value) {
            Parcel parcel = Parcel.obtain();
            try {
                value.writeToParcel(parcel, 0);
                return parcel.marshall().length;
            } finally {
                parcel.recycle();
            }
        }
    }

    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerParcelable(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        return Parcelable.class.isAssignableFrom(managedType.getRawType());
    }
}
