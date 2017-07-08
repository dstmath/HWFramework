package android.hardware.display;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class WifiDisplaySessionInfo implements Parcelable {
    public static final Creator<WifiDisplaySessionInfo> CREATOR = null;
    private final boolean mClient;
    private final String mGroupId;
    private final String mIP;
    private final String mPassphrase;
    private final int mSessionId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.display.WifiDisplaySessionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.display.WifiDisplaySessionInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.display.WifiDisplaySessionInfo.<clinit>():void");
    }

    public WifiDisplaySessionInfo() {
        this(true, 0, ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public WifiDisplaySessionInfo(boolean client, int session, String group, String pp, String ip) {
        this.mClient = client;
        this.mSessionId = session;
        this.mGroupId = group;
        this.mPassphrase = pp;
        this.mIP = ip;
    }

    public boolean isClient() {
        return this.mClient;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public String getGroupId() {
        return this.mGroupId;
    }

    public String getPassphrase() {
        return this.mPassphrase;
    }

    public String getIP() {
        return this.mIP;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mClient ? 1 : 0);
        dest.writeInt(this.mSessionId);
        dest.writeString(this.mGroupId);
        dest.writeString(this.mPassphrase);
        dest.writeString(this.mIP);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "WifiDisplaySessionInfo:\n    Client/Owner: " + (this.mClient ? "Client" : "Owner") + "\n    GroupId: " + this.mGroupId + "\n    Passphrase: " + this.mPassphrase + "\n    SessionId: " + this.mSessionId + "\n    IP Address: " + this.mIP;
    }
}
