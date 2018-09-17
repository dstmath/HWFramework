package tmsdkobf;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.SpaceManager;

/* compiled from: Unknown */
public class rr {
    private static int OA;
    private static int OB;
    private static rr OD;
    private static final String[] Ot = null;
    private static final int Ou = 0;
    private static final int Ov = 0;
    private static final int Ow = 0;
    private static final int Ox = 0;
    private static int Oz;
    private rs OC;
    private ISpaceScanListener Or;
    private ISpaceScanListener Os;
    private AtomicBoolean Oy;
    byte[] lL;
    private int mState;
    private Handler yO;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.rr.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ rr OE;

        AnonymousClass1(rr rrVar, Looper looper) {
            this.OE = rrVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 4097:
                    this.OE.Or.onStart();
                case 4098:
                    this.OE.Or.onFound(message.obj);
                case 4099:
                    this.OE.Or.onProgressChanged(message.arg1);
                case 4100:
                    this.OE.Or.onCancelFinished();
                case 4101:
                    this.OE.Or.onFinish(message.arg1, message.obj);
                case 4353:
                    this.OE.Os.onStart();
                case 4354:
                    this.OE.Os.onFound(message.obj);
                case 4355:
                    this.OE.Os.onProgressChanged(message.arg1);
                case 4356:
                    this.OE.Os.onCancelFinished();
                case 4357:
                    this.OE.Os.onFinish(message.arg1, message.obj);
                default:
            }
        }
    }

    /* compiled from: Unknown */
    public static class a {
        public long OF;
        public long OG;
        public long mInnerPicSize;
        public long mOutPicSize;
        public Pair<Integer, Long> mPhotoCountAndSize;
        public ArrayList<rq> mResultList;
        public Pair<Integer, Long> mScreenShotCountAndSize;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rr.<clinit>():void");
    }

    private rr() {
        this.lL = new byte[0];
        this.Or = null;
        this.Os = null;
        this.mState = 0;
        this.Oy = new AtomicBoolean();
        this.mState = Oz;
        this.Oy.set(false);
        this.yO = new AnonymousClass1(this, Looper.getMainLooper());
    }

    private ArrayList<rq> a(ContentResolver contentResolver, Uri uri, String[] strArr) {
        Cursor query;
        Throwable th;
        Cursor cursor;
        ArrayList<rq> arrayList = new ArrayList();
        try {
            query = contentResolver.query(uri, strArr, null, null, null);
            if (query == null) {
                try {
                    d.c("PhotoManager", "cursor is null!");
                } catch (Throwable th2) {
                    th = th2;
                }
            } else {
                query.moveToFirst();
                int count = query.getCount();
                int i = 0;
                while (!query.isAfterLast() && !this.Oy.get()) {
                    String string = query.getString(Ow);
                    i++;
                    int i2 = (i * 100) / count;
                    Message obtainMessage = this.yO.obtainMessage(4098);
                    obtainMessage.obj = string;
                    this.yO.sendMessage(obtainMessage);
                    obtainMessage = this.yO.obtainMessage(4099);
                    obtainMessage.arg1 = i2;
                    this.yO.sendMessage(obtainMessage);
                    if (dM(string)) {
                        if (rp.dF(string)) {
                            arrayList.add(new rq(b(query.getLong(Ov), string), c(query.getLong(Ox), string), string, query.getLong(Ou)));
                            query.moveToNext();
                        }
                    }
                    d.f("PhotoManager", "media file not exist : " + string);
                    query.moveToNext();
                }
            }
            if (query != null) {
                try {
                    query.close();
                } catch (Exception e) {
                }
            }
        } catch (Throwable th3) {
            th = th3;
            query = null;
            if (query != null) {
                query.close();
            }
            throw th;
        }
        return arrayList;
    }

    private static long b(long j, String str) {
        Object obj = null;
        if (j <= 0) {
            obj = 1;
        }
        if (obj == null) {
            return j;
        }
        long dK = !rp.dH(str) ? j : rp.dK(str);
        if (dK == 0) {
            dK = new File(str).lastModified();
        }
        return dK;
    }

    private void b(a aVar) {
        if (aVar.mResultList != null) {
            aVar.mInnerPicSize = 0;
            aVar.OF = 0;
            aVar.mOutPicSize = 0;
            aVar.OG = 0;
            Iterator it = aVar.mResultList.iterator();
            int i = 0;
            long j = 0;
            long j2 = 0;
            int i2 = 0;
            while (it.hasNext()) {
                rq rqVar = (rq) it.next();
                if (rqVar.mIsScreenShot) {
                    j += rqVar.mSize;
                    i++;
                }
                j2 += rqVar.mSize;
                i2++;
                if (rqVar.mIsOut) {
                    aVar.mOutPicSize += rqVar.mSize;
                } else {
                    aVar.mInnerPicSize += rqVar.mSize;
                }
            }
            aVar.mPhotoCountAndSize = new Pair(Integer.valueOf(i2), Long.valueOf(j2));
            aVar.mScreenShotCountAndSize = new Pair(Integer.valueOf(i), Long.valueOf(j));
        }
    }

    private static long c(long j, String str) {
        return ((j > 0 ? 1 : (j == 0 ? 0 : -1)) <= 0 ? 1 : null) == null ? j : new File(str).length();
    }

    private static int dL(String str) {
        int i = 0;
        String[] strArr = Ot;
        int length = strArr.length;
        int i2 = 0;
        while (i < length) {
            if (strArr[i].equals(str)) {
                return i2;
            }
            i2++;
            i++;
        }
        return -1;
    }

    private static boolean dM(String str) {
        return TextUtils.isEmpty(str) ? false : new File(str).exists();
    }

    public static rr jH() {
        if (OD == null) {
            OD = new rr();
        }
        return OD;
    }

    public List<ro> D(ArrayList<rq> arrayList) {
        if (this.OC == null) {
            this.OC = new rs();
        }
        this.mState = OB;
        this.yO.sendMessage(this.yO.obtainMessage(4353));
        long currentTimeMillis = System.currentTimeMillis();
        List<ro> a = this.OC.a((ArrayList) arrayList, this.yO);
        d.f("PhotoManager", "Similar time consume : " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
        return a;
    }

    public void b(ISpaceScanListener iSpaceScanListener) {
        this.Or = iSpaceScanListener;
    }

    public void c(ISpaceScanListener iSpaceScanListener) {
        this.Os = iSpaceScanListener;
    }

    public int jI() {
        if (OA != this.mState) {
            return -1;
        }
        this.Oy.set(true);
        return 1;
    }

    public int jJ() {
        if (OB != this.mState) {
            return -1;
        }
        this.OC.cancel();
        this.mState = Oz;
        return 1;
    }

    public a jK() {
        this.Oy.set(false);
        this.mState = OA;
        this.yO.sendMessage(this.yO.obtainMessage(4097));
        long currentTimeMillis = System.currentTimeMillis();
        Object a = a(TMSDKContext.getApplicaionContext().getContentResolver(), Media.EXTERNAL_CONTENT_URI, Ot);
        if (this.Oy.get()) {
            this.yO.sendMessage(this.yO.obtainMessage(4100));
            this.mState = Oz;
            return null;
        }
        if (a.size() == 0) {
            this.mState = Oz;
            d.c("PhotoManager", "no picture was found");
            Message obtainMessage = this.yO.obtainMessage(4101);
            obtainMessage.obj = null;
            obtainMessage.arg1 = SpaceManager.ERROR_CODE_UNKNOW;
            this.yO.sendMessage(obtainMessage);
        }
        rq.I(a);
        if (this.Oy.get()) {
            this.yO.sendMessage(this.yO.obtainMessage(4100));
            this.mState = Oz;
            return null;
        }
        a aVar = new a();
        aVar.mResultList = a;
        b(aVar);
        this.mState = Oz;
        d.f("PhotoManager", "scan time consume : " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
        Message obtainMessage2 = this.yO.obtainMessage(4101);
        obtainMessage2.obj = rd.a(aVar);
        obtainMessage2.arg1 = 0;
        this.yO.sendMessage(obtainMessage2);
        return aVar;
    }
}
