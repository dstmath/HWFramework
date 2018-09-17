package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class ku {
    private static volatile boolean xT = false;
    private static List<byte[]> xU = null;
    private static List<Long> xV = null;

    private static int a(RandomAccessFile randomAccessFile) throws Exception {
        if (randomAccessFile.readInt() == 712365948) {
            return randomAccessFile.readInt();
        }
        throw new Exception();
    }

    private static int a(RandomAccessFile randomAccessFile, int -l_5_I, int i) throws Exception {
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        do {
            int a = a(randomAccessFile);
            long readLong = randomAccessFile.readLong();
            if (a > 255) {
                i = 0;
                break;
            }
            Object obj = new byte[a];
            if (randomAccessFile.read(obj, 0, a) != a) {
                i = 0;
                break;
            }
            if (-l_5_I <= i) {
                arrayList.add(obj);
                arrayList2.add(Long.valueOf(readLong));
            }
            -l_5_I--;
        } while (-l_5_I > 0);
        randomAccessFile.setLength(0);
        if (i != 0) {
            a(randomAccessFile, 0);
            i = a(randomAccessFile, null, 0, arrayList, arrayList2);
            long filePointer = randomAccessFile.getFilePointer();
            randomAccessFile.seek(0);
            a(randomAccessFile, i);
            randomAccessFile.seek(filePointer);
            return i;
        }
        a(randomAccessFile, 0);
        return i;
    }

    private static int a(RandomAccessFile randomAccessFile, byte[] bArr, long j, List<byte[]> list, List<Long> list2) throws Exception {
        int i = 0;
        if (bArr != null && bArr.length > 0) {
            i = 1;
            a(randomAccessFile, bArr.length);
            randomAccessFile.writeLong(j);
            randomAccessFile.write(bArr);
        }
        if (list != null && list.size() > 0) {
            int size = list.size();
            for (int i2 = 0; i2 < size; i2++) {
                byte[] bArr2 = (byte[]) list.get(i2);
                if (bArr2 != null && bArr2.length > 0) {
                    i++;
                    byte[] bArr3 = (byte[]) list.get(i2);
                    a(randomAccessFile, bArr3.length);
                    randomAccessFile.writeLong(((Long) list2.get(i2)).longValue());
                    randomAccessFile.write(bArr3);
                }
            }
        }
        return i;
    }

    private static void a(RandomAccessFile randomAccessFile, int i) throws Exception {
        randomAccessFile.writeInt(712365948);
        randomAccessFile.writeInt(i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x0099 A:{SYNTHETIC, Splitter: B:40:0x0099} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x009f A:{PHI: r0 , ExcHandler: all (th java.lang.Throwable), Splitter: B:5:0x003a} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:19:0x006e, code:
            if (r0 != null) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:20:0x0070, code:
            if (r0 != null) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            r0.setLength(0);
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:43:0x009f, code:
            r10 = th;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:46:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(byte[] bArr, long j, List<byte[]> list, List<Long> list2) {
        Object obj = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + ".bufflocache001");
        int i = 0;
        RandomAccessFile randomAccessFile;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(file, "rws");
            try {
                if (randomAccessFile.length() <= 0) {
                    obj = 1;
                }
                if (obj == null) {
                    i = a(randomAccessFile);
                    if (i <= 20) {
                        randomAccessFile.seek(randomAccessFile.length());
                    } else {
                        i = a(randomAccessFile, i, 15);
                    }
                } else {
                    a(randomAccessFile, 0);
                }
                i += a(randomAccessFile, bArr, j, list, list2);
                randomAccessFile.seek(0);
                a(randomAccessFile, i);
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e2) {
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
            Throwable th3 = th2;
            randomAccessFile = null;
            if (randomAccessFile != null) {
            }
            throw th3;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a7 A:{PHI: r4 , ExcHandler: all (th java.lang.Throwable), Splitter: B:3:0x002f} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0099 A:{SYNTHETIC, Splitter: B:38:0x0099} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ab A:{SYNTHETIC, Splitter: B:48:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0099 A:{SYNTHETIC, Splitter: B:38:0x0099} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ab A:{SYNTHETIC, Splitter: B:48:0x00ab} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:45:0x00a7, code:
            r3 = th;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r4.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean b(ArrayList<String> arrayList, ArrayList<String> arrayList2) {
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + ".bufflocache001");
        if (!file.exists()) {
            return false;
        }
        boolean z = false;
        try {
            RandomAccessFile randomAccessFile2 = new RandomAccessFile(file, "rws");
            try {
                if ((randomAccessFile2.length() <= 0 ? 1 : null) == null) {
                    try {
                        int a = a(randomAccessFile2);
                        if (a > 0) {
                            for (int i = a; i > 0; i--) {
                                int a2 = a(randomAccessFile2);
                                if (a2 > IncomingSmsFilterConsts.PAY_SMS) {
                                    break;
                                }
                                long readLong = randomAccessFile2.readLong();
                                byte[] bArr = new byte[a2];
                                if (a2 != randomAccessFile2.read(bArr, 0, a2)) {
                                    break;
                                }
                                arrayList2.add(new String(bArr));
                                arrayList.add(String.valueOf(readLong));
                            }
                            if (arrayList.size() > 0) {
                                z = true;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        randomAccessFile = randomAccessFile2;
                        if (randomAccessFile != null) {
                        }
                        throw th;
                    }
                }
                if (randomAccessFile2 != null) {
                    try {
                        randomAccessFile2.close();
                    } catch (IOException e) {
                    }
                }
                randomAccessFile = randomAccessFile2;
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = randomAccessFile2;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        } catch (Throwable th4) {
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:9:0x0023, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void bt(String str) {
        synchronized (ku.class) {
            if (gf.S().ab().booleanValue()) {
                Object bytes = str.getBytes();
                long currentTimeMillis = System.currentTimeMillis();
                if (xT) {
                    if (xU == null) {
                        xU = new ArrayList();
                        xV = new ArrayList();
                    }
                    xU.add(bytes);
                    xV.add(Long.valueOf(currentTimeMillis));
                } else {
                    a(bytes, currentTimeMillis, null, null);
                }
            }
        }
    }

    public static synchronized void dO() {
        synchronized (ku.class) {
            if (!gf.S().ab().booleanValue()) {
            } else if (xT) {
            } else {
                xT = true;
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                if (b(arrayList, arrayList2)) {
                    JceStruct aqVar = new aq();
                    aqVar.bC = 54;
                    aqVar.bI = new HashMap();
                    aqVar.bI.put(Integer.valueOf(2), arrayList);
                    aqVar.bI.put(Integer.valueOf(3), arrayList2);
                    im.bK().a(3122, aqVar, null, 0, new jy() {
                        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                            switch (ne.bg(i3)) {
                                case 0:
                                    ku.r(true);
                                    break;
                                default:
                                    ku.r(false);
                                    break;
                            }
                            ku.xU = null;
                            ku.xV = null;
                            ku.xT = false;
                        }
                    });
                    return;
                }
                xT = false;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00aa A:{SYNTHETIC, Splitter: B:39:0x00aa} */
    /* JADX WARNING: Removed duplicated region for block: B:51:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00b8 A:{SYNTHETIC, Splitter: B:46:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0079 A:{PHI: r0 , ExcHandler: all (th java.lang.Throwable), Splitter: B:5:0x0037} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:31:0x0079, code:
            r12 = th;
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            r0.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void r(boolean z) {
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + ".bufflocache001");
        int i = 0;
        RandomAccessFile randomAccessFile;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(file, "rws");
            try {
                long length = randomAccessFile.length();
                if ((length <= 0 ? 1 : null) != null) {
                    a(randomAccessFile, 0);
                } else if (z) {
                    randomAccessFile.setLength(0);
                    a(randomAccessFile, 0);
                } else {
                    i = a(randomAccessFile);
                    randomAccessFile.seek(length);
                }
                if (xU != null) {
                    if (xU.size() > 0) {
                        i += a(randomAccessFile, null, 0, new ArrayList(xU), new ArrayList(xV));
                        randomAccessFile.seek(0);
                        a(randomAccessFile, i);
                        if (randomAccessFile == null) {
                            try {
                                randomAccessFile.close();
                                return;
                            } catch (IOException e) {
                                return;
                            }
                        }
                        return;
                    }
                }
                if (i == 0) {
                    randomAccessFile.setLength(0);
                }
                if (randomAccessFile == null) {
                }
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
            Throwable th3 = th2;
            randomAccessFile = null;
            if (randomAccessFile != null) {
            }
            throw th3;
        }
    }
}
