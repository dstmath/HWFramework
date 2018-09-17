package tmsdk.fg.module.spacemanager;

import android.text.TextUtils;
import tmsdk.common.OfflineVideo;
import tmsdk.common.tcc.QFile;

public class FileMedia extends FileInfo {
    public String album;
    public String artist;
    public OfflineVideo mOfflineVideo;
    public String[] mPlayers;
    public String pkg;
    public String title;

    public boolean delFile() {
        boolean delFile = super.delFile();
        if (this.mOfflineVideo != null) {
            if (TextUtils.isEmpty(this.mOfflineVideo.mPath)) {
                return false;
            }
            delFile = delFile || new QFile(this.mOfflineVideo.mPath).deleteSelf();
        }
        return delFile;
    }
}
