package tmsdk.fg.module.spacemanager;

import android.content.Context;
import java.util.List;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;
import tmsdkobf.jg;
import tmsdkobf.rd;

/* compiled from: Unknown */
public class SpaceManager extends BaseManagerF {
    public static final int ERROR_CODE_EXPIRED = -4096;
    public static final int ERROR_CODE_OK = 0;
    public static final int ERROR_CODE_PARAM = -1;
    public static final int ERROR_CODE_UNKNOW = -256;
    rd MD;
    private final String TAG;

    public SpaceManager() {
        this.TAG = "SpaceManager";
    }

    public boolean bigFileScan(ISpaceScanListener iSpaceScanListener) {
        if (jg.cl()) {
            return false;
        }
        this.MD.MK = iSpaceScanListener;
        this.MD.jq();
        return true;
    }

    public void onCreate(Context context) {
        this.MD = new rd();
        this.MD.a(this);
        this.MD.onCreate(context);
    }

    public boolean photoScan(ISpaceScanListener iSpaceScanListener) {
        if (jg.cl()) {
            return false;
        }
        this.MD.a(iSpaceScanListener);
        return true;
    }

    public boolean photoSimilarCategorise(ISpaceScanListener iSpaceScanListener, List<PhotoItem> list) {
        if (jg.cl()) {
            return false;
        }
        this.MD.a(iSpaceScanListener, list);
        return true;
    }

    public boolean stopBigFileScan() {
        if (jg.cl()) {
            return false;
        }
        this.MD.jr();
        return true;
    }

    public int stopPhotoScan() {
        return !jg.cl() ? this.MD.stopPhotoScan() : ERROR_CODE_EXPIRED;
    }

    public int stopPhotoSimilarCategorise() {
        return !jg.cl() ? this.MD.stopPhotoSimilarCategorise() : ERROR_CODE_EXPIRED;
    }

    public boolean stopWechatScan() {
        if (jg.cl()) {
            return false;
        }
        this.MD.ju();
        return false;
    }

    public boolean wechatScan(ISpaceScanListener iSpaceScanListener) {
        if (jg.cl()) {
            return false;
        }
        this.MD.ML = iSpaceScanListener;
        this.MD.jt();
        return true;
    }
}
