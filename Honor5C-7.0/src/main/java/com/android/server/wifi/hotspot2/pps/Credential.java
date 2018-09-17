package com.android.server.wifi.hotspot2.pps;

import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.IMSIParameter;
import com.android.server.wifi.anqp.eap.EAP.EAPMethodID;
import com.android.server.wifi.anqp.eap.EAPMethod;
import com.android.server.wifi.anqp.eap.NonEAPInnerAuth;
import com.android.server.wifi.anqp.eap.NonEAPInnerAuth.NonEAPType;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.OMAException;
import com.google.protobuf.nano.Extension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

public class Credential {
    public static final String CertTypeIEEE = "802.1ar";
    public static final String CertTypeX509 = "x509v3";
    private final CertType mCertType;
    private final boolean mCheckAAACert;
    private final long mCtime;
    private final boolean mDisregardPassword;
    private final EAPMethod mEAPMethod;
    private final long mExpTime;
    private final byte[] mFingerPrint;
    private final IMSIParameter mImsi;
    private final boolean mMachineManaged;
    private final String mPassword;
    private final String mRealm;
    private final String mSTokenApp;
    private final boolean mShare;
    private final String mUserName;

    public enum CertType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.pps.Credential.CertType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.pps.Credential.CertType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.Credential.CertType.<clinit>():void");
        }
    }

    public Credential(long ctime, long expTime, String realm, boolean checkAAACert, EAPMethod eapMethod, String userName, String password, boolean machineManaged, String stApp, boolean share) {
        this.mCtime = ctime;
        this.mExpTime = expTime;
        this.mRealm = realm;
        this.mCheckAAACert = checkAAACert;
        this.mEAPMethod = eapMethod;
        this.mUserName = userName;
        if (TextUtils.isEmpty(password)) {
            this.mPassword = null;
        } else {
            this.mPassword = new String(Base64.decode(password, 0), StandardCharsets.UTF_8);
        }
        this.mDisregardPassword = false;
        this.mMachineManaged = machineManaged;
        this.mSTokenApp = stApp;
        this.mShare = share;
        this.mCertType = null;
        this.mFingerPrint = null;
        this.mImsi = null;
    }

    public Credential(long ctime, long expTime, String realm, boolean checkAAACert, EAPMethod eapMethod, CertType certType, byte[] fingerPrint) {
        this.mCtime = ctime;
        this.mExpTime = expTime;
        this.mRealm = realm;
        this.mCheckAAACert = checkAAACert;
        this.mEAPMethod = eapMethod;
        this.mCertType = certType;
        this.mFingerPrint = fingerPrint;
        this.mUserName = null;
        this.mPassword = null;
        this.mDisregardPassword = false;
        this.mMachineManaged = false;
        this.mSTokenApp = null;
        this.mShare = false;
        this.mImsi = null;
    }

    public Credential(long ctime, long expTime, String realm, boolean checkAAACert, EAPMethod eapMethod, IMSIParameter imsi) {
        this.mCtime = ctime;
        this.mExpTime = expTime;
        this.mRealm = realm;
        this.mCheckAAACert = checkAAACert;
        this.mEAPMethod = eapMethod;
        this.mImsi = imsi;
        this.mCertType = null;
        this.mFingerPrint = null;
        this.mUserName = null;
        this.mPassword = null;
        this.mDisregardPassword = false;
        this.mMachineManaged = false;
        this.mSTokenApp = null;
        this.mShare = false;
    }

    public Credential(Credential other, String password) {
        this.mCtime = other.mCtime;
        this.mExpTime = other.mExpTime;
        this.mRealm = other.mRealm;
        this.mCheckAAACert = other.mCheckAAACert;
        this.mUserName = other.mUserName;
        this.mPassword = password;
        this.mDisregardPassword = other.mDisregardPassword;
        this.mMachineManaged = other.mMachineManaged;
        this.mSTokenApp = other.mSTokenApp;
        this.mShare = other.mShare;
        this.mEAPMethod = other.mEAPMethod;
        this.mCertType = other.mCertType;
        this.mFingerPrint = other.mFingerPrint;
        this.mImsi = other.mImsi;
    }

    public Credential(WifiEnterpriseConfig enterpriseConfig, KeyStore keyStore, boolean update) throws IOException {
        CertType certType;
        byte[] digest;
        boolean z;
        this.mCtime = -1;
        this.mExpTime = -1;
        this.mRealm = enterpriseConfig.getRealm();
        this.mCheckAAACert = false;
        this.mEAPMethod = mapEapMethod(enterpriseConfig.getEapMethod(), enterpriseConfig.getPhase2Method());
        if (this.mEAPMethod.getEAPMethodID() == EAPMethodID.EAP_TLS) {
            certType = CertType.x509v3;
        } else {
            certType = null;
        }
        this.mCertType = certType;
        if (enterpriseConfig.getClientCertificate() != null) {
            try {
                digest = MessageDigest.getInstance("SHA-256").digest(enterpriseConfig.getClientCertificate().getEncoded());
            } catch (GeneralSecurityException gse) {
                Log.e(Utils.hs2LogTag(getClass()), "Failed to generate certificate fingerprint: " + gse);
                digest = null;
            }
        } else if (enterpriseConfig.getClientCertificateAlias() != null) {
            byte[] octets = keyStore.get("USRCERT_" + enterpriseConfig.getClientCertificateAlias());
            if (octets != null) {
                try {
                    digest = MessageDigest.getInstance("SHA-256").digest(octets);
                } catch (GeneralSecurityException gse2) {
                    Log.e(Utils.hs2LogTag(getClass()), "Failed to construct digest: " + gse2);
                    digest = null;
                }
            } else {
                try {
                    digest = Base64.decode(enterpriseConfig.getClientCertificateAlias(), 0);
                } catch (IllegalArgumentException e) {
                    Log.e(Utils.hs2LogTag(getClass()), "Bad base 64 alias");
                    digest = null;
                }
            }
        } else {
            digest = null;
        }
        this.mFingerPrint = digest;
        String imsi = enterpriseConfig.getPlmn();
        IMSIParameter iMSIParameter = (imsi == null || imsi.length() == 0) ? null : new IMSIParameter(imsi);
        this.mImsi = iMSIParameter;
        this.mUserName = enterpriseConfig.getIdentity();
        this.mPassword = enterpriseConfig.getPassword();
        if (!update || this.mPassword.length() >= 2) {
            z = false;
        } else {
            z = true;
        }
        this.mDisregardPassword = z;
        this.mMachineManaged = false;
        this.mSTokenApp = null;
        this.mShare = false;
    }

    public static CertType mapCertType(String certType) throws OMAException {
        if (certType.equalsIgnoreCase(CertTypeX509)) {
            return CertType.x509v3;
        }
        if (certType.equalsIgnoreCase(CertTypeIEEE)) {
            return CertType.IEEE;
        }
        throw new OMAException("Invalid cert type: '" + certType + "'");
    }

    private static EAPMethod mapEapMethod(int eapMethod, int phase2Method) throws IOException {
        switch (eapMethod) {
            case Extension.TYPE_DOUBLE /*1*/:
                return new EAPMethod(EAPMethodID.EAP_TLS, null);
            case Extension.TYPE_FLOAT /*2*/:
                NonEAPInnerAuth inner;
                switch (phase2Method) {
                    case Extension.TYPE_DOUBLE /*1*/:
                        inner = new NonEAPInnerAuth(NonEAPType.PAP);
                        break;
                    case Extension.TYPE_FLOAT /*2*/:
                        inner = new NonEAPInnerAuth(NonEAPType.MSCHAP);
                        break;
                    case Extension.TYPE_INT64 /*3*/:
                        inner = new NonEAPInnerAuth(NonEAPType.MSCHAPv2);
                        break;
                    default:
                        throw new IOException("TTLS phase2 method " + phase2Method + " not valid for Passpoint");
                }
                return new EAPMethod(EAPMethodID.EAP_TTLS, inner);
            case Extension.TYPE_UINT64 /*4*/:
                return new EAPMethod(EAPMethodID.EAP_SIM, null);
            case Extension.TYPE_INT32 /*5*/:
                return new EAPMethod(EAPMethodID.EAP_AKA, null);
            case Extension.TYPE_FIXED64 /*6*/:
                return new EAPMethod(EAPMethodID.EAP_AKAPrim, null);
            default:
                String methodName;
                if (eapMethod < 0 || eapMethod >= Eap.strings.length) {
                    methodName = Integer.toString(eapMethod);
                } else {
                    methodName = Eap.strings[eapMethod];
                }
                throw new IOException("EAP method id " + methodName + " is not valid for Passpoint");
        }
    }

    public EAPMethod getEAPMethod() {
        return this.mEAPMethod;
    }

    public String getRealm() {
        return this.mRealm;
    }

    public IMSIParameter getImsi() {
        return this.mImsi;
    }

    public String getUserName() {
        return this.mUserName;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public boolean hasDisregardPassword() {
        return this.mDisregardPassword;
    }

    public CertType getCertType() {
        return this.mCertType;
    }

    public byte[] getFingerPrint() {
        return this.mFingerPrint;
    }

    public long getCtime() {
        return this.mCtime;
    }

    public long getExpTime() {
        return this.mExpTime;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Credential that = (Credential) o;
        if (this.mCheckAAACert == that.mCheckAAACert && this.mCtime == that.mCtime && this.mExpTime == that.mExpTime && this.mMachineManaged == that.mMachineManaged && this.mShare == that.mShare && this.mCertType == that.mCertType && this.mEAPMethod.equals(that.mEAPMethod) && Arrays.equals(this.mFingerPrint, that.mFingerPrint) && safeEquals(this.mImsi, that.mImsi)) {
            return (this.mDisregardPassword || safeEquals(this.mPassword, that.mPassword)) && this.mRealm.equals(that.mRealm) && safeEquals(this.mSTokenApp, that.mSTokenApp) && safeEquals(this.mUserName, that.mUserName);
        } else {
            return false;
        }
    }

    private static boolean safeEquals(Object s1, Object s2) {
        boolean z = false;
        if (s1 == null) {
            if (s2 == null) {
                z = true;
            }
            return z;
        }
        if (s2 != null) {
            z = s1.equals(s2);
        }
        return z;
    }

    public int hashCode() {
        int hashCode;
        int i = 1;
        int i2 = 0;
        int hashCode2 = ((((((((int) (this.mCtime ^ (this.mCtime >>> 32))) * 31) + ((int) (this.mExpTime ^ (this.mExpTime >>> 32)))) * 31) + this.mRealm.hashCode()) * 31) + (this.mCheckAAACert ? 1 : 0)) * 31;
        if (this.mUserName != null) {
            hashCode = this.mUserName.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mPassword != null) {
            hashCode = this.mPassword.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mMachineManaged) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mSTokenApp != null) {
            hashCode = this.mSTokenApp.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode2 + hashCode) * 31;
        if (!this.mShare) {
            i = 0;
        }
        i = (((hashCode + i) * 31) + this.mEAPMethod.hashCode()) * 31;
        if (this.mCertType != null) {
            hashCode = this.mCertType.hashCode();
        } else {
            hashCode = 0;
        }
        i = (i + hashCode) * 31;
        if (this.mFingerPrint != null) {
            hashCode = Arrays.hashCode(this.mFingerPrint);
        } else {
            hashCode = 0;
        }
        hashCode = (i + hashCode) * 31;
        if (this.mImsi != null) {
            i2 = this.mImsi.hashCode();
        }
        return hashCode + i2;
    }

    public String toString() {
        return "Credential{mCtime=" + Utils.toUTCString(this.mCtime) + ", mExpTime=" + Utils.toUTCString(this.mExpTime) + ", mRealm='" + this.mRealm + '\'' + ", mCheckAAACert=" + this.mCheckAAACert + ", mUserName='" + this.mUserName + '\'' + ", mPassword='" + this.mPassword + '\'' + ", mDisregardPassword=" + this.mDisregardPassword + ", mMachineManaged=" + this.mMachineManaged + ", mSTokenApp='" + this.mSTokenApp + '\'' + ", mShare=" + this.mShare + ", mEAPMethod=" + this.mEAPMethod + ", mCertType=" + this.mCertType + ", mFingerPrint=" + Utils.toHexString(this.mFingerPrint) + ", mImsi='" + this.mImsi + '\'' + '}';
    }
}
