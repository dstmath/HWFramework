package com.android.server.pm;

import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.huawei.aod.AodThemeConst;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import sun.misc.BASE64Decoder;
import vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface;

public class MspesExUtil {
    private static final String FILE_PACKAGE_CONFIG = "benchmarklist";
    private static final String FILE_PACKAGE_PUBKEY = ".pubkey";
    private static final int FLAG_FORBID_MSPES_UNINSTALL = 32;
    private static final int FLAG_FORBID_RUNNING_SOFTWARE_INSTALL = 1;
    private static final boolean IS_FASTBOOT_UNLOCK = SystemProperties.getBoolean("ro.fastboot.unlock", false);
    private static final boolean IS_HIDE_PRODUCT = SystemProperties.getBoolean("ro.build.hide", false);
    private static final int MAX_DECRYPT_BLOCK = 256;
    private static final List<String> MSPES_CALLER_PACKAGES = Arrays.asList("com.huawei.security", "com.huawei.security2");
    private static final String MSPES_FILE_PATH = "/log/.ss/";
    private static final int MSPES_OEM_MAIN_ID = 204;
    private static final int MSPES_OEM_MAXSIZE = 64;
    private static final int MSPES_OEM_SUB_ID = 7;
    private static final String RSA_ALGORITHM = "RSA";
    private static final String TAG = "MspesExUtil";
    private static final String TRANSFORMATION = "RSA/None/PKCS1Padding";
    private static final String UNICODE_UTF_8 = "UTF-8";
    private static volatile MspesExUtil sInstance;
    private IHwPackageManagerServiceExInner mHwPmsExInner;
    private List<String> mMspesForbidInstallPackages = new ArrayList();

