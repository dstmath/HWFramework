package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NearbyRecvBean implements Parcelable {
    public static final Parcelable.Creator<NearbyRecvBean> CREATOR = new Parcelable.Creator<NearbyRecvBean>() {
        /* class com.huawei.nearbysdk.NearbyRecvBean.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearbyRecvBean createFromParcel(Parcel source) {
            if (source == null) {
                HwLog.e(NearbyRecvBean.TAG, "Input param source is null.");
                return null;
            }
            try {
                Object boolObj = Parcel.class.getMethod("readBoolean", new Class[0]).invoke(source, new Object[0]);
                if (!(boolObj instanceof Boolean)) {
                    HwLog.d(NearbyRecvBean.TAG, "readBoolean type failed.");
                    return null;
                } else if (!((Boolean) boolObj).booleanValue()) {
                    return createFromParcelInner(source);
                } else {
                    Method methodBlob = Parcel.class.getMethod("readBlob", new Class[0]);
                    Parcel data = Parcel.obtain();
                    Object bytesObj = methodBlob.invoke(source, new Object[0]);
                    if (bytesObj instanceof byte[]) {
                        Object bytes = (byte[]) bytesObj;
                        HwLog.d(NearbyRecvBean.TAG, "ReadBlob size :" + bytes.length);
                        data.unmarshall(bytes, 0, bytes.length);
                        data.setDataPosition(0);
                        return createFromParcelInner(data);
                    }
                    HwLog.e(NearbyRecvBean.TAG, "ReadBlob type failed.");
                    return null;
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                HwLog.e(NearbyRecvBean.TAG, "Create from parcel failed.");
                return null;
            }
        }

        private NearbyRecvBean createFromParcelInner(Parcel source) {
            int sendType = source.readInt();
            Object[] pathObjectArray = source.readArray(String.class.getClassLoader());
            Object[] dirObjectArray = source.readArray(String.class.getClassLoader());
            if (pathObjectArray == null) {
                HwLog.e(NearbyRecvBean.TAG, "Create from parcel inner readArray failed.");
                return null;
            }
            String[] pathArray = new String[pathObjectArray.length];
            for (int i = 0; i < pathObjectArray.length; i++) {
                if (pathObjectArray[i] instanceof String) {
                    pathArray[i] = (String) pathObjectArray[i];
                }
            }
            String[] dirArray = null;
            if (!(dirObjectArray == null || dirObjectArray.length == 0)) {
                dirArray = new String[dirObjectArray.length];
                for (int i2 = 0; i2 < dirObjectArray.length; i2++) {
                    if (dirObjectArray[i2] instanceof String) {
                        dirArray[i2] = (String) dirObjectArray[i2];
                    }
                }
            }
            HwLog.d(NearbyRecvBean.TAG, "Create from parcel inner pathArray length:" + pathArray.length);
            return new NearbyRecvBean(sendType, pathArray, dirArray);
        }

        @Override // android.os.Parcelable.Creator
        public NearbyRecvBean[] newArray(int size) {
            return new NearbyRecvBean[size];
        }
    };
    private static final int DEFAULT_SIZE = 16;
    private static final String TAG = "NearbyRecvBean";
    private String[] mFilePathArray;
    private String[] mNewDirArray;
    private int mSendType;

    public NearbyRecvBean(int sendType, String[] pathArray) {
        this.mSendType = sendType;
        this.mFilePathArray = pathArray;
        this.mNewDirArray = null;
    }

    public NearbyRecvBean(int sendType, String[] pathArray, String[] dirArray) {
        this.mSendType = sendType;
        this.mFilePathArray = pathArray;
        this.mNewDirArray = dirArray;
    }

    public int getSendType() {
        return this.mSendType;
    }

    public void setSendType(int sendType) {
        this.mSendType = sendType;
    }

    public String[] getFilePathList() {
        return this.mFilePathArray;
    }

    public void setFilePathList(String[] filePathList) {
        this.mFilePathArray = filePathList;
    }

    public String[] getNewDirArray() {
        return this.mNewDirArray;
    }

    public void setNewDirArray(String[] newDirArray) {
        this.mNewDirArray = newDirArray;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            HwLog.e(TAG, "Input param dest is null.");
        } else if (this.mFilePathArray == null || this.mFilePathArray.length <= 500) {
            try {
                Parcel.class.getMethod("writeBoolean", Boolean.TYPE).invoke(dest, false);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                HwLog.e(TAG, "Invoke writeBoolean failed.");
            }
            writeToParcelInner(dest);
        } else {
            HwLog.d(TAG, "Write to parcel file path array size: " + this.mFilePathArray.length);
            Parcel data = Parcel.obtain();
            writeToParcelInner(data);
            byte[] bytes = data.marshall();
            data.recycle();
            try {
                Method methodBool = Parcel.class.getMethod("writeBoolean", Boolean.TYPE);
                Method methodBlob = Parcel.class.getMethod("writeBlob", byte[].class);
                if (bytes == null) {
                    methodBool.invoke(dest, false);
                    writeToParcelInner(dest);
                    return;
                }
                HwLog.d(TAG, "Write parcel writeBlob size :" + bytes.length);
                methodBool.invoke(dest, true);
                methodBlob.invoke(dest, bytes);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
                HwLog.e(TAG, "Write parcel failed.");
            }
        }
    }

    private void writeToParcelInner(Parcel dest) {
        dest.writeInt(this.mSendType);
        dest.writeArray(this.mFilePathArray);
        dest.writeArray(this.mNewDirArray);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder((int) DEFAULT_SIZE);
        stringBuilder.append(TAG);
        stringBuilder.append("FileSize: ");
        stringBuilder.append(this.mFilePathArray.length);
        return stringBuilder.toString();
    }
}
