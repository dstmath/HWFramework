package tmsdkobf;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class kn {
    private static final String[] tJ = new String[]{"MI 2"};

    private static boolean a(ArrayList<String> arrayList, String str) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str2 = (String) it.next();
            if (str.equals(str2)) {
                return true;
            }
            boolean z = false;
            try {
                String canonicalPath = new File(str2).getCanonicalPath();
                String canonicalPath2 = new File(str).getCanonicalPath();
                if (!(canonicalPath == null || canonicalPath2 == null)) {
                    z = canonicalPath.equals(canonicalPath2);
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (z) {
                return z;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x00eb A:{SYNTHETIC, Splitter: B:66:0x00eb} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00e2 A:{SYNTHETIC, Splitter: B:62:0x00e2} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<String> cM() {
        Exception e;
        Throwable th;
        ArrayList<String> arrayList = new ArrayList();
        if (kl.cK() == 0) {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (absolutePath != null) {
                arrayList.add(absolutePath);
            }
        }
        BufferedReader bufferedReader = null;
        try {
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader("/proc/mounts"));
            while (true) {
                try {
                    String readLine = bufferedReader2.readLine();
                    if (readLine == null) {
                        break;
                    }
                    if (!readLine.contains("vfat")) {
                        if (!(readLine.contains("exfat") || readLine.contains("/mnt") || readLine.contains("fuse"))) {
                        }
                    }
                    String[] split = readLine.split("\\s+");
                    if (split[1].equals(Environment.getExternalStorageDirectory().getPath())) {
                        if (!a(arrayList, split[1])) {
                            arrayList.add(split[1]);
                        }
                    } else if (!(!readLine.contains("/dev/block/vold") || readLine.contains("/mnt/secure") || readLine.contains("/mnt/asec") || readLine.contains("/mnt/obb") || readLine.contains("/dev/mapper") || readLine.contains("tmpfs") || a(arrayList, split[1]))) {
                        arrayList.add(split[1]);
                    }
                } catch (Exception e2) {
                    e = e2;
                    bufferedReader = bufferedReader2;
                    try {
                        e.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return arrayList;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = bufferedReader2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            n(arrayList);
            ArrayList<String> arrayList2 = arrayList;
            if (bufferedReader2 != null) {
                try {
                    bufferedReader2.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            return arrayList;
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (bufferedReader != null) {
            }
            return arrayList;
        }
    }

    private static void n(ArrayList<String> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            for (int i = 0; i < arrayList.size(); i++) {
                while (((String) arrayList.get(i)).endsWith("/")) {
                    arrayList.set(i, ((String) arrayList.get(i)).substring(0, ((String) arrayList.get(i)).length() - 1));
                }
            }
        }
    }
}
