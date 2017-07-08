package tmsdk.fg.module.spacemanager;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class WeChatCacheFiles {
    public static final String GLOBAL_TAG = "WeChatCacheFiles";
    public int mCleanType;
    public String mClearTip;
    public List<WeChatFileModel> mFileModes;
    public String mName;
    public int mScanType;
    public long mTotalSize;

    /* compiled from: Unknown */
    public interface CLEANTYPE {
        public static final int CLEANTYPE_CARE = 2;
        public static final int CLEANTYPE_SUGGEST = 1;
    }

    public WeChatCacheFiles() {
        this.mCleanType = 1;
        this.mFileModes = new ArrayList(1);
    }

    public void deleteFiles() {
        d.e("lzt", "start delete wechat files");
        if (this.mFileModes != null) {
            for (WeChatFileModel weChatFileModel : this.mFileModes) {
                if (weChatFileModel != null) {
                    weChatFileModel.deleteFile();
                    this.mTotalSize -= (long) weChatFileModel.mFileSize;
                }
            }
        }
        d.e("lzt", "start delete wechat files end");
    }

    public String getFilePath(WeChatFileModel weChatFileModel) {
        return weChatFileModel != null ? weChatFileModel.getFilePath() : null;
    }

    public String toString() {
        return this.mName + " :: " + getFilePath((WeChatFileModel) this.mFileModes.get(this.mFileModes.size() - 1));
    }
}
