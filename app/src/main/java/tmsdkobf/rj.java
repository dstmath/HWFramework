package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles;
import tmsdk.fg.module.spacemanager.WeChatFileModel;

/* compiled from: Unknown */
public class rj {
    public int NS;
    public int NT;
    public int NU;
    public int NV;
    public int NW;
    public rl NX;
    public rk NY;
    public int mCleanType;
    public String mClearTip;
    public List<WeChatFileModel> mFileModes;
    public String mName;
    public int mScanType;
    public long mTotalSize;
    public List<String> pN;

    public rj() {
        this.mFileModes = new ArrayList(1);
        this.NT = 0;
        this.NU = 0;
        this.NV = 0;
        this.NW = 0;
    }

    public WeChatCacheFiles jG() {
        WeChatCacheFiles weChatCacheFiles = new WeChatCacheFiles();
        weChatCacheFiles.mName = this.mName;
        weChatCacheFiles.mCleanType = this.mCleanType;
        weChatCacheFiles.mClearTip = this.mClearTip;
        weChatCacheFiles.mScanType = this.mScanType;
        weChatCacheFiles.mTotalSize = this.mTotalSize;
        weChatCacheFiles.mFileModes.addAll(this.mFileModes);
        return weChatCacheFiles;
    }
}
