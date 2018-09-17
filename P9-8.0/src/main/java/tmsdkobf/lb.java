package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class lb {

    static class a {
        public String id;
        public long time;

        a() {
        }
    }

    private static long c(File file) {
        File[] listFiles = file.listFiles();
        long j = 0;
        File[] fileArr = listFiles;
        int length = listFiles.length;
        for (int i = 0; i < length; i++) {
            long lastModified = fileArr[i].lastModified();
            if ((lastModified <= j ? 1 : null) == null) {
                j = lastModified;
            }
        }
        return j;
    }

    public static void en() {
        try {
            ArrayList eo = eo();
            if (eo != null && eo.size() > 0) {
                JceStruct aoVar = new ao(141, new ArrayList());
                Iterator it = eo.iterator();
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    ap apVar = new ap(new HashMap());
                    apVar.bG.put(Integer.valueOf(1), aVar.id);
                    apVar.bG.put(Integer.valueOf(7), String.valueOf(aVar.time));
                    aoVar.bD.add(apVar);
                }
                ob bK = im.bK();
                if (aoVar.bD.size() > 0 && bK != null) {
                    bK.a(4060, aoVar, null, 0, new jy() {
                        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                            if (i3 == 0 && i4 == 0) {
                                kz.k(System.currentTimeMillis() / 1000);
                            }
                        }
                    });
                }
                return;
            }
            la.b(141, 1001, "");
        } catch (Throwable th) {
        }
    }

    private static ArrayList<a> eo() {
        ArrayList<a> arrayList = new ArrayList();
        try {
            File file = new File(lu.eG() + "/tencent/MicroMsg/");
            if (!file.exists()) {
                return arrayList;
            }
            File[] listFiles = file.listFiles();
            File[] fileArr = listFiles;
            int length = listFiles.length;
            for (int i = 0; i < length; i++) {
                File file2 = fileArr[i];
                if (file2.getName().length() == 32) {
                    a aVar = new a();
                    aVar.id = file2.getName();
                    aVar.time = c(file2) / 1000;
                    arrayList.add(aVar);
                }
            }
            return arrayList;
        } catch (Throwable th) {
        }
    }
}
