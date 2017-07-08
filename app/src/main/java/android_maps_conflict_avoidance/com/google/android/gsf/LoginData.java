package android_maps_conflict_avoidance.com.google.android.gsf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LoginData implements Parcelable {
    public static final Creator<LoginData> CREATOR = null;
    public String mAuthtoken;
    public String mCaptchaAnswer;
    public byte[] mCaptchaData;
    public String mCaptchaMimeType;
    public String mCaptchaToken;
    public String mEncryptedPassword;
    public int mFlags;
    public String mJsonString;
    public String mOAuthAccessToken;
    public String mPassword;
    public String mService;
    public String mSid;
    public Status mStatus;
    public String mUsername;

    public enum Status {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.Status.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.Status.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.Status.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.<clinit>():void");
    }

    /* synthetic */ LoginData(Parcel in, LoginData loginData) {
        this(in);
    }

    public LoginData() {
        this.mUsername = null;
        this.mEncryptedPassword = null;
        this.mPassword = null;
        this.mService = null;
        this.mCaptchaToken = null;
        this.mCaptchaData = null;
        this.mCaptchaMimeType = null;
        this.mCaptchaAnswer = null;
        this.mFlags = 0;
        this.mStatus = null;
        this.mJsonString = null;
        this.mSid = null;
        this.mAuthtoken = null;
        this.mOAuthAccessToken = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mUsername);
        out.writeString(this.mEncryptedPassword);
        out.writeString(this.mPassword);
        out.writeString(this.mService);
        out.writeString(this.mCaptchaToken);
        if (this.mCaptchaData == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(this.mCaptchaData.length);
            out.writeByteArray(this.mCaptchaData);
        }
        out.writeString(this.mCaptchaMimeType);
        out.writeString(this.mCaptchaAnswer);
        out.writeInt(this.mFlags);
        if (this.mStatus == null) {
            out.writeString(null);
        } else {
            out.writeString(this.mStatus.name());
        }
        out.writeString(this.mJsonString);
        out.writeString(this.mSid);
        out.writeString(this.mAuthtoken);
        out.writeString(this.mOAuthAccessToken);
    }

    private LoginData(Parcel in) {
        this.mUsername = null;
        this.mEncryptedPassword = null;
        this.mPassword = null;
        this.mService = null;
        this.mCaptchaToken = null;
        this.mCaptchaData = null;
        this.mCaptchaMimeType = null;
        this.mCaptchaAnswer = null;
        this.mFlags = 0;
        this.mStatus = null;
        this.mJsonString = null;
        this.mSid = null;
        this.mAuthtoken = null;
        this.mOAuthAccessToken = null;
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mUsername = in.readString();
        this.mEncryptedPassword = in.readString();
        this.mPassword = in.readString();
        this.mService = in.readString();
        this.mCaptchaToken = in.readString();
        int len = in.readInt();
        if (len == -1) {
            this.mCaptchaData = null;
        } else {
            this.mCaptchaData = new byte[len];
            in.readByteArray(this.mCaptchaData);
        }
        this.mCaptchaMimeType = in.readString();
        this.mCaptchaAnswer = in.readString();
        this.mFlags = in.readInt();
        String status = in.readString();
        if (status == null) {
            this.mStatus = null;
        } else {
            this.mStatus = Status.valueOf(status);
        }
        this.mJsonString = in.readString();
        this.mSid = in.readString();
        this.mAuthtoken = in.readString();
        this.mOAuthAccessToken = in.readString();
    }
}
