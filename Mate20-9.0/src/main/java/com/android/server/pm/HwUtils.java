package com.android.server.pm;

import android.util.Slog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class HwUtils {
    public static final int BIT_LOW_EIGHT = 255;
    public static final int BIT_LOW_FOUR = 15;
    public static final int BYTE_SIZE = 1024;
    public static final int CONST_NUM_EIGHT = 8;
    public static final int CONST_NUM_EIGHTEEN = 18;
    public static final int CONST_NUM_FOUR = 4;
    public static final int CONST_NUM_NINE = 9;
    public static final int CONST_NUM_ONE = 1;
    public static final int CONST_NUM_SEVEN = 7;
    public static final int CONST_NUM_SEVENTEEN = 17;
    public static final int CONST_NUM_SIXTEEN = 16;
    public static final int CONST_NUM_TEN = 10;
    public static final int CONST_NUM_THIRTEEN = 13;
    public static final int CONST_NUM_THREE = 3;
    public static final int CONST_NUM_TWO = 2;
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    public static final int POST_DELAY_TIME_HALF_SECOND = 500;
    public static final int SET_PERMISSIONS_MODE = 416;
    public static final int SYSTEM_IMG_VERSION = 3;
    public static final String SYSTEM_SIGN_STR = "30820405308202eda00302010202090083309550b47e0583300d06092a864886f70d0101050500308198310b300906035504061302434e3112301006035504080c094775616e67646f6e673112301006035504070c095368656e677a68656e310f300d060355040a0c0648756177656931183016060355040b0c0f5465726d696e616c436f6d70616e793114301206035504030c0b416e64726f69645465616d3120301e06092a864886f70d01090116116d6f62696c65406875617765692e636f6d301e170d3136303530353037333531345a170d3433303932313037333531345a308198310b300906035504061302434e3112301006035504080c094775616e67646f6e673112301006035504070c095368656e677a68656e310f300d060355040a0c0648756177656931183016060355040b0c0f5465726d696e616c436f6d70616e793114301206035504030c0b416e64726f69645465616d3120301e06092a864886f70d01090116116d6f62696c65406875617765692e636f6d30820122300d06092a864886f70d01010105000382010f003082010a0282010100c9fe1b699203091cb3944030cb1ba7996567182c1ce8be5535d673bc2025f37958e5bb1f4ed870dc229ffc2ed7d16f6cf10c08bc63f53624abe49db543518ef0069686ea5b3f129188652e87eca4b794df591828dd94de14b91ddbf2af156426453b8e739b12625a44b0895bfa1db3cdcce7db52f4d5af7c9918c325475c8273a5e4fe002e0f68082e9ec61d100913618982928ab5767701a8f576113c0810a4850a606233fd654531562bf8a74ac81bf8bacd66ca8a5ca9751f08e9575b402221e48e474f7f2dc91d02cfd87ceeaeb39ccf754cff5f1e8dfe23587955481bf0b8a386993edadc0f725e124f1ecedbef8d3cfbd6ddc783cde4b193f79fae05ed0203010001a350304e301d0603551d0e041604148d42132bfdc2ed970e25f5677cedd26f32527bc8301f0603551d230418301680148d42132bfdc2ed970e25f5677cedd26f32527bc8300c0603551d13040530030101ff300d06092a864886f70d010105050003820101003bc6e2ba8703a211222da8ed350e12cf31ac4d91290c5524da44626c382c8186f8238860b7ebddebba996f204802d72246d1326332ca85aff4a10cdaaa0d886016e26075c9b98799bf4767663d8c1097dccbc609dd3946f6431a35a71ee9ff3731c5b2715c158fe8d64c700b7e3e387e63a62e80ecdd4d007af242abed4b694d5a70d12dbde433fd18e1a7d033142f44cbe9ca187134830b86ecfa78ae2ff6d201014e4cf1d1655f40f4e4f4dd04af3c0416709dd159845d25515ff12f2854180e2ccbc1b05dffce93f9487839c126fa39f1453468a41eb7872b84c736dcb0d90a29775cd863707044f28bce4d05edcce4699605b27ae11e981590f87384726d";
    public static final String TAG = "HwUtils";

    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & 255;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars);
    }

    public static byte[] getManifestFile(File apkFile) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        ZipFile zipFile = null;
        boolean catchFlag = false;
        InputStream zipInputStream = null;
        try {
            ZipFile zipFile2 = new ZipFile(apkFile);
            InputStream zipInputStream2 = zipFile2.getInputStream(zipFile2.getEntry("META-INF/MANIFEST.MF"));
            if (zipInputStream2 != null) {
                while (true) {
                    int read = zipInputStream2.read(b);
                    int length = read;
                    if (read <= 0) {
                        break;
                    }
                    os.write(b, 0, length);
                }
                catchFlag = true;
            }
            if (zipInputStream2 != null) {
                try {
                    zipInputStream2.close();
                } catch (IOException e) {
                    Slog.e(TAG, "getManifestFile, failed to close zipInputStream" + e.getMessage());
                }
            }
            try {
                zipFile2.close();
            } catch (IOException e2) {
                Slog.e(TAG, "getManifestFile, failed to close zipFile" + e2.getMessage());
            }
            return catchFlag ? os.toByteArray() : new byte[0];
        } catch (IOException e3) {
            Slog.e(TAG, " get manifest file failed due to IOException");
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "getManifestFile, failed to close zipInputStream" + e4.getMessage());
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "getManifestFile, failed to close zipFile" + e5.getMessage());
                }
            }
            return null;
        } catch (Throwable th) {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "getManifestFile, failed to close zipInputStream" + e6.getMessage());
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e7) {
                    Slog.e(TAG, "getManifestFile, failed to close zipFile" + e7.getMessage());
                }
            }
            throw th;
        }
    }
}
