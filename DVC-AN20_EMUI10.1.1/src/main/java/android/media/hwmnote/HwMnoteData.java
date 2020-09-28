package android.media.hwmnote;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class HwMnoteData {
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
        if (!HwMnoteTag.isValidIfd(ifdId) || ifdId < 0) {
            return null;
        }
        HwMnoteIfdData[] hwMnoteIfdDataArr = this.mIfdDatas;
        if (ifdId < hwMnoteIfdDataArr.length) {
            return hwMnoteIfdDataArr[ifdId];
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void addIfdData(HwMnoteIfdData data) {
        if (data != null && data.getId() >= 0) {
            int id = data.getId();
            HwMnoteIfdData[] hwMnoteIfdDataArr = this.mIfdDatas;
            if (id < hwMnoteIfdDataArr.length) {
                hwMnoteIfdDataArr[data.getId()] = data;
            }
        }
    }

    /* access modifiers changed from: protected */
    public HwMnoteIfdData getOrCreateIfdData(int ifdId) {
        HwMnoteIfdData ifdData = null;
        if (ifdId >= 0) {
            HwMnoteIfdData[] hwMnoteIfdDataArr = this.mIfdDatas;
            if (ifdId < hwMnoteIfdDataArr.length) {
                ifdData = hwMnoteIfdDataArr[ifdId];
            }
        }
        if (ifdData != null) {
            return ifdData;
        }
        HwMnoteIfdData ifdData2 = new HwMnoteIfdData(ifdId);
        this.mIfdDatas[ifdId] = ifdData2;
        return ifdData2;
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag getTag(short tag, int ifd) {
        HwMnoteIfdData ifdData = null;
        if (HwMnoteTag.isValidIfd(ifd) && ifd >= 0) {
            HwMnoteIfdData[] hwMnoteIfdDataArr = this.mIfdDatas;
            if (ifd < hwMnoteIfdDataArr.length) {
                ifdData = hwMnoteIfdDataArr[ifd];
            }
        }
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
        HwMnoteIfdData ifdData = null;
        if (HwMnoteTag.isValidIfd(ifdId) && ifdId >= 0) {
            HwMnoteIfdData[] hwMnoteIfdDataArr = this.mIfdDatas;
            if (ifdId < hwMnoteIfdDataArr.length) {
                ifdData = hwMnoteIfdDataArr[ifdId];
            }
        }
        if (ifdData != null) {
            ifdData.removeTag(tagId);
        }
    }

    /* access modifiers changed from: protected */
    public List<HwMnoteTag> getAllTags() {
        HwMnoteTag[] tags;
        ArrayList<HwMnoteTag> ret = new ArrayList<>();
        HwMnoteIfdData[] hwMnoteIfdDataArr = this.mIfdDatas;
        for (HwMnoteIfdData data : hwMnoteIfdDataArr) {
            if (!(data == null || (tags = data.getAllTags()) == null)) {
                for (HwMnoteTag tag : tags) {
                    ret.add(tag);
                }
            }
        }
        return ret;
    }
}
