package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.spacemanager.FileMedia;
import tmsdk.fg.module.spacemanager.FileScanResult;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.PhotoScanResult;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult.PhotoSimilarBucketItem;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles;
import tmsdkobf.ru.a;

/* compiled from: Unknown */
public class rd extends BaseManagerF {
    ru ME;
    ri MF;
    Runnable MG;
    Runnable MH;
    a MI;
    a MJ;
    public ISpaceScanListener MK;
    public ISpaceScanListener ML;
    private SpaceManager MM;
    private rr MN;
    Context mContext;
    Object mLock;
    Handler yO;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.rd.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ rd MO;
        final /* synthetic */ List MR;

        AnonymousClass6(rd rdVar, List list) {
            this.MO = rdVar;
            this.MR = list;
        }

        public void run() {
            this.MO.ML.onFinish(0, this.MR);
        }
    }

    public rd() {
        this.mLock = new Object();
        this.yO = new Handler(Looper.getMainLooper());
        this.MN = rr.jH();
        this.mContext = TMSDKContext.getApplicaionContext();
        init();
    }

    private List<WeChatCacheFiles> E(List<rj> list) {
        List<WeChatCacheFiles> arrayList = new ArrayList();
        for (rj rjVar : list) {
            if (rjVar.mFileModes.size() > 0) {
                arrayList.add(rjVar.jG());
            }
        }
        return arrayList;
    }

    public static ArrayList<rq> F(List<PhotoItem> list) {
        ArrayList<rq> arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            PhotoItem photoItem = (PhotoItem) list.get(i);
            arrayList.add(new rq(photoItem.mTime, photoItem.mSize, photoItem.mPath, photoItem.mDbId));
        }
        return arrayList;
    }

    public static List<PhotoSimilarResult> G(List<ro> list) {
        List arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ro roVar = (ro) list.get(i);
            PhotoSimilarResult photoSimilarResult = new PhotoSimilarResult();
            photoSimilarResult.mItemList = new ArrayList();
            photoSimilarResult.mTime = roVar.mTime;
            photoSimilarResult.mTimeString = roVar.mTimeString;
            int size2 = roVar.mItemList.size();
            for (int i2 = 0; i2 < size2; i2++) {
                PhotoSimilarBucketItem photoSimilarBucketItem = new PhotoSimilarBucketItem();
                photoSimilarBucketItem.mId = ((ro.a) roVar.mItemList.get(i2)).mDbId;
                photoSimilarBucketItem.mPath = ((ro.a) roVar.mItemList.get(i2)).mPath;
                photoSimilarBucketItem.mFileSize = ((ro.a) roVar.mItemList.get(i2)).mSize;
                photoSimilarBucketItem.mSelected = ((ro.a) roVar.mItemList.get(i2)).mSelected;
                photoSimilarResult.mItemList.add(photoSimilarBucketItem);
            }
            arrayList.add(photoSimilarResult);
        }
        return arrayList;
    }

    public static PhotoScanResult a(rr.a aVar) {
        PhotoScanResult photoScanResult = new PhotoScanResult();
        photoScanResult.mInnerPicSize = aVar.mInnerPicSize;
        photoScanResult.mOutPicSize = aVar.mOutPicSize;
        photoScanResult.mPhotoCountAndSize = new Pair(Integer.valueOf(((Integer) aVar.mPhotoCountAndSize.first).intValue()), Long.valueOf(((Long) aVar.mPhotoCountAndSize.second).longValue()));
        photoScanResult.mScreenShotCountAndSize = new Pair(Integer.valueOf(((Integer) aVar.mScreenShotCountAndSize.first).intValue()), Long.valueOf(((Long) aVar.mScreenShotCountAndSize.second).longValue()));
        photoScanResult.mResultList = new ArrayList();
        int size = aVar.mResultList.size();
        for (int i = 0; i < size; i++) {
            rq rqVar = (rq) aVar.mResultList.get(i);
            PhotoItem photoItem = new PhotoItem();
            photoItem.mDbId = rqVar.mDbId;
            photoItem.mIsOut = rqVar.mIsOut;
            photoItem.mIsScreenShot = rqVar.mIsScreenShot;
            photoItem.mPath = rqVar.mPath;
            photoItem.mSize = rqVar.mSize;
            photoItem.mTime = rqVar.mTime;
            photoScanResult.mResultList.add(photoItem);
        }
        return photoScanResult;
    }

    private void a(a aVar, FileScanResult fileScanResult, boolean z) {
        List<OfflineVideo> bw = new hh(null, z).bw();
        if (bw != null && bw.size() != 0) {
            long j = 0;
            List arrayList = new ArrayList();
            rf rfVar = null;
            for (OfflineVideo offlineVideo : bw) {
                if (rfVar == null) {
                    rfVar = new rf();
                    rfVar.jz();
                }
                if (TextUtils.isEmpty(offlineVideo.mAppName)) {
                    String toLowerCase = re.a(offlineVideo.mPath, rf.MY).toLowerCase();
                    offlineVideo.mPackage = rfVar.f(toLowerCase, z);
                    offlineVideo.mAppName = rfVar.g(toLowerCase, z);
                }
                FileMedia fileMedia = new FileMedia();
                fileMedia.type = 2;
                fileMedia.mPath = offlineVideo.mPath;
                fileMedia.title = offlineVideo.mTitle;
                fileMedia.pkg = offlineVideo.mPackage;
                fileMedia.mSrcName = offlineVideo.mAppName;
                fileMedia.mSize = offlineVideo.mSize;
                fileMedia.mOfflineVideo = offlineVideo;
                j += fileMedia.mSize;
                arrayList.add(fileMedia);
                if (aVar != null) {
                    aVar.a(j, fileMedia);
                }
            }
            if (fileScanResult.mVideoFiles != null) {
                arrayList.addAll(fileScanResult.mVideoFiles);
            }
            fileScanResult.mVideoFiles = arrayList;
        }
    }

    private void init() {
        js();
        jv();
    }

    private void js() {
        this.MG = new Runnable() {
            final /* synthetic */ rd MO;

            /* compiled from: Unknown */
            /* renamed from: tmsdkobf.rd.1.2 */
            class AnonymousClass2 implements Runnable {
                final /* synthetic */ AnonymousClass1 MP;
                final /* synthetic */ FileScanResult MQ;

                AnonymousClass2(AnonymousClass1 anonymousClass1, FileScanResult fileScanResult) {
                    this.MP = anonymousClass1;
                    this.MQ = fileScanResult;
                }

                public void run() {
                    this.MP.MO.MK.onFinish(0, this.MQ);
                }
            }

            {
                this.MO = r1;
            }

            public void run() {
                this.MO.jx();
                boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
                if (this.MO.MK != null) {
                    this.MO.yO.post(new Runnable() {
                        final /* synthetic */ AnonymousClass1 MP;

                        {
                            this.MP = r1;
                        }

                        public void run() {
                            this.MP.MO.MK.onStart();
                        }
                    });
                    this.MO.ME = new ru(this.MO.mContext, this.MO.MI, SmsCheckResult.ESCT_263);
                    FileScanResult S = this.MO.ME.S(isENG);
                    this.MO.a(this.MO.MI, S, isENG);
                    synchronized (this.MO.mLock) {
                        this.MO.yO.post(new AnonymousClass2(this, S));
                    }
                }
            }
        };
    }

    private void jv() {
        this.MH = new Runnable() {
            final /* synthetic */ rd MO;

            {
                this.MO = r1;
            }

            public void run() {
                this.MO.jw();
            }
        };
    }

    private boolean jw() {
        jy();
        if (this.ML == null) {
            return false;
        }
        this.yO.post(new Runnable() {
            final /* synthetic */ rd MO;

            {
                this.MO = r1;
            }

            public void run() {
                this.MO.ML.onStart();
            }
        });
        List R = rg.R(((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        this.MF = new ri(R, 1);
        if (this.MJ == null) {
            rg.ND = null;
            this.yO.post(new Runnable() {
                final /* synthetic */ rd MO;

                {
                    this.MO = r1;
                }

                public void run() {
                    this.MO.ML.onFinish(-2, null);
                    this.MO.ML = null;
                }
            });
            return false;
        } else if (this.MF.a(this.MJ)) {
            this.yO.post(new AnonymousClass6(this, E(R)));
            this.MF = null;
            rg.ND = null;
            return true;
        } else {
            rg.ND = null;
            this.yO.post(new Runnable() {
                final /* synthetic */ rd MO;

                {
                    this.MO = r1;
                }

                public void run() {
                    this.MO.ML.onFinish(-1, null);
                    this.MO.ML = null;
                }
            });
            return false;
        }
    }

    private void jx() {
        if (this.MI == null) {
            this.MI = new a() {
                final /* synthetic */ rd MO;

                /* compiled from: Unknown */
                /* renamed from: tmsdkobf.rd.7.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ Object MS;
                    final /* synthetic */ AnonymousClass7 MT;

                    AnonymousClass1(AnonymousClass7 anonymousClass7, Object obj) {
                        this.MT = anonymousClass7;
                        this.MS = obj;
                    }

                    public void run() {
                        this.MT.MO.MK.onFound(this.MS);
                    }
                }

                /* compiled from: Unknown */
                /* renamed from: tmsdkobf.rd.7.3 */
                class AnonymousClass3 implements Runnable {
                    final /* synthetic */ AnonymousClass7 MT;
                    final /* synthetic */ int MU;

                    AnonymousClass3(AnonymousClass7 anonymousClass7, int i) {
                        this.MT = anonymousClass7;
                        this.MU = i;
                    }

                    public void run() {
                        this.MT.MO.MK.onProgressChanged(this.MU);
                    }
                }

                {
                    this.MO = r1;
                }

                public void a(long j, Object obj) {
                    if (this.MO.MK != null) {
                        this.MO.yO.post(new AnonymousClass1(this, obj));
                    }
                }

                public void onCancel() {
                    if (this.MO.MK != null) {
                        this.MO.yO.post(new Runnable() {
                            final /* synthetic */ AnonymousClass7 MT;

                            {
                                this.MT = r1;
                            }

                            public void run() {
                                this.MT.MO.MK.onCancelFinished();
                            }
                        });
                    }
                }

                public void onProgressChanged(int i) {
                    if (this.MO.MK != null) {
                        if (i >= 100) {
                            i = 99;
                        }
                        this.MO.yO.post(new AnonymousClass3(this, i));
                    }
                }
            };
        }
    }

    private void jy() {
        if (this.MJ == null) {
            this.MJ = new a() {
                final /* synthetic */ rd MO;

                /* compiled from: Unknown */
                /* renamed from: tmsdkobf.rd.8.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ Object MS;
                    final /* synthetic */ AnonymousClass8 MV;

                    AnonymousClass1(AnonymousClass8 anonymousClass8, Object obj) {
                        this.MV = anonymousClass8;
                        this.MS = obj;
                    }

                    public void run() {
                        this.MV.MO.ML.onFound(this.MS);
                    }
                }

                /* compiled from: Unknown */
                /* renamed from: tmsdkobf.rd.8.3 */
                class AnonymousClass3 implements Runnable {
                    final /* synthetic */ AnonymousClass8 MV;
                    final /* synthetic */ int MW;

                    AnonymousClass3(AnonymousClass8 anonymousClass8, int i) {
                        this.MV = anonymousClass8;
                        this.MW = i;
                    }

                    public void run() {
                        this.MV.MO.ML.onProgressChanged(this.MW);
                    }
                }

                {
                    this.MO = r1;
                }

                public void a(long j, Object obj) {
                    if (this.MO.ML != null) {
                        this.MO.yO.post(new AnonymousClass1(this, obj));
                    }
                }

                public void onCancel() {
                    if (this.MO.ML != null) {
                        this.MO.yO.post(new Runnable() {
                            final /* synthetic */ AnonymousClass8 MV;

                            {
                                this.MV = r1;
                            }

                            public void run() {
                                this.MV.MO.ML.onCancelFinished();
                            }
                        });
                    }
                }

                public void onProgressChanged(int i) {
                    if (this.MO.ML != null) {
                        this.MO.yO.post(new AnonymousClass3(this, i));
                    }
                }
            };
        }
    }

    public void a(ISpaceScanListener iSpaceScanListener) {
        this.MN.b(iSpaceScanListener);
        this.MN.jK();
        ma.bx(29992);
    }

    public void a(ISpaceScanListener iSpaceScanListener, List<PhotoItem> list) {
        this.MN.c(iSpaceScanListener);
        this.MN.D(F(list));
        ma.bx(29993);
    }

    public void a(SpaceManager spaceManager) {
        this.MM = spaceManager;
    }

    public void jq() {
        jq.ct().a(this.MG, "scanbigfile");
        ma.bx(29990);
    }

    public void jr() {
        if (this.ME != null) {
            this.ME.jM();
        }
    }

    public void jt() {
        jq.ct().a(this.MH, "startWeChatScan");
        ma.bx(29991);
    }

    public void ju() {
        if (this.MF != null) {
            this.MF.bf();
            this.MF.NI = true;
        }
    }

    public void onCreate(Context context) {
    }

    public int stopPhotoScan() {
        return this.MN.jI();
    }

    public int stopPhotoSimilarCategorise() {
        return this.MN.jJ();
    }
}
