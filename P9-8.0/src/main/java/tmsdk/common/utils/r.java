package tmsdk.common.utils;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import tmsdk.common.NumMarker.MarkFileInfo;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.module.qscanner.impl.AmScannerV2;
import tmsdk.common.module.update.UpdateConfig;
import tmsdkobf.ad;
import tmsdkobf.fd;
import tmsdkobf.lq;
import tmsdkobf.lu;

public class r {
    public static String a(Context context, long j) {
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String fileNameIdByFlag = UpdateConfig.getFileNameIdByFlag(j);
        if (fileNameIdByFlag == null) {
            return null;
        }
        String str = absolutePath + File.separator + fileNameIdByFlag;
        if (!new File(str).exists()) {
            lu.b(context, fileNameIdByFlag, absolutePath);
        }
        return str;
    }

    public static String b(Context context, int i, String str) {
        if (str == null || context == null) {
            return null;
        }
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String str2 = Integer.toString(i) + str;
        String str3 = absolutePath + File.separator + str2;
        if (!new File(str3).exists()) {
            lu.b(context, str2, absolutePath);
        }
        return str3;
    }

    public static ad b(Context context, long j) {
        if (j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE) {
            return i(context, a(context, j));
        }
        if (j == 2) {
            return m(50001, b(context, 50001, ".sdb"));
        }
        if (j != UpdateConfig.UPDATE_FLAG_YELLOW_PAGEV2_Large) {
            return cM(a(context, j));
        }
        ad n = n(40461, b(context, 40461, ".sdb"));
        if (n == null) {
            return n;
        }
        f.f("gjj", "fileId:" + n.aE + " timestamp:" + n.timestamp + " pfutimestamp:" + n.aG + " version:" + n.version);
        return n;
    }

    private static byte[] b(String str, int i, int i2) throws IOException {
        byte[] bArr = new byte[i2];
        RandomAccessFile randomAccessFile = new RandomAccessFile(str, "r");
        try {
            randomAccessFile.skipBytes(i);
            randomAccessFile.read(bArr);
            return bArr;
        } finally {
            randomAccessFile.close();
        }
    }

    public static ad cM(String str) {
        try {
            Object b = b(str, 0, 24);
            ad adVar = new ad();
            Object obj = new byte[4];
            System.arraycopy(b, 4, obj, 0, 4);
            adVar.timestamp = lq.k(obj);
            Object obj2 = new byte[16];
            System.arraycopy(b, 8, obj2, 0, 16);
            adVar.aF = obj2;
            adVar.aE = cN(new File(str).getName());
            return adVar;
        } catch (IOException e) {
            f.f("UpdateManager", e.getMessage());
            return null;
        }
    }

    private static int cN(String str) {
        try {
            return Integer.parseInt(str.substring(0, str.lastIndexOf(".")));
        } catch (Exception e) {
            f.e("UpdateManager", "fileName: " + str + " e: " + e.getMessage());
            return 0;
        }
    }

    public static ad i(Context context, String str) {
        File file = new File(str);
        if (!file.exists()) {
            return null;
        }
        fd g = AmScannerV2.g(context, str);
        if (g == null) {
            return null;
        }
        ad adVar = new ad();
        adVar.aE = UpdateConfig.getFileIdByFileName(file.getName());
        adVar.timestamp = g.timestamp;
        adVar.version = g.version;
        return adVar;
    }

    public static fd j(Context context, String str) {
        return !new File(str).exists() ? null : AmScannerV2.g(context, str);
    }

    public static String k(Context context, String str) {
        if (str == null || context == null) {
            return null;
        }
        String str2 = context.getFilesDir().getAbsolutePath() + File.separator + str;
        return new File(str2).exists() ? str2 : null;
    }

    public static ad m(int i, String str) {
        try {
            Object b = b(str, 0, 48);
            ad adVar = new ad();
            adVar.version = b[0];
            Object obj = new byte[4];
            System.arraycopy(b, 4, obj, 0, 4);
            adVar.timestamp = lq.k(obj);
            Object obj2 = new byte[16];
            System.arraycopy(b, 8, obj2, 0, 16);
            adVar.aF = obj2;
            Object obj3 = new byte[4];
            System.arraycopy(b, 44, obj3, 0, 4);
            adVar.aG = lq.k(obj3);
            adVar.aE = i;
            return adVar;
        } catch (IOException e) {
            f.f("UpdateManager", e.getMessage());
            return null;
        }
    }

    public static ad n(int i, String str) {
        ad adVar = new ad();
        try {
            Object b = b(str, 0, 24);
            adVar.version = b[0];
            Object obj = new byte[4];
            System.arraycopy(b, 4, obj, 0, 4);
            adVar.timestamp = lq.k(obj);
            Object obj2 = new byte[16];
            System.arraycopy(b, 8, obj2, 0, 16);
            adVar.aF = obj2;
            adVar.aG = adVar.timestamp;
            adVar.aE = i;
            return adVar;
        } catch (IOException e) {
            f.f("UpdateManager", e.getMessage());
            adVar.aE = i;
            adVar.aF = new byte[0];
            adVar.timestamp = 0;
            if (adVar.aF == null) {
                adVar.aF = lq.at("");
            }
            return adVar;
        }
    }

    public static ad o(int i, String str) {
        MarkFileInfo markFileInfo = ((NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class)).getMarkFileInfo(i, str);
        if (markFileInfo == null) {
            return null;
        }
        ad adVar = new ad();
        adVar.version = markFileInfo.version;
        adVar.timestamp = markFileInfo.timeStampSecondWhole;
        adVar.aG = markFileInfo.timeStampSecondLastDiff == 0 ? markFileInfo.timeStampSecondWhole : markFileInfo.timeStampSecondLastDiff;
        adVar.aF = lq.bJ(markFileInfo.md5 == null ? "" : markFileInfo.md5);
        adVar.aE = i;
        return adVar;
    }
}
