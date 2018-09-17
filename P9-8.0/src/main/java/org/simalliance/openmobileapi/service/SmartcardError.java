package org.simalliance.openmobileapi.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmartcardError implements Parcelable {
    public static final Creator<SmartcardError> CREATOR = new Creator<SmartcardError>() {
        public SmartcardError createFromParcel(Parcel in) {
            return new SmartcardError(in, null);
        }

        public SmartcardError[] newArray(int size) {
            return new SmartcardError[size];
        }
    };
    private String mClazz;
    private String mMessage;

    public SmartcardError() {
        this.mClazz = "";
        this.mMessage = "";
    }

    private SmartcardError(Parcel in) {
        this.mClazz = in.readString();
        this.mMessage = in.readString();
    }

    public SmartcardError(String clazz, String message) {
        if (clazz == null) {
            clazz = "";
        }
        this.mClazz = clazz;
        if (message == null) {
            message = "";
        }
        this.mMessage = message;
    }

    public void clear() {
        this.mClazz = "";
        this.mMessage = "";
    }

    public Exception createException() {
        try {
            if (this.mClazz.length() == 0) {
                return null;
            }
            if (this.mMessage.length() == 0) {
                return (Exception) Class.forName(this.mClazz).newInstance();
            }
            if ("java.util.MissingResourceException".equals(this.mClazz)) {
                return (Exception) Class.forName(this.mClazz).getConstructor(new Class[]{String.class, String.class, String.class}).newInstance(new Object[]{this.mMessage, "", ""});
            }
            return (Exception) Class.forName(this.mClazz).getConstructor(new Class[]{String.class}).newInstance(new Object[]{this.mMessage});
        } catch (Exception e) {
            return null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.mClazz = in.readString();
        this.mMessage = in.readString();
    }

    public void setError(Class clazz, String message) {
        this.mClazz = clazz == null ? "" : clazz.getName();
        if (message == null) {
            message = "";
        }
        this.mMessage = message;
    }

    public void throwException() throws CardException {
        Exception e = createException();
        if (e != null) {
            if (e instanceof CardException) {
                throw ((CardException) e);
            } else if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mClazz);
        out.writeString(this.mMessage);
    }

    public String toString() {
        return "SmartcardError [mClazz=" + this.mClazz + ", mMessage=" + this.mMessage + "]";
    }
}
