package tmsdkobf;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.i;

public class la {
    public static synchronized void a(String str, String str2, int i) {
        synchronized (la.class) {
            try {
                if (TextUtils.isEmpty(str)) {
                    return;
                }
                ArrayList bE = bE(str2);
                if (bE.size() < 500) {
                    File file = new File(str2);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    if (i == 90 || i == SmsCheckResult.ESCT_163) {
                        bE = new ArrayList();
                    }
                    bE.add(TccCryptor.encrypt(str.getBytes(), null));
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                    objectOutputStream.writeObject(bE);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                } else {
                    return;
                }
            } catch (Throwable th) {
            }
        }
    }

    public static synchronized void b(int i, int i2, String str) {
        synchronized (la.class) {
            Object str2;
            if (str2 == null) {
                str2 = "";
            }
            try {
                JceStruct aoVar = new ao(SmsCheckResult.ESCT_184, new ArrayList());
                ap apVar = new ap(new HashMap());
                apVar.bG.put(Integer.valueOf(1), String.valueOf(i));
                apVar.bG.put(Integer.valueOf(2), String.valueOf(i2));
                apVar.bG.put(Integer.valueOf(3), str2);
                aoVar.bD.add(apVar);
                ob bK = im.bK();
                if (aoVar.bD.size() > 0 && bK != null) {
                    bK.a(4060, aoVar, null, 0, new jy() {
                        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                        }
                    });
                }
            } catch (Throwable th) {
            }
        }
    }

    public static synchronized ArrayList<String> bD(String str) {
        ArrayList<String> arrayList;
        synchronized (la.class) {
            arrayList = new ArrayList();
            try {
                ArrayList bE = bE(str);
                if (bE.size() > 0) {
                    Iterator it = bE.iterator();
                    while (it.hasNext()) {
                        arrayList.add(new String(TccCryptor.decrypt((byte[]) it.next(), null)));
                    }
                }
            } catch (Throwable th) {
            }
        }
        return arrayList;
    }

    static synchronized ArrayList<byte[]> bE(String str) {
        ArrayList<byte[]> arrayList;
        synchronized (la.class) {
            arrayList = new ArrayList();
            try {
                if (new File(str).exists()) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(str));
                    ArrayList arrayList2 = (ArrayList) objectInputStream.readObject();
                    if (arrayList2 != null) {
                        if (arrayList2.size() > 0) {
                            arrayList.addAll(arrayList2);
                        }
                    }
                    objectInputStream.close();
                } else {
                    return arrayList;
                }
            } catch (Throwable th) {
            }
        }
        return arrayList;
    }

    public static synchronized void bF(String str) {
        synchronized (la.class) {
            try {
                File file = new File(str);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Throwable th) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:54:0x00af, code:
            if ((r0 - tmsdkobf.kz.dX() <= (((long) r6) * 2) * 3600 ? 1 : null) == null) goto L_0x00b1;
     */
    /* JADX WARNING: Missing block: B:55:0x00b1, code:
            tmsdkobf.lh.eq();
     */
    /* JADX WARNING: Missing block: B:59:0x00c5, code:
            if ((r0 - tmsdkobf.kz.dX() > ((long) r6) * 3600 ? 1 : null) == null) goto L_0x009b;
     */
    /* JADX WARNING: Missing block: B:95:0x0142, code:
            if ((r0 - tmsdkobf.kz.ec() <= (((long) r7) * 2) * 3600 ? 1 : null) == null) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:96:0x0144, code:
            tmsdkobf.le.eq();
     */
    /* JADX WARNING: Missing block: B:100:0x0158, code:
            if ((r0 - tmsdkobf.kz.ec() > ((long) r7) * 3600 ? 1 : null) == null) goto L_0x012e;
     */
    /* JADX WARNING: Missing block: B:118:0x0191, code:
            if ((r0 - tmsdkobf.kz.ee() < ((long) r6) * 3600 ? 1 : null) == null) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:135:0x01ca, code:
            if ((r0 - tmsdkobf.kz.ef() <= (((long) r7) * 2) * 3600 ? 1 : null) == null) goto L_0x01cc;
     */
    /* JADX WARNING: Missing block: B:136:0x01cc, code:
            tmsdkobf.ld.et();
            tmsdkobf.ld.eq();
     */
    /* JADX WARNING: Missing block: B:140:0x01e3, code:
            if ((r0 - tmsdkobf.kz.ef() > ((long) r7) * 3600 ? 1 : null) == null) goto L_0x01b6;
     */
    /* JADX WARNING: Missing block: B:158:0x021c, code:
            if ((r0 - tmsdkobf.kz.eh() < ((long) r6) * 3600 ? 1 : null) == null) goto L_0x021e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void el() {
        synchronized (la.class) {
            int i;
            int i2;
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            Context applicaionContext = TMSDKContext.getApplicaionContext();
            boolean hm = i.hm();
            boolean K = i.K(applicaionContext);
            ky aJ = kz.aJ(141);
            if (aJ != null) {
                if (aJ.xZ && hm) {
                    i = 0;
                    try {
                        i = Integer.valueOf(aJ.yb).intValue();
                    } catch (Throwable th) {
                    }
                    if (i <= 0) {
                        i = SmsCheckResult.ESCT_168;
                    }
                    if ((currentTimeMillis - kz.dV() <= ((long) i) * 3600 ? 1 : null) == null) {
                        lb.en();
                    }
                }
            }
            aJ = kz.aJ(SmsCheckResult.ESCT_146);
            if (aJ != null) {
                if (aJ.xZ) {
                    i = 0;
                    try {
                        i = Integer.valueOf(aJ.yb).intValue();
                    } catch (Throwable th2) {
                    }
                    if (i <= 0) {
                        i = 24;
                    }
                    if ((kz.dX() > 0 ? 1 : null) == null) {
                        kz.l(currentTimeMillis);
                    } else {
                        if (K) {
                        }
                        if (hm) {
                        }
                    }
                }
            }
            aJ = kz.aJ(150);
            if (aJ != null && aJ.xZ) {
                i = 0;
                i2 = 0;
                try {
                    i = aJ.ya;
                    i2 = Integer.valueOf(aJ.yb).intValue();
                } catch (Throwable th3) {
                }
                if (i <= 0) {
                    i = 24;
                }
                if (i2 <= 0) {
                    i2 = 24;
                }
                if ((kz.eb() > 0 ? 1 : null) == null) {
                    kz.m((currentTimeMillis - 86400) - 1);
                }
                if ((currentTimeMillis - kz.eb() < ((long) i) * 3600 ? 1 : null) == null) {
                    le.ep();
                }
                if ((kz.ec() > 0 ? 1 : null) == null) {
                    kz.n(currentTimeMillis);
                } else {
                    if (K) {
                    }
                    if (hm) {
                    }
                }
            }
            aJ = kz.aJ(151);
            if (aJ != null && aJ.xZ) {
                i = 0;
                i2 = 0;
                try {
                    i = aJ.ya;
                    i2 = Integer.valueOf(aJ.yb).intValue();
                } catch (Throwable th4) {
                }
                if (i <= 0) {
                    i = 24;
                }
                if (i2 <= 0) {
                    i2 = 24;
                }
                if ((kz.ee() <= 0 ? 1 : null) == null) {
                }
                ld.ep();
                if ((kz.ef() > 0 ? 1 : null) == null) {
                    kz.p(currentTimeMillis);
                } else {
                    if (K) {
                    }
                    if (hm) {
                    }
                }
            }
            aJ = kz.aJ(SmsCheckResult.ESCT_163);
            if (aJ != null && aJ.xZ) {
                i = 0;
                i2 = 0;
                try {
                    i = aJ.ya;
                    i2 = Integer.valueOf(aJ.yb).intValue();
                } catch (Throwable th5) {
                }
                if (i <= 0) {
                    i = 4;
                }
                if (i2 <= 0) {
                    i2 = 24;
                }
                if ((kz.eh() <= 0 ? 1 : null) == null) {
                }
                lc.ep();
                if ((kz.ei() > 0 ? 1 : null) == null) {
                    kz.r(currentTimeMillis);
                } else if (hm) {
                    if ((currentTimeMillis - kz.ei() <= ((long) i2) * 3600 ? 1 : null) == null) {
                        lc.eq();
                    }
                }
            }
        }
    }

    public static boolean em() {
        long currentTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        if (!(Math.abs(currentTimeMillis - kz.ej()) >= 2000)) {
            return false;
        }
        kz.s(currentTimeMillis);
        return true;
    }

    public static synchronized void j(List<String> list) {
        synchronized (la.class) {
            kz.i(list);
        }
    }
}
