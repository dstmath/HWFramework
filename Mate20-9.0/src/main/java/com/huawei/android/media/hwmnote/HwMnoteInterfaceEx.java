package com.huawei.android.media.hwmnote;

import android.media.hwmnote.HwMnoteInterface;
import android.media.hwmnote.HwMnoteInterfaceUtils;
import java.io.IOException;

public class HwMnoteInterfaceEx {
    public static final int HW_MNOTE_TAG_BURST_NUMBER = HwMnoteInterfaceUtils.HW_MNOTE_TAG_BURST_NUMBER;
    public static final int HW_MNOTE_TAG_CAPTURE_MODE = HwMnoteInterfaceUtils.HW_MNOTE_TAG_CAPTURE_MODE;
    public static final int HW_MNOTE_TAG_FACE_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_CONF;
    public static final int HW_MNOTE_TAG_FACE_COUNT = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_COUNT;
    public static final int HW_MNOTE_TAG_FACE_IFD = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_IFD;
    public static final int HW_MNOTE_TAG_FACE_LEYE_CENTER = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_LEYE_CENTER;
    public static final int HW_MNOTE_TAG_FACE_MOUTH_CENTER = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_MOUTH_CENTER;
    public static final int HW_MNOTE_TAG_FACE_RECT = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_RECT;
    public static final int HW_MNOTE_TAG_FACE_REYE_CENTER = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_REYE_CENTER;
    public static final int HW_MNOTE_TAG_FACE_SMILE_SCORE = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_SMILE_SCORE;
    public static final int HW_MNOTE_TAG_FACE_VERSION = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_VERSION;
    public static final int HW_MNOTE_TAG_FRONT_CAMERA = HwMnoteInterfaceUtils.HW_MNOTE_TAG_FRONT_CAMERA;
    public static final int HW_MNOTE_TAG_SCENE_BEACH_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BEACH_CONF;
    public static final int HW_MNOTE_TAG_SCENE_BLUESKY_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BLUESKY_CONF;
    public static final int HW_MNOTE_TAG_SCENE_FLOWERS_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FLOWERS_CONF;
    public static final int HW_MNOTE_TAG_SCENE_FOOD_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FOOD_CONF;
    public static final int HW_MNOTE_TAG_SCENE_GREENPLANT_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_GREENPLANT_CONF;
    public static final int HW_MNOTE_TAG_SCENE_IFD = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_IFD;
    public static final int HW_MNOTE_TAG_SCENE_NIGHT_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_NIGHT_CONF;
    public static final int HW_MNOTE_TAG_SCENE_SNOW_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_SNOW_CONF;
    public static final int HW_MNOTE_TAG_SCENE_STAGE_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_STAGE_CONF;
    public static final int HW_MNOTE_TAG_SCENE_SUNSET_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_SUNSET_CONF;
    public static final int HW_MNOTE_TAG_SCENE_TEXT_CONF = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_TEXT_CONF;
    public static final int HW_MNOTE_TAG_SCENE_VERSION = HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_VERSION;
    private static final String TAG = "HwMnoteInterfaceEx";
    private HwMnoteInterface mHwMnoteInterface;

    public HwMnoteInterfaceEx() {
        this.mHwMnoteInterface = null;
        this.mHwMnoteInterface = new HwMnoteInterface();
    }

    public void readHwMnote(byte[] mnote) throws IOException {
        this.mHwMnoteInterface.readHwMnote(mnote);
    }

    public byte[] getHwMnote() throws IOException {
        return this.mHwMnoteInterface.getHwMnote();
    }

    public Integer getTagIntValue(int tagId) {
        return this.mHwMnoteInterface.getTagIntValue(tagId);
    }

    public Long getTagLongValue(int tagId) {
        return this.mHwMnoteInterface.getTagLongValue(tagId);
    }

    public byte[] getTagByteValues(int tagId) {
        return this.mHwMnoteInterface.getTagByteValues(tagId);
    }

    public boolean setTagValue(int tagId, Object val) {
        return this.mHwMnoteInterface.setTagValue(tagId, val);
    }
}
