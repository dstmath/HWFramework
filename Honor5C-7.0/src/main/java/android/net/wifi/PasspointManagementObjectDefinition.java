package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PasspointManagementObjectDefinition implements Parcelable {
    public static final Creator<PasspointManagementObjectDefinition> CREATOR = null;
    private final String mBaseUri;
    private final String mMoTree;
    private final String mUrn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.PasspointManagementObjectDefinition.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.PasspointManagementObjectDefinition.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.PasspointManagementObjectDefinition.<clinit>():void");
    }

    public PasspointManagementObjectDefinition(String baseUri, String urn, String moTree) {
        this.mBaseUri = baseUri;
        this.mUrn = urn;
        this.mMoTree = moTree;
    }

    public String getBaseUri() {
        return this.mBaseUri;
    }

    public String getUrn() {
        return this.mUrn;
    }

    public String getMoTree() {
        return this.mMoTree;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBaseUri);
        dest.writeString(this.mUrn);
        dest.writeString(this.mMoTree);
    }
}
