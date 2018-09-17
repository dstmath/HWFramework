package tmsdkobf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class gd {
    public static void b(String str, String str2) throws Exception {
        ZipFile zipFile = new ZipFile(str);
        Enumeration entries = zipFile.entries();
        byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            if (zipEntry.isDirectory()) {
                new File(str2, zipEntry.getName()).mkdirs();
            } else {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                File file = new File(str2, zipEntry.getName());
                File parentFile = file.getParentFile();
                if (!(parentFile == null || parentFile.exists())) {
                    parentFile.mkdirs();
                }
                gc.c("ZipUtils", "file " + file.getAbsolutePath());
                OutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, IncomingSmsFilterConsts.PAY_SMS);
                while (true) {
                    int read = bufferedInputStream.read(bArr, 0, IncomingSmsFilterConsts.PAY_SMS);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                fileOutputStream.close();
                bufferedInputStream.close();
            }
        }
        zipFile.close();
    }
}
