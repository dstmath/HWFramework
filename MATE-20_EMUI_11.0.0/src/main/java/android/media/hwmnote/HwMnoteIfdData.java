package android.media.hwmnote;

import android.util.ArrayMap;

class HwMnoteIfdData {
    private static final int[] SIFDS = {0, 1, 2};
    private static final short TAG_ID_MAKER_NOTE = -28036;
    private final ArrayMap<Short, HwMnoteTag> mHwMnoteTags = new ArrayMap<>();
    private final int mIfdId;
    private int mOffsetToNextIfd = 0;

    HwMnoteIfdData(int ifdId) {
        this.mIfdId = ifdId;
    }

    protected static int[] getIfds() {
        return SIFDS;
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag[] getAllTags() {
        return (HwMnoteTag[]) this.mHwMnoteTags.values().toArray(new HwMnoteTag[this.mHwMnoteTags.size()]);
    }

    /* access modifiers changed from: protected */
    public int getId() {
        return this.mIfdId;
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag getTag(short tagId) {
        return this.mHwMnoteTags.get(Short.valueOf(tagId));
    }

    /* access modifiers changed from: protected */
    public HwMnoteTag setTag(HwMnoteTag tag) {
        if (tag == null) {
            return null;
        }
        tag.setIfd(this.mIfdId);
        return this.mHwMnoteTags.put(Short.valueOf(tag.getTagId()), tag);
    }

    /* access modifiers changed from: protected */
    public boolean checkCollision(short tagId) {
        return this.mHwMnoteTags.get(Short.valueOf(tagId)) != null;
    }

    /* access modifiers changed from: protected */
    public void removeTag(short tagId) {
        this.mHwMnoteTags.remove(Short.valueOf(tagId));
    }

    /* access modifiers changed from: protected */
    public int getTagCount() {
        return this.mHwMnoteTags.size();
    }

    /* access modifiers changed from: protected */
    public void setOffsetToNextIfd(int offset) {
        this.mOffsetToNextIfd = offset;
    }

    /* access modifiers changed from: protected */
    public int getOffsetToNextIfd() {
        return this.mOffsetToNextIfd;
    }
}
