package com.android.server.pm;

import android.content.pm.PackageParser.ActivityIntentInfo;
import android.util.ArraySet;
import android.util.Slog;
import java.util.ArrayList;

public class IntentFilterVerificationState {
    public static final int STATE_UNDEFINED = 0;
    public static final int STATE_VERIFICATION_FAILURE = 3;
    public static final int STATE_VERIFICATION_PENDING = 1;
    public static final int STATE_VERIFICATION_SUCCESS = 2;
    static final String TAG = null;
    private ArrayList<ActivityIntentInfo> mFilters;
    private ArraySet<String> mHosts;
    private String mPackageName;
    private int mRequiredVerifierUid;
    private int mState;
    private int mUserId;
    private boolean mVerificationComplete;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.IntentFilterVerificationState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.IntentFilterVerificationState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.IntentFilterVerificationState.<clinit>():void");
    }

    public IntentFilterVerificationState(int verifierUid, int userId, String packageName) {
        this.mRequiredVerifierUid = STATE_UNDEFINED;
        this.mFilters = new ArrayList();
        this.mHosts = new ArraySet();
        this.mRequiredVerifierUid = verifierUid;
        this.mUserId = userId;
        this.mPackageName = packageName;
        this.mState = STATE_UNDEFINED;
        this.mVerificationComplete = false;
    }

    public void setState(int state) {
        if (state > STATE_VERIFICATION_FAILURE || state < 0) {
            this.mState = STATE_UNDEFINED;
        } else {
            this.mState = state;
        }
    }

    public int getState() {
        return this.mState;
    }

    public void setPendingState() {
        setState(STATE_VERIFICATION_PENDING);
    }

    public ArrayList<ActivityIntentInfo> getFilters() {
        return this.mFilters;
    }

    public boolean isVerificationComplete() {
        return this.mVerificationComplete;
    }

    public boolean isVerified() {
        boolean z = false;
        if (!this.mVerificationComplete) {
            return false;
        }
        if (this.mState == STATE_VERIFICATION_SUCCESS) {
            z = true;
        }
        return z;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getHostsString() {
        StringBuilder sb = new StringBuilder();
        int count = this.mHosts.size();
        for (int i = STATE_UNDEFINED; i < count; i += STATE_VERIFICATION_PENDING) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append((String) this.mHosts.valueAt(i));
        }
        return sb.toString();
    }

    public boolean setVerifierResponse(int callerUid, int code) {
        if (this.mRequiredVerifierUid == callerUid) {
            int state = STATE_UNDEFINED;
            if (code == STATE_VERIFICATION_PENDING) {
                state = STATE_VERIFICATION_SUCCESS;
            } else if (code == -1) {
                state = STATE_VERIFICATION_FAILURE;
            }
            this.mVerificationComplete = true;
            setState(state);
            return true;
        }
        Slog.d(TAG, "Cannot set verifier response with callerUid:" + callerUid + " and code:" + code + " as required verifierUid is:" + this.mRequiredVerifierUid);
        return false;
    }

    public void addFilter(ActivityIntentInfo filter) {
        this.mFilters.add(filter);
        this.mHosts.addAll(filter.getHostsList());
    }
}
