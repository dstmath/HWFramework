package com.android.server.wifi.hotspot2.pps;

import android.util.Base64;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.OMAException;
import com.android.server.wifi.hotspot2.omadm.OMANode;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import java.nio.charset.StandardCharsets;

public class UpdateInfo {
    private final String mCertFP;
    private final String mCertURL;
    private final long mInterval;
    private final String mPassword;
    private final boolean mSPPClientInitiated;
    private final String mURI;
    private final UpdateRestriction mUpdateRestriction;
    private final String mUsername;

    public enum UpdateRestriction {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.pps.UpdateInfo.UpdateRestriction.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.pps.UpdateInfo.UpdateRestriction.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.UpdateInfo.UpdateRestriction.<clinit>():void");
        }
    }

    public UpdateInfo(OMANode policyUpdate) throws OMAException {
        this.mInterval = PasspointManagementObjectManager.getLong(policyUpdate, PasspointManagementObjectManager.TAG_UpdateInterval, null) * PasspointManagementObjectManager.IntervalFactor;
        this.mSPPClientInitiated = ((Boolean) PasspointManagementObjectManager.getSelection(policyUpdate, PasspointManagementObjectManager.TAG_UpdateMethod)).booleanValue();
        this.mUpdateRestriction = (UpdateRestriction) PasspointManagementObjectManager.getSelection(policyUpdate, PasspointManagementObjectManager.TAG_Restriction);
        this.mURI = PasspointManagementObjectManager.getString(policyUpdate, PasspointManagementObjectManager.TAG_URI);
        OMANode unp = policyUpdate.getChild(PasspointManagementObjectManager.TAG_UsernamePassword);
        if (unp != null) {
            this.mUsername = PasspointManagementObjectManager.getString(unp.getChild(PasspointManagementObjectManager.TAG_Username));
            this.mPassword = new String(Base64.decode(PasspointManagementObjectManager.getString(unp.getChild(PasspointManagementObjectManager.TAG_Password)).getBytes(StandardCharsets.US_ASCII), 0), StandardCharsets.UTF_8);
        } else {
            this.mUsername = null;
            this.mPassword = null;
        }
        OMANode trustRoot = PasspointManagementObjectManager.getChild(policyUpdate, PasspointManagementObjectManager.TAG_TrustRoot);
        this.mCertURL = PasspointManagementObjectManager.getString(trustRoot, PasspointManagementObjectManager.TAG_CertURL);
        this.mCertFP = PasspointManagementObjectManager.getString(trustRoot, PasspointManagementObjectManager.TAG_CertSHA256Fingerprint);
    }

    public long getInterval() {
        return this.mInterval;
    }

    public boolean isSPPClientInitiated() {
        return this.mSPPClientInitiated;
    }

    public UpdateRestriction getUpdateRestriction() {
        return this.mUpdateRestriction;
    }

    public String getURI() {
        return this.mURI;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public String getCertURL() {
        return this.mCertURL;
    }

    public String getCertFP() {
        return this.mCertFP;
    }

    public String toString() {
        return "UpdateInfo{interval=" + Utils.toHMS(this.mInterval) + ", SPPClientInitiated=" + this.mSPPClientInitiated + ", updateRestriction=" + this.mUpdateRestriction + ", URI='" + this.mURI + '\'' + ", username='" + this.mUsername + '\'' + ", password=" + this.mPassword + ", certURL='" + this.mCertURL + '\'' + ", certFP='" + this.mCertFP + '\'' + '}';
    }
}
