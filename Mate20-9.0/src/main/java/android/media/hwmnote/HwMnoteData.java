package android.media.hwmnote;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

class HwMnoteData {
    private static final String TAG = "HwMnoteData";
    private final ByteOrder mByteOrder;
    private final HwMnoteIfdData[] mIfdDatas = new HwMnoteIfdData[3];

    HwMnoteData(ByteOrder order) {
        this.mByteOrder = order;
    }

    /* access modifiers changed from: protected */
    public ByteOrder getByteOrder() {
        return this.mByteOrder;
    }

    /* access modifiers changed from: protected */
    public HwMnoteIfdData getIfdData(int ifdId) {
        if (HwMnoteTag.isValidIfd(ifdId)) {
            return this.mIfdDatas[ifdId];
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void addIfdData(HwMnoteIfdData data) {
        this.mIfdDatas[data.getId()] = data;
    }

    /* access modifiers changed from: protected */
    public HwMnoteIfdData getOrCreateIfdData(int ifdId) {
        HwMnoteIfdData ifdData = this.mIfdDatas[ifdId];
        if (ifdData != null) {
            return ifdData;
        }
        HwMnoteIfdData ifdData2 = new HwMnoteIfdData(ifdId);
        this.mIfdDatas[ifdId] = ifdData2;
        return ifdData2;
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag getTag(short tag, int ifd) {
        HwMnoteIfdData ifdData = this.mIfdDatas[ifd];
        if (ifdData == null) {
            return null;
        }
        return ifdData.getTag(tag);
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag addTag(HwMnoteTag tag) {
        if (tag != null) {
            return addTag(tag, tag.getIfd());
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag addTag(HwMnoteTag tag, int ifdId) {
        if (tag == null || !HwMnoteTag.isValidIfd(ifdId)) {
            return null;
        }
        return getOrCreateIfdData(ifdId).setTag(tag);
    }

    /* access modifiers changed from: protected */
    public void removeTag(short tagId, int ifdId) {
        HwMnoteIfdData ifdData = this.mIfdDatas[ifdId];
        if (ifdData != null) {
            ifdData.removeTag(tagId);
        }
    }

    /* access modifiers changed from: protected */
    public List<HwMnoteTag> getAllTags() {
        ArrayList<HwMnoteTag> ret = new ArrayList<>();
        for (HwMnoteIfdData data : this.mIfdDatas) {
            if (data != null) {
                HwMnoteTag[] tags = data.getAllTags();
                if (tags != null) {
                    for (HwMnoteTag tag : tags) {
                        ret.add(tag);
                    }
                }
            }
        }
        if (ret.size() == 0) {
            return null;
        }
        return ret;
    }
}
