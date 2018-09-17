package org.simalliance.openmobileapi.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmartcardError implements Parcelable {
    public static final Creator<SmartcardError> CREATOR = null;
    private String mClazz;
    private String mMessage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.simalliance.openmobileapi.service.SmartcardError.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.simalliance.openmobileapi.service.SmartcardError.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.simalliance.openmobileapi.service.SmartcardError.<clinit>():void");
    }

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
