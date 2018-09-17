package tmsdk.common.utils;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import tmsdk.common.NumMarker.MarkFileInfo;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.fg.module.qscanner.AmScannerStatic;
import tmsdkobf.ew;
import tmsdkobf.mo;
import tmsdkobf.ms;
import tmsdkobf.z;

/* compiled from: Unknown */
public class n {
    public static String a(Context context, long j) {
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String fileNameIdByFlag = UpdateConfig.getFileNameIdByFlag(j);
        if (fileNameIdByFlag == null) {
            return null;
        }
        String str = absolutePath + File.separator + fileNameIdByFlag;
        if (!new File(str).exists()) {
            ms.a(context, fileNameIdByFlag, absolutePath);
        }
        return str;
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

    public static z do(String str) {
        try {
            Object b = b(str, 0, 24);
            z zVar = new z();
            Object obj = new byte[4];
            System.arraycopy(b, 4, obj, 0, 4);
            zVar.timestamp = mo.k(obj);
            obj = new byte[16];
            System.arraycopy(b, 8, obj, 0, 16);
            zVar.an = obj;
            zVar.am = dp(new File(str).getName());
            return zVar;
        } catch (IOException e) {
            d.d("UpdateUtil", e.getMessage());
            return null;
        }
    }

    private static int dp(String str) {
        try {
            return Integer.parseInt(str.substring(0, str.lastIndexOf(".")));
        } catch (Exception e) {
            d.c("UpdateUtil", "fileName: " + str + " e: " + e.getMessage());
            return 0;
        }
    }

    public static z g(Context context, String str) {
        File file = new File(str);
        if (!file.exists()) {
            return null;
        }
        ew loadAmfHeader = AmScannerStatic.loadAmfHeader(context, str);
        if (loadAmfHeader == null) {
            return null;
        }
        z zVar = new z();
        zVar.am = UpdateConfig.getFileIdByFileName(file.getName());
        zVar.timestamp = loadAmfHeader.timestamp;
        zVar.version = loadAmfHeader.version;
        return zVar;
    }

    public static ew h(Context context, String str) {
        return !new File(str).exists() ? null : AmScannerStatic.loadAmfHeader(context, str);
    }

    public static z h(int i, String str) {
        try {
            Object b = b(str, 0, 48);
            z zVar = new z();
            zVar.version = b[0];
            Object obj = new byte[4];
            System.arraycopy(b, 4, obj, 0, 4);
            zVar.timestamp = mo.k(obj);
            obj = new byte[16];
            System.arraycopy(b, 8, obj, 0, 16);
            zVar.an = obj;
            obj = new byte[4];
            System.arraycopy(b, 44, obj, 0, 4);
            zVar.ao = mo.k(obj);
            zVar.am = i;
            return zVar;
        } catch (IOException e) {
            d.d("UpdateUtil", e.getMessage());
            return null;
        }
    }

    public static z i(int i, String str) {
        MarkFileInfo markFileInfo = ((NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class)).getMarkFileInfo();
        if (markFileInfo == null) {
            return null;
        }
        z zVar = new z();
        zVar.version = markFileInfo.version;
        zVar.timestamp = markFileInfo.timeStampSecondWhole;
        zVar.ao = markFileInfo.timeStampSecondLastDiff == 0 ? markFileInfo.timeStampSecondWhole : markFileInfo.timeStampSecondLastDiff;
        zVar.an = mo.cx(markFileInfo.md5 == null ? "" : markFileInfo.md5);
        zVar.am = i;
        return zVar;
    }
}
