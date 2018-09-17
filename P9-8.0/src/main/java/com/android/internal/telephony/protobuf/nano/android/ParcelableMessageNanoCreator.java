package com.android.internal.telephony.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.android.internal.telephony.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

public final class ParcelableMessageNanoCreator<T extends MessageNano> implements Creator<T> {
    private static final String TAG = "PMNCreator";
    private final Class<T> mClazz;

    public ParcelableMessageNanoCreator(Class<T> clazz) {
        this.mClazz = clazz;
    }

    public T createFromParcel(Parcel in) {
        String className = in.readString();
        T t = null;
        try {
            t = (MessageNano) Class.forName(className, false, getClass().getClassLoader()).asSubclass(MessageNano.class).getConstructor(new Class[0]).newInstance(new Object[0]);
            MessageNano.mergeFrom(t, in.createByteArray());
            return t;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Exception trying to create proto from parcel", e);
            return t;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Exception trying to create proto from parcel", e2);
            return t;
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "Exception trying to create proto from parcel", e3);
            return t;
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "Exception trying to create proto from parcel", e4);
            return t;
        } catch (InstantiationException e5) {
            Log.e(TAG, "Exception trying to create proto from parcel", e5);
            return t;
        } catch (InvalidProtocolBufferNanoException e6) {
            Log.e(TAG, "Exception trying to create proto from parcel", e6);
            return t;
        }
    }

    public T[] newArray(int i) {
        return (MessageNano[]) Array.newInstance(this.mClazz, i);
    }

    static <T extends MessageNano> void writeToParcel(Class<T> clazz, MessageNano message, Parcel out) {
        out.writeString(clazz.getName());
        out.writeByteArray(MessageNano.toByteArray(message));
    }
}
