package com.huawei.server.security.attestationservice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.android.server.HwSignature;
import com.huawei.attestation.IHwAttestationService;
import com.huawei.information.HwDeviceInfo;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class HwAttestationService extends IHwAttestationService.Stub implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.attestationservice.HwAttestationService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(HwAttestationService.TAG, "create HwAttestationService");
            return new HwAttestationService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static int MAX_SING_DTAT_LEN = HwSecDiagnoseConstant.BIT_SETIDS;
    private static final String PERMISSION_DEVICE_ATTESTATION = "com.huawei.permission.MANAGE_DEVICE_ATTESTATION";
    private static final String PERMISSION_DEVICE_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static final String TAG = "HwAttestationService";
    private int lastError = 0;
    private Context mContext;
    private HwSignature mSignTool;

    public HwAttestationService(Context context) {
        this.mContext = context;
        this.mSignTool = HwSignature.getInstance();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.attestationservice.HwAttestationService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        Log.i(TAG, "HwAttestationService onStart");
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        Log.i(TAG, "HwAttestationService onStop");
    }

    public int getLastError() {
        return this.lastError;
    }

    public byte[] getDeviceID(int deviceIdType) {
        if (!checkCallingPermission(Binder.getCallingUid()) && this.mContext.checkCallingPermission(PERMISSION_DEVICE_READ_PHONE_STATE) == -1) {
            Log.e(TAG, "permission denied");
            this.lastError = -5;
            return null;
        } else if (deviceIdType != 1) {
            return null;
        } else {
            byte[] emmcid = readEMMCIDByNative();
            if (emmcid.length > MAX_SING_DTAT_LEN) {
                return null;
            }
            return emmcid;
        }
    }

    public int getPublickKey(int keyIndex, byte[] keyBuf) {
        this.lastError = 0;
        if (keyIndex != 1) {
            Log.e(TAG, "not supprot keyIndex:" + keyIndex);
            this.lastError = -4;
            return 0;
        } else if (keyBuf == null) {
            this.lastError = -4;
            return 0;
        } else {
            int actLen = this.mSignTool.getPublicKey(1, keyBuf);
            if (actLen <= 0) {
                this.lastError = -6;
            }
            return actLen;
        }
    }

    public byte[] getAttestationSignature(int keyIndex, int deviceIdType, String signatureType, byte[] challenge) {
        if (!checkCallingPermission(Binder.getCallingUid()) && this.mContext.checkCallingPermission(PERMISSION_DEVICE_READ_PHONE_STATE) == -1) {
            Log.e(TAG, "permission denied");
            this.lastError = -5;
            return null;
        } else if (deviceIdType == 1) {
            return getAttestationSignatureByEMMCId(keyIndex, signatureType, challenge);
        } else {
            return null;
        }
    }

    public byte[] getAttestationSignatureWithPkgName(int keyIndex, int deviceIdType, String signatureType, byte[] challenge, String packageName) {
        if (packageName == null) {
            this.lastError = -4;
            return null;
        } else if (!checkCallingPermission(packageName)) {
            Log.e(TAG, "permission denied");
            this.lastError = -5;
            return null;
        } else if (deviceIdType == 1) {
            return getAttestationSignatureByEMMCId(keyIndex, signatureType, challenge, packageName);
        } else {
            return null;
        }
    }

    public byte[] getAttestationSignatureByEMMCId(int keyIndex, String signatureType, byte[] challenge, String packageName) {
        boolean isCheckPkgName;
        if (keyIndex != 1) {
            this.lastError = -4;
            return null;
        } else if (signatureType == null || challenge == null || packageName == null) {
            this.lastError = -4;
            return null;
        } else {
            int length = signatureType.getBytes(Charset.defaultCharset()).length;
            int i = MAX_SING_DTAT_LEN;
            if (length > i || challenge.length > i) {
                this.lastError = -4;
                return null;
            }
            String[] pkgList = this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
            if (pkgList != null) {
                if (pkgList.length != 0) {
                    int index = 0;
                    while (true) {
                        if (index >= pkgList.length) {
                            isCheckPkgName = false;
                            break;
                        } else if (packageName.equals(pkgList[index])) {
                            isCheckPkgName = true;
                            break;
                        } else {
                            index++;
                        }
                    }
                    if (isCheckPkgName) {
                        if (packageName.getBytes(Charset.defaultCharset()).length <= MAX_SING_DTAT_LEN) {
                            byte[] emmcid = readEMMCIDByNative();
                            if (emmcid.length > MAX_SING_DTAT_LEN) {
                                this.lastError = -3;
                                return null;
                            } else if (this.mSignTool.checkKeyStatus(1) != 0) {
                                this.lastError = -2;
                                return null;
                            } else {
                                this.lastError = 0;
                                return this.mSignTool.signMessage(1, packageName.getBytes(Charset.defaultCharset()), packageName.getBytes(Charset.defaultCharset()).length, emmcid, emmcid.length, signatureType.getBytes(Charset.defaultCharset()), signatureType.getBytes(Charset.defaultCharset()).length, challenge, challenge.length);
                            }
                        }
                    }
                    this.lastError = -4;
                    return null;
                }
            }
            this.lastError = -4;
            return null;
        }
    }

    public byte[] getAttestationSignatureByEMMCId(int keyIndex, String signatureType, byte[] challenge) {
        if (keyIndex != 1) {
            this.lastError = -4;
            return null;
        } else if (signatureType == null || challenge == null) {
            this.lastError = -4;
            return null;
        } else {
            int length = signatureType.getBytes(Charset.defaultCharset()).length;
            int i = MAX_SING_DTAT_LEN;
            if (length > i || challenge.length > i) {
                this.lastError = -4;
                return null;
            }
            String pkgName = getPackageName(Binder.getCallingUid());
            if (pkgName == null) {
                this.lastError = -4;
                return null;
            } else if (pkgName.getBytes(Charset.defaultCharset()).length > MAX_SING_DTAT_LEN) {
                this.lastError = -4;
                return null;
            } else {
                byte[] emmcid = readEMMCIDByNative();
                if (emmcid.length > MAX_SING_DTAT_LEN) {
                    this.lastError = -3;
                    return null;
                } else if (this.mSignTool.checkKeyStatus(1) != 0) {
                    this.lastError = -2;
                    return null;
                } else {
                    this.lastError = 0;
                    return this.mSignTool.signMessage(1, pkgName.getBytes(Charset.defaultCharset()), pkgName.getBytes(Charset.defaultCharset()).length, emmcid, emmcid.length, signatureType.getBytes(Charset.defaultCharset()), signatureType.getBytes(Charset.defaultCharset()).length, challenge, challenge.length);
                }
            }
        }
    }

    private String getPackageName(int uid) {
        String[] pkg = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (pkg == null || pkg.length == 0) {
            return null;
        }
        return pkg[0];
    }

    private boolean checkCallingPermission(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            boolean isCheckPermission = false;
            if ((pm.getApplicationInfo(getPackageName(uid), 0).flags & 1) != 0) {
                isCheckPermission = true;
            }
            if (isCheckPermission || pm.checkSignatures(getPackageName(uid), "android") < 0) {
                return isCheckPermission;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException in HwAttestationService.checkCallingPermission()", e);
            return false;
        }
    }

    private boolean checkCallingPermission(String pkgName) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        boolean isCheckPermission = false;
        try {
            if ((pm.getApplicationInfo(pkgName, 0).flags & 1) != 0) {
                isCheckPermission = true;
            }
            if (isCheckPermission || pm.checkSignatures(pkgName, "android") < 0) {
                return isCheckPermission;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException in HwAttestationService.checkCallingPermission()", e);
            return false;
        }
    }

    private byte[] readEMMCIDByNative() {
        String emmcid = HwDeviceInfo.getEMMCID();
        if (emmcid != null) {
            try {
                return emmcid.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "readEMMCIDByNative UnsupportedEncodingException e : " + e.getMessage());
            }
        }
        return new byte[0];
    }

    public int getDeviceCertType(int keyIndex) {
        this.lastError = 0;
        if (this.mContext.checkCallingPermission(PERMISSION_DEVICE_ATTESTATION) == -1) {
            Log.e(TAG, "permission denied");
            this.lastError = -5;
            return -1;
        } else if (keyIndex != 1) {
            Log.e(TAG, "not supprot keyIndex:" + keyIndex);
            this.lastError = -4;
            return -1;
        } else {
            int certType = this.mSignTool.getDeviceCertType(keyIndex);
            if (certType < 0) {
                this.lastError = -7;
            }
            return certType;
        }
    }

    public int getDeviceCert(int keyIndex, int certType, byte[] certBuf) {
        this.lastError = 0;
        if (this.mContext.checkCallingPermission(PERMISSION_DEVICE_ATTESTATION) == -1) {
            Log.e(TAG, "permission denied");
            this.lastError = -5;
            return -1;
        } else if (keyIndex != 1) {
            Log.e(TAG, "not supprot keyIndex:" + keyIndex);
            this.lastError = -4;
            return -1;
        } else if (certBuf == null) {
            Log.e(TAG, "certBuf is null");
            this.lastError = -4;
            return -1;
        } else {
            int certLen = this.mSignTool.getDeviceCert(keyIndex, certType, certBuf);
            if (certLen <= 0) {
                Log.e(TAG, "getDeviceCert get cert fail: " + certLen);
                this.lastError = -8;
            }
            return certLen;
        }
    }
}
