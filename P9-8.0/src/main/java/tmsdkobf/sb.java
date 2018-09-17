package tmsdkobf;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.utils.f;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.SpaceManager;

public class sb {
    private static int QA = 4;
    private static sb QC = null;
    private static final String[] Qq = new String[]{"_id", "_data", "datetaken", "_size"};
    private static final int Qr = dH("_id");
    private static final int Qs = dH("datetaken");
    private static final int Qt = dH("_data");
    private static final int Qu = dH("_size");
    private static int Qx = 1;
    private static int Qy = 2;
    private static int Qz = 3;
    private sc QB;
    private ISpaceScanListener Qo;
    private ISpaceScanListener Qp;
    private int Qv;
    private AtomicBoolean Qw;
    private int mState;
    byte[] mg;
    private Handler vW;

    public static class a {
        public long QE;
        public long QF;
        public long mInnerPicSize;
        public long mOutPicSize;
        public Pair<Integer, Long> mPhotoCountAndSize;
        public ArrayList<sa> mResultList;
        public Pair<Integer, Long> mScreenShotCountAndSize;
    }

    private sb() {
        this.mg = new byte[0];
        this.Qo = null;
        this.Qp = null;
        this.mState = 0;
        this.Qv = 0;
        this.Qw = new AtomicBoolean();
        this.mState = Qx;
        this.Qv = QA;
        this.Qw.set(false);
        this.vW = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 4097:
                        if (sb.this.Qo != null) {
                            sb.this.Qo.onStart();
                            return;
                        }
                        return;
                    case 4098:
                        if (sb.this.Qo != null) {
                            sb.this.Qo.onFound(message.obj);
                            return;
                        }
                        return;
                    case 4099:
                        if (sb.this.Qo != null) {
                            sb.this.Qo.onProgressChanged(message.arg1);
                            return;
                        }
                        return;
                    case 4100:
                        if (sb.this.Qo != null) {
                            sb.this.Qo.onCancelFinished();
                            sb.this.Qo = null;
                            sb.this.mState = sb.Qx;
                            return;
                        }
                        return;
                    case 4101:
                        if (sb.this.Qo != null) {
                            sb.this.Qo.onFinish(message.arg1, message.obj);
                            sb.this.Qo = null;
                            sb.this.mState = sb.Qx;
                            return;
                        }
                        return;
                    case 4353:
                        if (sb.this.Qp != null) {
                            sb.this.Qp.onStart();
                            return;
                        }
                        return;
                    case 4354:
                        if (sb.this.Qp != null) {
                            sb.this.Qp.onFound(message.obj);
                            return;
                        }
                        return;
                    case 4355:
                        if (sb.this.Qp != null) {
                            sb.this.Qp.onProgressChanged(message.arg1);
                            return;
                        }
                        return;
                    case 4356:
                        if (sb.this.Qp != null) {
                            sb.this.Qp.onCancelFinished();
                            sb.this.Qp = null;
                            sb.this.Qv = sb.QA;
                            return;
                        }
                        return;
                    case 4357:
                        if (sb.this.Qp != null) {
                            sb.this.Qp.onFinish(message.arg1, message.obj);
                            sb.this.Qp = null;
                            sb.this.Qv = sb.QA;
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private static long a(long -l_3_J, String str) {
        Object obj = null;
        if (-l_3_J <= 0) {
            obj = 1;
        }
        if (obj == null) {
            return -l_3_J;
        }
        if (rz.dD(str)) {
            -l_3_J = rz.dG(str);
        }
        if (-l_3_J == 0) {
            -l_3_J = new File(str).lastModified();
        }
        return -l_3_J;
    }

    private ArrayList<sa> a(ContentResolver contentResolver, Uri uri, String[] strArr, String[] strArr2) {
        ArrayList<sa> arrayList = new ArrayList();
        Cursor cursor = null;
        if (strArr2 != null) {
            try {
                String[] c = c(strArr2);
                String str = "bucket_id=?";
                for (int i = 1; i < strArr2.length; i++) {
                    str = str + " OR bucket_id=?";
                }
                cursor = contentResolver.query(uri, strArr, str + " AND _size>30720", c, null);
            } catch (Throwable th) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            cursor = contentResolver.query(uri, strArr, "_size>30720", null, null);
        }
        if (cursor == null) {
            f.e("PhotoManager", "cursor is null!");
        } else {
            cursor.moveToFirst();
            int count = cursor.getCount();
            int i2 = 0;
            while (!cursor.isAfterLast() && !this.Qw.get()) {
                String string = cursor.getString(Qt);
                i2++;
                int i3 = (i2 * 100) / count;
                Message obtainMessage = this.vW.obtainMessage(4098);
                obtainMessage.obj = string;
                this.vW.sendMessage(obtainMessage);
                Message obtainMessage2 = this.vW.obtainMessage(4099);
                obtainMessage2.arg1 = i3;
                this.vW.sendMessage(obtainMessage2);
                if (dI(string)) {
                    if (rz.dB(string)) {
                        ArrayList<sa> arrayList2 = arrayList;
                        arrayList2.add(new sa(a(cursor.getLong(Qs), string), b(cursor.getLong(Qu), string), string, cursor.getLong(Qr)));
                        cursor.moveToNext();
                    }
                }
                f.g("PhotoManager", "media file not exist : " + string);
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e2) {
            }
        }
        return arrayList;
    }

    private static long b(long -l_3_J, String str) {
        return ((-l_3_J > 0 ? 1 : (-l_3_J == 0 ? 0 : -1)) <= 0 ? 1 : null) == null ? -l_3_J : new File(str).length();
    }

    private void b(a aVar) {
        long j = 0;
        long j2 = 0;
        int i = 0;
        int i2 = 0;
        if (aVar.mResultList != null) {
            aVar.mInnerPicSize = 0;
            aVar.QE = 0;
            aVar.mOutPicSize = 0;
            aVar.QF = 0;
            Iterator it = aVar.mResultList.iterator();
            while (it.hasNext()) {
                sa saVar = (sa) it.next();
                if (saVar.mIsScreenShot) {
                    j2 += saVar.mSize;
                    i2++;
                }
                j += saVar.mSize;
                i++;
                if (saVar.mIsOut) {
                    aVar.mOutPicSize += saVar.mSize;
                } else {
                    aVar.mInnerPicSize += saVar.mSize;
                }
            }
            aVar.mPhotoCountAndSize = new Pair(Integer.valueOf(i), Long.valueOf(j));
            aVar.mScreenShotCountAndSize = new Pair(Integer.valueOf(i2), Long.valueOf(j2));
        }
    }

    private String[] c(String[] strArr) {
        String[] strArr2 = new String[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            strArr2[i] = String.valueOf(strArr[i].toLowerCase().hashCode());
        }
        return strArr2;
    }

    private static int dH(String str) {
        int i = 0;
        for (String equals : Qq) {
            if (equals.equals(str)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static boolean dI(String str) {
        return TextUtils.isEmpty(str) ? false : new File(str).exists();
    }

    public static sb kq() {
        if (QC == null) {
            QC = new sb();
        }
        return QC;
    }

    public void b(ISpaceScanListener iSpaceScanListener) {
        this.Qo = iSpaceScanListener;
    }

    /* JADX WARNING: Missing block: B:7:0x0013, code:
            r13.vW.sendMessage(r13.vW.obtainMessage(4097));
            r2 = java.lang.System.currentTimeMillis();
            r6 = a(tmsdk.common.TMSDKContext.getApplicaionContext().getContentResolver(), android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Qq, r14);
     */
    /* JADX WARNING: Missing block: B:8:0x003a, code:
            if (r13.Qw.get() != false) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:10:0x0040, code:
            if (r6.size() == 0) goto L_0x008a;
     */
    /* JADX WARNING: Missing block: B:11:0x0042, code:
            tmsdkobf.sa.O(r6);
     */
    /* JADX WARNING: Missing block: B:12:0x004b, code:
            if (r13.Qw.get() != false) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:13:0x004d, code:
            r7 = new tmsdkobf.sb.a();
            r7.mResultList = r6;
            b(r7);
            r8 = java.lang.System.currentTimeMillis() - r2;
            r0 = r13.vW.obtainMessage(4101);
            r0.obj = tmsdkobf.ri.a(r7);
            r0.arg1 = 0;
            r13.vW.sendMessage(r0);
     */
    /* JADX WARNING: Missing block: B:14:0x0074, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:22:0x007b, code:
            r13.vW.sendMessage(r13.vW.obtainMessage(4100));
     */
    /* JADX WARNING: Missing block: B:23:0x0089, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:24:0x008a, code:
            r7 = r13.vW.obtainMessage(4101);
            r7.obj = null;
            r7.arg1 = tmsdk.fg.module.spacemanager.SpaceManager.ERROR_CODE_IMG_NOT_FOUND;
            r13.vW.sendMessage(r7);
     */
    /* JADX WARNING: Missing block: B:25:0x009f, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:26:0x00a0, code:
            r13.vW.sendMessage(r13.vW.obtainMessage(4100));
     */
    /* JADX WARNING: Missing block: B:27:0x00ae, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean b(String[] strArr) {
        synchronized (this) {
            if (this.mState == Qx) {
                this.Qw.set(false);
                this.mState = Qy;
            } else {
                return false;
            }
        }
    }

    public void c(ISpaceScanListener iSpaceScanListener) {
        this.Qp = iSpaceScanListener;
    }

    public int kr() {
        if (Qy != this.mState) {
            return -1;
        }
        this.Qw.set(true);
        return 1;
    }

    public int ks() {
        if (Qz != this.Qv) {
            return -1;
        }
        this.QB.cancel();
        this.Qv = QA;
        return 1;
    }

    public boolean w(ArrayList<sa> arrayList) {
        Class cls = SpaceManager.class;
        synchronized (SpaceManager.class) {
            if (this.Qv == QA) {
                if (this.QB == null) {
                    this.QB = new sc();
                }
                this.Qv = Qz;
                this.vW.sendMessage(this.vW.obtainMessage(4353));
                long currentTimeMillis = System.currentTimeMillis();
                List a = this.QB.a((ArrayList) arrayList, this.vW);
                String str = "PhotoManager";
                f.g(str, "Similar time consume : " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
                return true;
            }
            return false;
        }
    }
}
