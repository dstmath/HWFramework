package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.s;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.PhotoScanResult;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult.PhotoSimilarBucketItem;
import tmsdkobf.ry.a;

public class ri extends BaseManagerF {
    sd Pn;
    private sb Po = sb.kq();

    public ri() {
        s.bW(16);
    }

    public static ArrayList<sa> H(List<PhotoItem> list) {
        ArrayList<sa> arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            PhotoItem photoItem = (PhotoItem) list.get(i);
            arrayList.add(new sa(photoItem.mTime, photoItem.mSize, photoItem.mPath, photoItem.mDbId));
        }
        return arrayList;
    }

    public static List<PhotoSimilarResult> I(List<ry> list) {
        List arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ry ryVar = (ry) list.get(i);
            PhotoSimilarResult photoSimilarResult = new PhotoSimilarResult();
            photoSimilarResult.mItemList = new ArrayList();
            photoSimilarResult.mTime = ryVar.mTime;
            photoSimilarResult.mTimeString = ryVar.mTimeString;
            int size2 = ryVar.mItemList.size();
            for (int i2 = 0; i2 < size2; i2++) {
                PhotoSimilarBucketItem photoSimilarBucketItem = new PhotoSimilarBucketItem();
                photoSimilarBucketItem.mId = ((a) ryVar.mItemList.get(i2)).mDbId;
                photoSimilarBucketItem.mPath = ((a) ryVar.mItemList.get(i2)).mPath;
                photoSimilarBucketItem.mFileSize = ((a) ryVar.mItemList.get(i2)).mSize;
                photoSimilarBucketItem.mSelected = ((a) ryVar.mItemList.get(i2)).mSelected;
                photoSimilarResult.mItemList.add(photoSimilarBucketItem);
            }
            arrayList.add(photoSimilarResult);
        }
        return arrayList;
    }

    public static PhotoScanResult a(sb.a aVar) {
        PhotoScanResult photoScanResult = new PhotoScanResult();
        photoScanResult.mInnerPicSize = aVar.mInnerPicSize;
        photoScanResult.mOutPicSize = aVar.mOutPicSize;
        photoScanResult.mPhotoCountAndSize = new Pair(Integer.valueOf(((Integer) aVar.mPhotoCountAndSize.first).intValue()), Long.valueOf(((Long) aVar.mPhotoCountAndSize.second).longValue()));
        photoScanResult.mScreenShotCountAndSize = new Pair(Integer.valueOf(((Integer) aVar.mScreenShotCountAndSize.first).intValue()), Long.valueOf(((Long) aVar.mScreenShotCountAndSize.second).longValue()));
        photoScanResult.mResultList = new ArrayList();
        int size = aVar.mResultList.size();
        for (int i = 0; i < size; i++) {
            sa saVar = (sa) aVar.mResultList.get(i);
            PhotoItem photoItem = new PhotoItem();
            photoItem.mDbId = saVar.mDbId;
            photoItem.mIsOut = saVar.mIsOut;
            photoItem.mIsScreenShot = saVar.mIsScreenShot;
            photoItem.mPath = saVar.mPath;
            photoItem.mSize = saVar.mSize;
            photoItem.mTime = saVar.mTime;
            photoScanResult.mResultList.add(photoItem);
        }
        return photoScanResult;
    }

    public boolean a(ISpaceScanListener iSpaceScanListener) {
        if (this.Pn != null) {
            return false;
        }
        this.Pn = new sd(this);
        if (!this.Pn.a(iSpaceScanListener)) {
            return false;
        }
        kt.saveActionData(29990);
        return true;
    }

    public boolean a(ISpaceScanListener iSpaceScanListener, List<PhotoItem> list) {
        this.Po.c(iSpaceScanListener);
        boolean w = this.Po.w(H(list));
        kt.saveActionData(29993);
        return w;
    }

    public boolean a(ISpaceScanListener iSpaceScanListener, String[] strArr) {
        this.Po.b(iSpaceScanListener);
        boolean b = this.Po.b(strArr);
        kt.saveActionData(29992);
        return b;
    }

    public void bX(int i) {
        switch (i) {
            case 0:
                this.Pn = null;
                return;
            default:
                return;
        }
    }

    public double detectBlur(String str) {
        if (TextUtils.isEmpty(str) || !new File(str).exists()) {
            return -1.0d;
        }
        rx rxVar = new rx();
        return rx.detectBlur(str);
    }

    public void kf() {
        if (this.Pn != null) {
            this.Pn.kv();
        }
    }

    public void kg() {
        if (this.Po != null) {
            this.Po.b(null);
            this.Po.c(null);
        }
    }

    public void onCreate(Context context) {
    }

    public void onDestory() {
        if (this.Pn != null) {
            this.Pn.kv();
        }
    }

    public int stopPhotoScan() {
        return this.Po.kr();
    }

    public int stopPhotoSimilarCategorise() {
        return this.Po.ks();
    }
}
