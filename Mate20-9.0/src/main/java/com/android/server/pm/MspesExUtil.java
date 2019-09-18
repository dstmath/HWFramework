package com.android.server.pm;

import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
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
    private static final boolean FASTBOOT_UNLOCK = SystemProperties.getBoolean("ro.fastboot.unlock", false);
    private static final String FILE_PACKAGE_CONFIG = "benchmarklist";
    private static final String FILE_PACKAGE_PUBKEY = ".pubkey";
    private static final int FLAG_FORBID_MSPES_UNINSTALL = 32;
    private static final int FLAG_FORBID_RUNNING_SOFTWARE_INSTALL = 1;
    private static final boolean HIDE_PRODUCT_INFO = SystemProperties.getBoolean("ro.build.hide", false);
    private static final int MAX_DECRYPT_BLOCK = 256;
    private static final List<String> MSPES_CALLER_PACKAGE = Arrays.asList(new String[]{"com.huawei.security", "com.huawei.security2"});
    private static final String MSPES_FILE_PATH = "/log/.ss/";
    private static final int MSPES_OEM_MAIN_ID = 204;
    private static final int MSPES_OEM_MAXSIZE = 64;
    private static final int MSPES_OEM_SUB_ID = 7;
    private static final String RSA_ALGORITHM = "RSA";
    private static final String TAG = "MspesExUtil";
    private static final String TRANSFORMATION = "RSA/None/PKCS1Padding";
    private static volatile MspesExUtil mInstance;
    private IHwPackageManagerServiceExInner mHwPmsExInner;
    private List<String> mMspesForbidInstallPackages = new ArrayList();

    private MspesExUtil(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static MspesExUtil getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (mInstance == null) {
            synchronized (HwDelAppManager.class) {
                if (mInstance == null) {
                    mInstance = new MspesExUtil(pmsEx);
                }
            }
        }
        return mInstance;
    }

    public String getMspesOEMConfig() {
        if (!checkMspesStrategyCaller()) {
            return null;
        }
        try {
            IHwFactoryInterface iHwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal");
            if (iHwFactoryInterface == null) {
                Slog.w(TAG, "IHwFactoryInterface init failed!");
                return null;
            }
            final Map<String, Object> result = new HashMap<>();
            result.put("ret", -1);
            iHwFactoryInterface.oeminfo_Read_reused(204, 7, 64, new IHwFactoryInterface.oeminfo_Read_reusedCallback() {
                public void onValues(int ret, String out) {
                    result.put("ret", Integer.valueOf(ret));
                    result.put("out", out);
                }
            });
            if (((Integer) result.get("ret")).intValue() != 0) {
                return null;
            }
            return (String) result.get("out");
        } catch (RemoteException e) {
            Slog.w(TAG, "failed to oeminfo_Read_reused");
            return null;
        }
    }

    public int updateMspesOEMConfig(String src) {
        if (!checkMspesStrategyCaller() || src == null) {
            return -1;
        }
        int ret = -1;
        try {
            IHwFactoryInterface iHwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal");
            if (iHwFactoryInterface == null) {
                Slog.w(TAG, "IHwFactoryInterface init failed!");
                return -1;
            }
            ret = iHwFactoryInterface.oeminfo_write_reused(204, 7, src.length(), src);
            return ret;
        } catch (RemoteException e) {
            Slog.w(TAG, "failed to oeminfo_Read_reused");
        }
    }

    public String readMspesFile(String fileName) {
        if (checkMspesStrategyCaller()) {
            return readFile(fileName);
        }
        return null;
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
        String forbiddenInstallApps = null;
        if (TextUtils.isEmpty(publicKey64)) {
            Slog.w(TAG, "publicKey64 is null");
            return null;
        }
        String packageConfig = readFile(FILE_PACKAGE_CONFIG);
        if (TextUtils.isEmpty(packageConfig)) {
            Slog.w(TAG, "packageConfig is null");
            return null;
        }
        try {
            forbiddenInstallApps = new String(decrypt(loadPublicKey(publicKey64), hexStringToByte(packageConfig)));
        } catch (Exception e) {
            Slog.w(TAG, "decrypt forbiddenInstallApps failed, e = " + e.getMessage());
        }
        return forbiddenInstallApps;
    }

    private boolean checkMspesStrategyCaller() {
        int callingUid = Binder.getCallingUid();
        boolean equals = true;
        if (callingUid == 1000) {
            return true;
        }
        String pkgName = this.mHwPmsExInner.getIPmsInner().getNameForUidInner(callingUid);
        if (!MSPES_CALLER_PACKAGE.contains(pkgName) || !this.mHwPmsExInner.isPlatformSignatureApp(pkgName)) {
            equals = false;
        }
        if (!equals) {
            Slog.w(TAG, "checkMspesStrategyCaller ,check caller failed!");
        }
        return equals;
    }

    private long getMspesConfigValue() {
        String mspesConfig = SystemProperties.get("ro.mspes.config", null);
        if (!TextUtils.isEmpty(mspesConfig)) {
            try {
                return Long.decode(mspesConfig.trim()).longValue();
            } catch (NumberFormatException e) {
                Slog.w(TAG, " ro.mspes.config  is not a number");
                return 0;
            }
        } else if (HIDE_PRODUCT_INFO || FASTBOOT_UNLOCK) {
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
        boolean z = false;
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        if (isForbidMspesUninstallEable() && MSPES_CALLER_PACKAGE.contains(pkg)) {
            z = true;
        }
        boolean result = z;
        if (result) {
            Slog.w(TAG, "forbidden uninstall mspes, pkg = " + pkg);
        }
        return result;
    }

    public boolean isInMspesForbidInstallPackageList(String pkg) {
        boolean z = false;
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        if (isForbidRunningSoftwareInstallEnable() && this.mMspesForbidInstallPackages.contains(pkg)) {
            z = true;
        }
        boolean result = z;
        if (result) {
            Slog.w(TAG, "forbidden install apk, pkg = " + pkg);
        }
        return result;
    }

    private String readFile(String fileName) {
        String filePath = MSPES_FILE_PATH + fileName;
        try {
            StringBuffer buffer = new StringBuffer();
            InputStream is = new FileInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            String stringBuffer = buffer.toString();
            closeStream(reader);
            closeStream(is);
            return stringBuffer;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "emui readfile fail, fileName = " + fileName + ", e = " + e.getMessage());
        } catch (IOException e2) {
            Log.e(TAG, "emui readfile fail, fileName = " + fileName + ", e = " + e2.getMessage());
        } catch (Exception e3) {
            Log.e(TAG, "emui readfile fail, fileName = " + fileName + ", e = " + e3.getMessage());
        } catch (Throwable th) {
            closeStream(null);
            closeStream(null);
            throw th;
        }
        closeStream(null);
        closeStream(null);
        return null;
    }

    private boolean writeFile(String fileName, String content) {
        File file = new File(MSPES_FILE_PATH, fileName);
        if (!file.exists() || file.delete()) {
            FileOutputStream fileOutputStream = null;
            OutputStreamWriter outputStreamWriter = null;
            BufferedWriter bufWriter = null;
            try {
                File dir = new File(MSPES_FILE_PATH);
                if (dir.exists() || dir.mkdirs()) {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(new File(dir, fileName), true);
                    outputStreamWriter = new OutputStreamWriter(fileOutputStream2, "UTF-8");
                    BufferedWriter bufWriter2 = new BufferedWriter(outputStreamWriter);
                    bufWriter2.write(content);
                    bufWriter2.newLine();
                    bufWriter2.flush();
                    fileOutputStream2.flush();
                    outputStreamWriter.flush();
                    bufWriter2.close();
                    bufWriter = null;
                    fileOutputStream2.close();
                    fileOutputStream = null;
                    outputStreamWriter.close();
                    closeStream(bufWriter);
                    closeStream(fileOutputStream);
                    closeStream(null);
                    return true;
                }
                Log.w(TAG, "writeFile() !isMkdirsSuccess return false, fileName = " + fileName);
                return false;
            } catch (FileNotFoundException e) {
                Log.e(TAG, "writeFile(), write file fail! fileName = " + fileName + ", e = " + e.getMessage());
                return false;
            } catch (UnsupportedEncodingException e2) {
                Log.e(TAG, "writeFile(), write file fail! fileName = " + fileName + ", e = " + e2.getMessage());
                return false;
            } catch (IOException e3) {
                Log.e(TAG, "writeFile(), write file fail! fileName = " + fileName + ", e = " + e3.getMessage());
                return false;
            } catch (Exception e4) {
                Log.e(TAG, "writeFile(), write file fail! fileName = " + fileName + ", e = " + e4.getMessage());
                return false;
            } finally {
                closeStream(bufWriter);
                closeStream(fileOutputStream);
                closeStream(outputStreamWriter);
            }
        } else {
            Log.w(TAG, "writeFile(), file.delete failed, fileName = " + fileName);
            return false;
        }
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeStream IOException");
            }
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
        byte[] cache;
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(2, publicKey);
        int inputLen = encrypted.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > 256) {
                cache = cipher.doFinal(encrypted, offSet, 256);
            } else {
                cache = cipher.doFinal(encrypted, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * 256;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    private byte[] hexStringToByte(String hex) {
        byte[] b = new byte[(hex.length() / 2)];
        int j = 0;
        int i = 0;
        int lenth = b.length;
        while (i < lenth) {
            int j2 = j + 1;
            int j3 = j2 + 1;
            b[i] = (byte) ((parse(hex.charAt(j)) << 4) | parse(hex.charAt(j2)));
            i++;
            j = j3;
        }
        return b;
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
