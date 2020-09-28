package android.media.hwmnote;

import android.util.Log;
import android.util.SparseIntArray;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashSet;

public class HwMnoteInterfaceImpl implements IHwMnoteInterface {
    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    public static final int DEFINITION_NULL = 0;
    public static final int IFD_NULL = -1;
    private static final String NULL_ARGUMENT_STRING = "Argument is null";
    private static final int SHIFT_BYTE_FLAG = 24;
    private static final int SHIFT_BYTE_VALUE = 16;
    private static final String TAG = "HwMnoteInterfaceImpl";
    public static final int TAG_NULL = -1;
    private static final int UNSIGNED_BYTE = 255;
    private static final int UNSIGNED_WORD = 65535;
    private static HashSet<Short> sOffsetTags = new HashSet<>(2);
    private HwMnoteData mData = new HwMnoteData(DEFAULT_BYTE_ORDER);
    private SparseIntArray mHwMnoteTagInfo = null;

    static {
        sOffsetTags.add(Short.valueOf(getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD)));
    }

    public HwMnoteData getmData() {
        return this.mData;
    }

    public void setmData(HwMnoteData mData2) {
        this.mData = mData2;
    }

    public static short getTrueTagKey(int tag) {
        return (short) tag;
    }

    public static int getTrueIfd(int tag) {
        return tag >>> 16;
    }

    @Override // android.media.hwmnote.IHwMnoteInterface
    public void readHwMnote(byte[] mnote) throws IOException {
        InputStream inStream = new ByteArrayInputStream(mnote);
        try {
            this.mData = new HwMnoteReader(this).read(inStream);
            try {
                inStream.close();
            } catch (IOException e) {
                Log.e(TAG, "close stream IOException");
            }
        } catch (IOException | IllegalArgumentException e2) {
            throw new IOException(e2.getMessage());
        } catch (Throwable th) {
            this.mData = null;
            try {
                inStream.close();
            } catch (IOException e3) {
                Log.e(TAG, "close stream IOException");
            }
            throw th;
        }
    }

    @Override // android.media.hwmnote.IHwMnoteInterface
    public byte[] getHwMnote() throws IOException {
        HwMnoteOutputStream mos = new HwMnoteOutputStream();
        mos.setData(this.mData);
        return mos.getHwMnoteBuffer();
    }

    public void clearHwMnote() {
        this.mData = new HwMnoteData(DEFAULT_BYTE_ORDER);
    }

    public HwMnoteTag getTag(int tagId, int ifdId) {
        if (!HwMnoteTag.isValidIfd(ifdId)) {
            return null;
        }
        return this.mData.getTag(getTrueTagKey(tagId), ifdId);
    }

    public Object getTagValue(int tagId) {
        HwMnoteTag tag = getTag(tagId, getDefinedTagDefaultIfd(tagId));
        if (tag == null) {
            return null;
        }
        return tag.getValue();
    }

    @Override // android.media.hwmnote.IHwMnoteInterface
    public Long getTagLongValue(int tagId) {
        long[] longTagId = getTagLongValues(tagId);
        if (longTagId == null || longTagId.length <= 0) {
            return null;
        }
        return Long.valueOf(longTagId[0]);
    }

    @Override // android.media.hwmnote.IHwMnoteInterface
    public Integer getTagIntValue(int tagId) {
        int[] longTagId = getTagIntValues(tagId);
        if (longTagId == null || longTagId.length <= 0) {
            return null;
        }
        return Integer.valueOf(longTagId[0]);
    }

    public long[] getTagLongValues(int tagId) {
        HwMnoteTag tag = getTag(tagId, getDefinedTagDefaultIfd(tagId));
        if (tag == null) {
            return new long[0];
        }
        return tag.getValueAsLongs();
    }

    public int[] getTagIntValues(int tagId) {
        HwMnoteTag tag = getTag(tagId, getDefinedTagDefaultIfd(tagId));
        if (tag == null) {
            return new int[0];
        }
        return tag.getValueAsInts();
    }

    @Override // android.media.hwmnote.IHwMnoteInterface
    public byte[] getTagByteValues(int tagId) {
        HwMnoteTag tag = getTag(tagId, getDefinedTagDefaultIfd(tagId));
        if (tag == null) {
            return new byte[0];
        }
        return tag.getValueAsBytes();
    }

    public int getDefinedTagDefaultIfd(int tagId) {
        if (getTagInfo().get(tagId) == 0) {
            return -1;
        }
        return getTrueIfd(tagId);
    }

    protected static boolean isOffsetTag(short tag) {
        return sOffsetTags.contains(Short.valueOf(tag));
    }

    private HwMnoteTag buildTag(int tagId, Object val) {
        int ifdId = getTrueIfd(tagId);
        int info = getTagInfo().get(tagId);
        if (info == 0 || val == null) {
            return null;
        }
        short type = getTypeFromInfo(info);
        int definedCount = getComponentCountFromInfo(info);
        boolean hasDefinedCount = definedCount != 0;
        if (!isIfdAllowed(info, ifdId)) {
            return null;
        }
        HwMnoteTag tag = new HwMnoteTag(getTrueTagKey(tagId), type, definedCount, ifdId, hasDefinedCount);
        if (!tag.setValue(val)) {
            return null;
        }
        return tag;
    }

    @Override // android.media.hwmnote.IHwMnoteInterface
    public boolean setTagValue(int tagId, Object val) {
        HwMnoteTag tag = getTag(tagId, getDefinedTagDefaultIfd(tagId));
        if (tag != null) {
            return tag.setValue(val);
        }
        HwMnoteTag tag2 = buildTag(tagId, val);
        if (tag2 == null) {
            return false;
        }
        this.mData.addTag(tag2);
        return true;
    }

    public void deleteTag(int tagId) {
        this.mData.removeTag(getTrueTagKey(tagId), getDefinedTagDefaultIfd(tagId));
    }

    /* access modifiers changed from: protected */
    public SparseIntArray getTagInfo() {
        if (this.mHwMnoteTagInfo == null) {
            this.mHwMnoteTagInfo = new SparseIntArray();
            initTagInfo();
        }
        return this.mHwMnoteTagInfo;
    }

    private void initTagInfo() {
        int ifdValue = (getFlagsFromAllowedIfds(new int[]{0}) << SHIFT_BYTE_FLAG) | 262144 | 1;
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD, ifdValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD, ifdValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_CAPTURE_MODE, ifdValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_BURST_NUMBER, ifdValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FRONT_CAMERA, ifdValue);
        int sceneValue = (getFlagsFromAllowedIfds(new int[]{1}) << SHIFT_BYTE_FLAG) | 262144 | 1;
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_VERSION, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FOOD_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_STAGE_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BLUESKY_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_GREENPLANT_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BEACH_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_SNOW_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_SUNSET_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FLOWERS_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_NIGHT_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_TEXT_CONF, sceneValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_MASTER_AI_MODE, sceneValue);
        int faceFlags = getFlagsFromAllowedIfds(new int[]{2}) << SHIFT_BYTE_FLAG;
        int faceValue = 1 | 262144 | faceFlags;
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_VERSION, faceValue);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_COUNT, faceValue);
        int faceValueUndefined = 458752 | faceFlags;
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_CONF, faceValueUndefined);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_SMILE_SCORE, faceValueUndefined);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_RECT, faceValueUndefined);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_LEYE_CENTER, faceValueUndefined);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_REYE_CENTER, faceValueUndefined);
        this.mHwMnoteTagInfo.put(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_MOUTH_CENTER, faceValueUndefined);
    }

    protected static int getAllowedIfdFlagsFromInfo(int info) {
        return info >>> SHIFT_BYTE_FLAG;
    }

    protected static boolean isIfdAllowed(int info, int ifd) {
        int[] ifds = HwMnoteIfdData.getIfds();
        int ifdFlags = getAllowedIfdFlagsFromInfo(info);
        int i = 0;
        while (true) {
            boolean isAllowed = false;
            if (i >= ifds.length) {
                return false;
            }
            if (((ifdFlags >> i) & 1) == 1) {
                isAllowed = true;
            }
            if (ifd == ifds[i] && isAllowed) {
                return true;
            }
            i++;
        }
    }

    protected static int getFlagsFromAllowedIfds(int[] allowedIfds) {
        if (allowedIfds == null || allowedIfds.length == 0) {
            return 0;
        }
        int flags = 0;
        int[] ifds = HwMnoteIfdData.getIfds();
        for (int ifdNum = 0; ifdNum < 3; ifdNum++) {
            int length = allowedIfds.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (ifds[ifdNum] == allowedIfds[i]) {
                    flags |= 1 << ifdNum;
                    break;
                }
                i++;
            }
        }
        return flags;
    }

    protected static short getTypeFromInfo(int info) {
        return (short) ((info >> 16) & UNSIGNED_BYTE);
    }

    protected static int getComponentCountFromInfo(int info) {
        return 65535 & info;
    }
}
