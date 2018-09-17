package tmsdkobf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.fg.module.spacemanager.SpaceManager;

/* compiled from: Unknown */
public class mb {
    private static volatile boolean Av;
    private static List<byte[]> Aw;
    private static List<Long> Ax;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.mb.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.mb.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.mb.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void B(boolean z) {
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + ".bufflocache001");
        RandomAccessFile randomAccessFile2;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            randomAccessFile2 = new RandomAccessFile(file, "rws");
            try {
                int i;
                int a;
                long length = randomAccessFile2.length();
                if ((length <= 0 ? 1 : null) != null) {
                    a(randomAccessFile2, 0);
                    i = 0;
                } else if (z) {
                    randomAccessFile2.setLength(0);
                    a(randomAccessFile2, 0);
                    i = 0;
                } else {
                    a = a(randomAccessFile2);
                    randomAccessFile2.seek(length);
                    i = a;
                }
                if (Aw != null && Aw.size() > 0) {
                    a = a(randomAccessFile2, null, 0, new ArrayList(Aw), new ArrayList(Ax)) + i;
                    randomAccessFile2.seek(0);
                    a(randomAccessFile2, a);
                } else if (i == 0) {
                    randomAccessFile2.setLength(0);
                }
                if (randomAccessFile2 != null) {
                    try {
                        randomAccessFile2.close();
                    } catch (IOException e) {
                    }
                }
            } catch (Throwable th2) {
                Throwable th3 = th2;
                randomAccessFile = randomAccessFile2;
                th = th3;
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            throw th;
        }
    }

    private static int a(RandomAccessFile randomAccessFile) throws Exception {
        if (randomAccessFile.readInt() == 712365948) {
            return randomAccessFile.readInt();
        }
        throw new Exception();
    }

    private static int a(RandomAccessFile randomAccessFile, int i, int i2) throws Exception {
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        do {
            int a = a(randomAccessFile);
            long readLong = randomAccessFile.readLong();
            if (a > 255) {
                i2 = 0;
                break;
            }
            Object obj = new byte[a];
            if (randomAccessFile.read(obj, 0, a) != a) {
                i2 = 0;
                break;
            }
            if (i <= i2) {
                arrayList.add(obj);
                arrayList2.add(Long.valueOf(readLong));
            }
            i--;
        } while (i > 0);
        randomAccessFile.setLength(0);
        if (i2 != 0) {
            a(randomAccessFile, 0);
            i2 = a(randomAccessFile, null, 0, arrayList, arrayList2);
            long filePointer = randomAccessFile.getFilePointer();
            randomAccessFile.seek(0);
            a(randomAccessFile, i2);
            randomAccessFile.seek(filePointer);
            return i2;
        }
        a(randomAccessFile, 0);
        return i2;
    }

    private static int a(RandomAccessFile randomAccessFile, byte[] bArr, long j, List<byte[]> list, List<Long> list2) throws Exception {
        int i;
        if (bArr != null && bArr.length > 0) {
            i = 1;
            a(randomAccessFile, bArr.length);
            randomAccessFile.writeLong(j);
            randomAccessFile.write(bArr);
        } else {
            i = 0;
        }
        if (list != null && list.size() > 0) {
            int size = list.size();
            int i2 = 0;
            while (i2 < size) {
                int i3;
                byte[] bArr2 = (byte[]) list.get(i2);
                if (bArr2 != null && bArr2.length > 0) {
                    int i4 = i + 1;
                    bArr2 = (byte[]) list.get(i2);
                    a(randomAccessFile, bArr2.length);
                    randomAccessFile.writeLong(((Long) list2.get(i2)).longValue());
                    randomAccessFile.write(bArr2);
                    i3 = i4;
                } else {
                    i3 = i;
                }
                i2++;
                i = i3;
            }
        }
        return i;
    }

    private static void a(RandomAccessFile randomAccessFile, int i) throws Exception {
        randomAccessFile.writeInt(712365948);
        randomAccessFile.writeInt(i);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(byte[] bArr, long j, List<byte[]> list, List<Long> list2) {
        RandomAccessFile randomAccessFile;
        Throwable th;
        RandomAccessFile randomAccessFile2 = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + ".bufflocache001");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(file, "rws");
            try {
                int a;
                int i;
                if ((randomAccessFile.length() <= 0 ? 1 : null) == null) {
                    a = a(randomAccessFile);
                    if (a <= 20) {
                        randomAccessFile.seek(randomAccessFile.length());
                    } else {
                        a = a(randomAccessFile, a, 15);
                    }
                    i = a;
                } else {
                    a(randomAccessFile, 0);
                    i = 0;
                }
                a = a(randomAccessFile, bArr, j, list, list2) + i;
                randomAccessFile.seek(0);
                a(randomAccessFile, a);
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e2) {
            } catch (Throwable th2) {
                Throwable th3 = th2;
                randomAccessFile2 = randomAccessFile;
                th = th3;
                if (randomAccessFile2 != null) {
                    try {
                        randomAccessFile2.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (randomAccessFile2 != null) {
                randomAccessFile2.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean b(ArrayList<String> arrayList, ArrayList<String> arrayList2) {
        boolean z;
        RandomAccessFile randomAccessFile = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + ".bufflocache001");
        if (!file.exists()) {
            return false;
        }
        RandomAccessFile randomAccessFile2;
        try {
            randomAccessFile2 = new RandomAccessFile(file, "rws");
            try {
                boolean z2;
                if (!(randomAccessFile2.length() <= 0)) {
                    int a = a(randomAccessFile2);
                    if (a > 0) {
                        while (a > 0) {
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
                            a--;
                        }
                        if (arrayList.size() > 0) {
                            z2 = true;
                            if (randomAccessFile2 != null) {
                                try {
                                    randomAccessFile2.close();
                                } catch (IOException e) {
                                }
                            }
                            z = z2;
                            return z;
                        }
                    }
                }
                z2 = false;
                if (randomAccessFile2 != null) {
                    randomAccessFile2.close();
                }
                z = z2;
            } catch (IOException e2) {
            } catch (Throwable th) {
                Throwable th2 = th;
                randomAccessFile = randomAccessFile2;
                r0 = th2;
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e3) {
                    }
                }
                throw r0;
            }
        } catch (Throwable th3) {
            Throwable th4;
            th4 = th3;
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            throw th4;
        }
        return z;
    }

    public static synchronized void cq(String str) {
        synchronized (mb.class) {
            if (fw.w().F().booleanValue()) {
                Object bytes = str.getBytes();
                long currentTimeMillis = System.currentTimeMillis();
                if (Av) {
                    if (Aw == null) {
                        Aw = new ArrayList();
                        Ax = new ArrayList();
                    }
                    Aw.add(bytes);
                    Ax.add(Long.valueOf(currentTimeMillis));
                } else {
                    a(bytes, currentTimeMillis, null, null);
                }
                return;
            }
        }
    }

    public static synchronized void eE() {
        synchronized (mb.class) {
            if (!fw.w().F().booleanValue()) {
            } else if (Av) {
            } else {
                Av = true;
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                if (b(arrayList, arrayList2)) {
                    fs ajVar = new aj();
                    ajVar.bc = 54;
                    ajVar.bd = new HashMap();
                    ajVar.bd.put(Integer.valueOf(2), arrayList);
                    ajVar.bd.put(Integer.valueOf(3), arrayList2);
                    jq.cu().a(3122, ajVar, null, 0, new lg() {
                        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
                            switch (oi.bV(i3)) {
                                case SpaceManager.ERROR_CODE_OK /*0*/:
                                    mb.B(true);
                                    break;
                                default:
                                    mb.B(false);
                                    break;
                            }
                            mb.Aw = null;
                            mb.Ax = null;
                            mb.Av = false;
                        }
                    });
                    return;
                }
                Av = false;
            }
        }
    }
}
