package android.content.pm;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public final class EphemeralResolveInfo implements Parcelable {
    public static final Creator<EphemeralResolveInfo> CREATOR = null;
    public static final String SHA_ALGORITHM = "SHA-256";
    private final byte[] mDigestBytes;
    private final int mDigestPrefix;
    private final List<IntentFilter> mFilters;
    private final String mPackageName;

    public static final class EphemeralResolveIntentInfo extends IntentFilter {
        private final EphemeralResolveInfo mResolveInfo;

        public EphemeralResolveIntentInfo(IntentFilter orig, EphemeralResolveInfo resolveInfo) {
            super(orig);
            this.mResolveInfo = resolveInfo;
        }

        public EphemeralResolveInfo getEphemeralResolveInfo() {
            return this.mResolveInfo;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.EphemeralResolveInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.EphemeralResolveInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.EphemeralResolveInfo.<clinit>():void");
    }

    public EphemeralResolveInfo(Uri uri, String packageName, List<IntentFilter> filters) {
        this.mFilters = new ArrayList();
        if (uri == null || packageName == null || filters == null || filters.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.mDigestBytes = generateDigest(uri);
        this.mDigestPrefix = (((this.mDigestBytes[0] << 24) | (this.mDigestBytes[1] << 16)) | (this.mDigestBytes[2] << 8)) | (this.mDigestBytes[3] << 0);
        this.mFilters.addAll(filters);
        this.mPackageName = packageName;
    }

    EphemeralResolveInfo(Parcel in) {
        this.mFilters = new ArrayList();
        this.mDigestBytes = in.createByteArray();
        this.mDigestPrefix = in.readInt();
        this.mPackageName = in.readString();
        in.readList(this.mFilters, null);
    }

    public byte[] getDigestBytes() {
        return this.mDigestBytes;
    }

    public int getDigestPrefix() {
        return this.mDigestPrefix;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public List<IntentFilter> getFilters() {
        return this.mFilters;
    }

    private static byte[] generateDigest(Uri uri) {
        try {
            return MessageDigest.getInstance(SHA_ALGORITHM).digest(uri.getHost().getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("could not find digest algorithm");
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.mDigestBytes);
        out.writeInt(this.mDigestPrefix);
        out.writeString(this.mPackageName);
        out.writeList(this.mFilters);
    }
}
