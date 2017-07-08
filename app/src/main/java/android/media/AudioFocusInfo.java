package android.media;

import android.media.AudioAttributes.Builder;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class AudioFocusInfo implements Parcelable {
    public static final Creator<AudioFocusInfo> CREATOR = null;
    private AudioAttributes mAttributes;
    private String mClientId;
    private int mFlags;
    private int mGainRequest;
    private int mLossReceived;
    private String mPackageName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioFocusInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioFocusInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioFocusInfo.<clinit>():void");
    }

    public AudioFocusInfo(AudioAttributes aa, String clientId, String packageName, int gainRequest, int lossReceived, int flags) {
        if (aa == null) {
            aa = new Builder().build();
        }
        this.mAttributes = aa;
        if (clientId == null) {
            clientId = ProxyInfo.LOCAL_EXCL_LIST;
        }
        this.mClientId = clientId;
        if (packageName == null) {
            packageName = ProxyInfo.LOCAL_EXCL_LIST;
        }
        this.mPackageName = packageName;
        this.mGainRequest = gainRequest;
        this.mLossReceived = lossReceived;
        this.mFlags = flags;
    }

    public AudioAttributes getAttributes() {
        return this.mAttributes;
    }

    public String getClientId() {
        return this.mClientId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getGainRequest() {
        return this.mGainRequest;
    }

    public int getLossReceived() {
        return this.mLossReceived;
    }

    public void clearLossReceived() {
        this.mLossReceived = 0;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mAttributes.writeToParcel(dest, flags);
        dest.writeString(this.mClientId);
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mGainRequest);
        dest.writeInt(this.mLossReceived);
        dest.writeInt(this.mFlags);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mAttributes, this.mClientId, this.mPackageName, Integer.valueOf(this.mGainRequest), Integer.valueOf(this.mFlags)});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AudioFocusInfo other = (AudioFocusInfo) obj;
        return this.mAttributes.equals(other.mAttributes) && this.mClientId.equals(other.mClientId) && this.mPackageName.equals(other.mPackageName) && this.mGainRequest == other.mGainRequest && this.mLossReceived == other.mLossReceived && this.mFlags == other.mFlags;
    }
}
