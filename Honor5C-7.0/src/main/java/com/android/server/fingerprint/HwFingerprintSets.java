package com.android.server.fingerprint;

import android.hardware.fingerprint.Fingerprint;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Slog;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class HwFingerprintSets implements Parcelable {
    public static final Creator<HwFingerprintSets> CREATOR = null;
    private static final String TAG = "HwFingerprintSets";
    public ArrayList<HwFingerprintGroup> mFingerprintGroups;

    public static class HwFingerprintGroup {
        public static final int DESCRIPTION_LEN = 256;
        public ArrayList<Fingerprint> mFingerprints;
        public int mGroupId;

        public HwFingerprintGroup() {
            this.mFingerprints = new ArrayList();
        }

        private HwFingerprintGroup(Parcel in) {
            this.mFingerprints = new ArrayList();
            this.mGroupId = in.readInt();
            Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, mGroupId=" + this.mGroupId);
            int fpCount = in.readInt();
            Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, fpCount=" + fpCount);
            for (int i = 0; i < fpCount; i++) {
                int fingerid = in.readInt();
                Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, fingerid=" + fingerid);
                byte[] description = new byte[DESCRIPTION_LEN];
                in.readByteArray(description);
                int length = DESCRIPTION_LEN;
                for (int pos = 0; pos < DESCRIPTION_LEN; pos++) {
                    if (description[pos] == null) {
                        length = pos;
                        break;
                    }
                }
                String desc = new String(description, 0, length, StandardCharsets.UTF_8);
                Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, description=" + desc);
                this.mFingerprints.add(new Fingerprint(desc, this.mGroupId, fingerid, 0));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.fingerprint.HwFingerprintSets.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.fingerprint.HwFingerprintSets.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.HwFingerprintSets.<clinit>():void");
    }

    public HwFingerprintSets() {
        this.mFingerprintGroups = new ArrayList();
    }

    private HwFingerprintSets(Parcel in) {
        this.mFingerprintGroups = new ArrayList();
        int groupCount = in.readInt();
        Slog.i(TAG, "HwFingerprintSets, groupCount=" + groupCount);
        for (int i = 0; i < groupCount; i++) {
            this.mFingerprintGroups.add(new HwFingerprintGroup(null));
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
    }
}
