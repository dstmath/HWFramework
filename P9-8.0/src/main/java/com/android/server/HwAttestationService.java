package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.util.Log;
import com.huawei.attestation.IHwAttestationService.Stub;
import com.huawei.information.HwDeviceInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class HwAttestationService extends Stub {
    private static final String EMMC_CONFIG_FILE = "/sys/block/mmcblk0/device/cid";
    private static final String HW_DEVICE_ATTESTATION_ACCESS = "com.huawei.deviceattestation.HW_DEVICE_ATTESTATION_ACCESS";
    private static int MAX_SING_DTAT_LEN = 512;
    private static int OPEN_REASE_MATCH_NUM = 0;
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

    public int getLastError() {
        return this.lastError;
    }

    public byte[] getDeviceID(int deviceIdType) {
        if (this.mContext.checkCallingPermission(PERMISSION_DEVICE_READ_PHONE_STATE) == -1) {
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
        if (this.mContext.checkCallingPermission(PERMISSION_DEVICE_READ_PHONE_STATE) == -1) {
            Log.e(TAG, "permission denied");
            this.lastError = -5;
            return null;
        } else if (deviceIdType == 1) {
            return getAttestationSignatureByEMMCId(keyIndex, signatureType, challenge);
        } else {
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
            if (signatureType.getBytes(Charset.defaultCharset()).length > MAX_SING_DTAT_LEN || challenge.length > MAX_SING_DTAT_LEN) {
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

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0089 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029 A:{SYNTHETIC, Splitter: B:12:0x0029} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0089 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029 A:{SYNTHETIC, Splitter: B:12:0x0029} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0089 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007a A:{SYNTHETIC, Splitter: B:37:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007a A:{SYNTHETIC, Splitter: B:37:0x007a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readEMMCID() throws IOException {
        IOException e;
        Throwable th;
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            File file = new File(EMMC_CONFIG_FILE);
            if (!file.exists() || file.isDirectory()) {
                throw new FileNotFoundException();
            }
            BufferedReader br2 = new BufferedReader(new FileReader(file));
            try {
                StringBuffer sb2 = new StringBuffer();
                try {
                    for (String temp = br2.readLine(); temp != null; temp = br2.readLine()) {
                        sb2.append(temp);
                    }
                    if (br2 != null) {
                        try {
                            br2.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "IOException in HwAttestationService.readEMMCID()", e2);
                        }
                    }
                    sb = sb2;
                } catch (IOException e3) {
                    e2 = e3;
                    sb = sb2;
                    br = br2;
                    try {
                        Log.e(TAG, "IOException in HwAttestationService.readEMMCID()", e2);
                        if (br != null) {
                        }
                        if (sb == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (br != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    if (br != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e2 = e4;
                br = br2;
                Log.e(TAG, "IOException in HwAttestationService.readEMMCID()", e2);
                if (br != null) {
                }
                if (sb == null) {
                }
            } catch (Throwable th4) {
                th = th4;
                br = br2;
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e22) {
                        Log.e(TAG, "IOException in HwAttestationService.readEMMCID()", e22);
                    }
                }
                throw th;
            }
            if (sb == null) {
                return sb.toString().getBytes(Charset.defaultCharset());
            }
            return null;
        } catch (IOException e5) {
            e22 = e5;
            Log.e(TAG, "IOException in HwAttestationService.readEMMCID()", e22);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e222) {
                    Log.e(TAG, "IOException in HwAttestationService.readEMMCID()", e222);
                }
            }
            if (sb == null) {
            }
        }
    }

    private byte[] readEMMCIDByNative() {
        String emmcid = HwDeviceInfo.getEMMCID();
        if (emmcid != null) {
            try {
                return emmcid.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
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