    private MspesExUtil(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static synchronized MspesExUtil getInstance(IHwPackageManagerServiceExInner pmsEx) {
        MspesExUtil mspesExUtil;
        synchronized (MspesExUtil.class) {
            if (sInstance == null) {
                sInstance = new MspesExUtil(pmsEx);
            }
            mspesExUtil = sInstance;
        }
        return mspesExUtil;
    }

    public String getMspesOEMConfig() {
        if (!checkMspesStrategyCaller()) {
            return "";
        }
        try {
            IHwFactoryInterface hwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal");
            if (hwFactoryInterface == null) {
                Slog.w(TAG, "IHwFactoryInterface init failed!");
                return "";
            }
            final Map<String, Object> result = new HashMap<>();
            result.put("ret", -1);
            hwFactoryInterface.oeminfo_Read_reused(204, 7, 64, new IHwFactoryInterface.oeminfo_Read_reusedCallback() {
                /* class com.android.server.pm.MspesExUtil.AnonymousClass1 */

                @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.oeminfo_Read_reusedCallback
                public void onValues(int ret, String out) {
                    result.put("ret", Integer.valueOf(ret));
                    result.put("out", out);
                }
            });
            if (((Integer) result.get("ret")).intValue() == 0 && result.get("out") != null) {
                return (String) result.get("out");
            }
            return "";
        } catch (RemoteException e) {
            Slog.w(TAG, "failed to oeminfo_Read_reused");
        }
    }

    public int updateMspesOEMConfig(String src) {
        if (!checkMspesStrategyCaller() || src == null) {
            return -1;
        }
        try {
            IHwFactoryInterface hwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal");
            if (hwFactoryInterface == null) {
                Slog.w(TAG, "IHwFactoryInterface init failed!");
                return -1;
            } else if (src.length() == 0) {
                return hwFactoryInterface.oeminfo_erase_reused(204, 7);
            } else {
                return hwFactoryInterface.oeminfo_write_reused(204, 7, src.length(), src);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "failed to oeminfo_Read_reused");
            return -1;
        }
    }

    public String readMspesFile(String fileName) {
        if (checkMspesStrategyCaller()) {
            return readFile(fileName);
        }
        return "";
    }

    public boolean writeMspesFile(String fileName, String content) {
        return checkMspesStrategyCaller() && writeFile(fileName, content);
    }

    public void initMspesForbidInstallApps() {
        if (isForbidRunningSoftwareInstallEnable()) {
            String forbidInstallApps = getForbidInstallApps();
            if (!TextUtils.isEmpty(forbidInstallApps)) {
                this.mMspesForbidInstallPackages = new ArrayList(Arrays.asList(forbidInstallApps.trim().split(",")));
            }
        }
    }

    private String getForbidInstallApps() {
        String publicKey64 = readFile(FILE_PACKAGE_PUBKEY);
        if (TextUtils.isEmpty(publicKey64)) {
            Slog.w(TAG, "publicKey64 is null");
            return "";
        }
        String packageConfig = readFile(FILE_PACKAGE_CONFIG);
        if (TextUtils.isEmpty(packageConfig)) {
            Slog.w(TAG, "packageConfig is null");
            return "";
        }
        try {
            return new String(decrypt(loadPublicKey(publicKey64), hexStringToByte(packageConfig)), UNICODE_UTF_8);
        } catch (Exception e) {
            Slog.w(TAG, "getForbidInstallApps, decrypt forbiddenInstallApps failed, catch Exception");
            return null;
        }
    }

    private boolean checkMspesStrategyCaller() {
        int callingUid = Binder.getCallingUid();
        boolean isEqual = true;
        if (callingUid == 1000) {
            return true;
        }
        if (this.mHwPmsExInner.getIPmsInner() == null) {
            return false;
        }
        String pkgName = this.mHwPmsExInner.getIPmsInner().getNameForUidInner(callingUid);
        if (!MSPES_CALLER_PACKAGES.contains(pkgName) || !this.mHwPmsExInner.isPlatformSignatureApp(pkgName)) {
            isEqual = false;
        }
        if (!isEqual) {
            Slog.w(TAG, "checkMspesStrategyCaller ,check caller failed!");
        }
        return isEqual;
    }

    private long getMspesConfigValue() {
        String mspesConfig = SystemProperties.get("ro.mspes.config", (String) null);
        if (!TextUtils.isEmpty(mspesConfig)) {
            try {
                return Long.decode(mspesConfig.trim()).longValue();
            } catch (NumberFormatException e) {
                Slog.w(TAG, "ro.mspes.config is not a number");
                return 0;
            }
        } else if (IS_HIDE_PRODUCT || IS_FASTBOOT_UNLOCK) {
            return 0 | 1 | 32;
        } else {
            return 0;
        }
    }

    private boolean isForbidRunningSoftwareInstallEnable() {
        if ((1 & getMspesConfigValue()) != 0) {
            return true;
        }
        return false;
    }

    private boolean isForbidMspesUninstallEable() {
        if ((32 & getMspesConfigValue()) != 0) {
            return true;
        }
        return false;
    }

    public boolean isForbidMspesUninstall(String pkg) {
        boolean result = false;
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        if (isForbidMspesUninstallEable() && MSPES_CALLER_PACKAGES.contains(pkg)) {
            result = true;
        }
        if (result) {
            Slog.w(TAG, "forbidden uninstall mspes, pkg = " + pkg);
        }
        return result;
    }

    public boolean isInMspesForbidInstallPackageList(String pkg) {
        boolean result = false;
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        if (isForbidRunningSoftwareInstallEnable() && this.mMspesForbidInstallPackages.contains(pkg)) {
            result = true;
        }
        if (result) {
            Slog.w(TAG, "forbidden install apk, pkg = " + pkg);
        }
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0060, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0067, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006b, code lost:
        throw r7;
     */
    private String readFile(String fileName) {
        if (fileName == null) {
            return "";
        }
        try {
            InputStream inputStream = new FileInputStream(fileName.split(AodThemeConst.SPLASH).length > 1 ? fileName : MSPES_FILE_PATH + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UNICODE_UTF_8));
            StringBuffer buffer = new StringBuffer();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            String stringBuffer = buffer.toString();
            $closeResource(null, reader);
            $closeResource(null, inputStream);
            return stringBuffer;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException, emui readfile fail, fileName = " + fileName);
            return "";
        } catch (IOException e2) {
            Log.e(TAG, "IOException, emui readfile fail, fileName = " + fileName);
            return "";
        } catch (Exception e3) {
            Log.e(TAG, "Exception, emui readfile fail, fileName = " + fileName);
            return "";
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008b, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008c, code lost:
        $closeResource(r5, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008f, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0092, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0093, code lost:
        $closeResource(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0096, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0099, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009a, code lost:
        $closeResource(r5, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x009d, code lost:
        throw r6;
     */
    private boolean writeFile(String fileName, String content) {
        File file = new File(MSPES_FILE_PATH, fileName);
        if (!file.exists() || file.delete()) {
            File dir = new File(MSPES_FILE_PATH);
            if (dir.exists() || dir.mkdirs()) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, fileName), true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, UNICODE_UTF_8);
                    BufferedWriter bufWriter = new BufferedWriter(outputStreamWriter);
                    bufWriter.write(content);
                    bufWriter.newLine();
                    bufWriter.flush();
                    fileOutputStream.flush();
                    outputStreamWriter.flush();
                    $closeResource(null, bufWriter);
                    $closeResource(null, outputStreamWriter);
                    $closeResource(null, fileOutputStream);
                    return true;
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "writeFile(), write file fail. FileNotFoundException! fileName = " + fileName);
                    return false;
                } catch (UnsupportedEncodingException e2) {
                    Log.e(TAG, "writeFile(), write file fail. UnsupportedEncodingException! fileName = " + fileName);
                    return false;
                } catch (IOException e3) {
                    Log.e(TAG, "writeFile(), write file fail. IOException! fileName = " + fileName);
                    return false;
                } catch (Exception e4) {
                    Log.e(TAG, "writeFile(), write file fail! Exception! fileName = " + fileName);
                    return false;
                }
            } else {
                Log.w(TAG, "writeFile() !isMkdirsSuccess return false, fileName = " + fileName);
                return false;
            }
        } else {
            Log.w(TAG, "writeFile(), file.delete failed, fileName = " + fileName);
            return false;
        }
    }

    private RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception {
        try {
            return (RSAPublicKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(base64Decode(publicKeyStr)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e2) {
            throw new RuntimeException(e2);
        }
    }

    private byte[] base64Decode(String data) throws IOException {
        return new BASE64Decoder().decodeBuffer(data);
    }

    private byte[] decrypt(RSAPublicKey publicKey, byte[] encrypted) throws Exception {
        byte[] cacheBytes;
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(2, publicKey);
        int inputLen = encrypted.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > 256) {
                cacheBytes = cipher.doFinal(encrypted, offSet, 256);
            } else {
                cacheBytes = cipher.doFinal(encrypted, offSet, inputLen - offSet);
            }
            out.write(cacheBytes, 0, cacheBytes.length);
            i++;
            offSet = i * 256;
        }
        byte[] decryptedBytes = out.toByteArray();
        out.close();
        return decryptedBytes;
    }

    private byte[] hexStringToByte(String hex) {
        byte[] bytes = new byte[(hex.length() / 2)];
        int j = 0;
        int i = 0;
        int lenth = bytes.length;
        while (i < lenth) {
            int j2 = j + 1;
            int j3 = j2 + 1;
            bytes[i] = (byte) ((parse(hex.charAt(j)) << 4) | parse(hex.charAt(j2)));
            i++;
            j = j3;
        }
        return bytes;
    }

    private int parse(char c) {
        if (c >= 'a') {
            return ((c - 'a') + 10) & 15;
        }
        if (c >= 'A') {
            return ((c - 'A') + 10) & 15;
        }
        return (c - '0') & 15;
    }
}
