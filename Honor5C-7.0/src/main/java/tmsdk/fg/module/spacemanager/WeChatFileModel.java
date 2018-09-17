package tmsdk.fg.module.spacemanager;

import android.text.TextUtils;
import tmsdk.common.tcc.QFile;

/* compiled from: Unknown */
public class WeChatFileModel implements Comparable<WeChatFileModel> {
    public boolean isDeleted;
    public byte mDay;
    public int mFileSize;
    public long mModifyTime;
    public byte mMonth;
    public String mPrefixPath;
    public String mSubFilePath;
    public short mYear;

    public WeChatFileModel() {
        this.isDeleted = false;
    }

    public int compareTo(WeChatFileModel weChatFileModel) {
        if (this.mYear <= weChatFileModel.mYear) {
            if (this.mYear != weChatFileModel.mYear || this.mMonth <= weChatFileModel.mMonth) {
                if (this.mYear == weChatFileModel.mYear && this.mMonth == weChatFileModel.mMonth) {
                    if (this.mDay <= weChatFileModel.mDay) {
                    }
                }
                return (this.mYear == weChatFileModel.mYear && this.mMonth == weChatFileModel.mMonth && this.mDay == weChatFileModel.mDay) ? 0 : -1;
            }
        }
        return 1;
    }

    public void deleteFile() {
        Object obj = this.mSubFilePath;
        if (this.mPrefixPath != null) {
            obj = this.mPrefixPath + this.mSubFilePath;
        }
        if (!TextUtils.isEmpty(obj)) {
            new QFile(obj).deleteSelf();
            this.isDeleted = true;
        }
    }

    public String getFilePath() {
        if (TextUtils.isEmpty(this.mSubFilePath)) {
            return null;
        }
        String str = this.mSubFilePath;
        if (this.mPrefixPath != null) {
            str = this.mPrefixPath + this.mSubFilePath;
        }
        return str;
    }
}
