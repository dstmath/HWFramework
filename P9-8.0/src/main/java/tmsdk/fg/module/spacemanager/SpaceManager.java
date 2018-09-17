package tmsdk.fg.module.spacemanager;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.f;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;
import tmsdkobf.ic;
import tmsdkobf.ri;
import tmsdkobf.rk;

public class SpaceManager extends BaseManagerF {
    public static final int CODE_OK = 1;
    public static final int ERROR_CODE_CONCURRENCY = -4097;
    public static final int ERROR_CODE_EXPIRED = -4096;
    public static final int ERROR_CODE_IMG_NOT_FOUND = -257;
    public static final int ERROR_CODE_OK = 0;
    public static final int ERROR_CODE_PARAM = -1;
    public static final int ERROR_CODE_UNKNOW = -256;
    public static final String TAG = "TMSDK_SpaceManager";
    ri Pm;

    public void appendCustomSdcardRoots(String str) {
        rk.appendCustomSdcardRoots(str);
    }

    public void appendWhitePath(String str) {
        rk.dl(str);
    }

    public boolean bigFileScan(ISpaceScanListener iSpaceScanListener) {
        f.f(TAG, "bigFileScan");
        return !ic.bE() ? this.Pm.a(iSpaceScanListener) : false;
    }

    public void clearCustomSdcardRoots() {
        rk.clearCustomSdcardRoots();
    }

    public double detectBlur(String str) {
        return !ic.bE() ? this.Pm.detectBlur(str) : -4096.0d;
    }

    public List<String> getSdcardRoots() {
        return new ArrayList(rk.ke());
    }

    public void onCreate(Context context) {
        this.Pm = new ri();
        this.Pm.onCreate(context);
    }

    public void onDestory() {
        this.Pm.onDestory();
    }

    public int photoScan(ISpaceScanListener iSpaceScanListener) {
        f.f(TAG, "photoScan");
        if (ic.bE()) {
            return ERROR_CODE_EXPIRED;
        }
        if (iSpaceScanListener != null) {
            return !this.Pm.a(iSpaceScanListener, null) ? 1 : ERROR_CODE_CONCURRENCY;
        } else {
            return -1;
        }
    }

    public int photoSimilarCategorise(ISpaceScanListener iSpaceScanListener, List<PhotoItem> list) {
        if (ic.bE()) {
            return ERROR_CODE_EXPIRED;
        }
        if (iSpaceScanListener != null) {
            return !this.Pm.a(iSpaceScanListener, (List) list) ? 1 : ERROR_CODE_CONCURRENCY;
        } else {
            return -1;
        }
    }

    public void setIgnoredSdcardRoots(List<String> list) {
        rk.E(list);
    }

    public void shutDownPhotoISpaceScanListener() {
        this.Pm.kg();
    }

    public boolean stopBigFileScan() {
        if (ic.bE()) {
            return false;
        }
        this.Pm.kf();
        return true;
    }

    public int stopPhotoScan() {
        return !ic.bE() ? this.Pm.stopPhotoScan() : ERROR_CODE_EXPIRED;
    }

    public int stopPhotoSimilarCategorise() {
        return !ic.bE() ? this.Pm.stopPhotoSimilarCategorise() : ERROR_CODE_EXPIRED;
    }
}
