package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.EditorInfo;
import ohos.global.icu.text.DateFormat;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.codec.ProfileLevel;
import ohos.miscservices.inputmethod.EditorAttribute;

public class EditorInfoAdapter {
    private static final int DEFAULT_IME_OPTION_FLAG = 0;
    private static final int NOT_INCLUDED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "EditorInfoAdapter");

    public static EditorInfo convertToEditorInfo(EditorAttribute editorAttribute) {
        int inputOption = editorAttribute.getInputOption();
        int enterKeyType = editorAttribute.getEnterKeyType();
        int inputPattern = editorAttribute.getInputPattern();
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.imeOptions = convertToImeOption(enterKeyType, inputOption);
        editorInfo.inputType = convertToInputType(inputPattern, inputOption);
        return editorInfo;
    }

    public static int convertToInputType(int i, int i2) {
        int i3;
        switch (i) {
            case 1:
                i3 = 1;
                break;
            case 2:
                i3 = 2;
                break;
            case 3:
                i3 = 3;
                break;
            case 4:
                i3 = 4;
                break;
            case 5:
                i3 = 17;
                break;
            case 6:
                i3 = 33;
                break;
            case 7:
                i3 = DateFormat.RELATIVE_LONG;
                break;
            default:
                i3 = 0;
                break;
        }
        HiLog.info(TAG, "the inputType is %{public}d", Integer.valueOf(i3));
        return convertToInputTypeOfFlag(i3, i2);
    }

    private static int convertToInputTypeOfFlag(int i, int i2) {
        if ((i2 & 1) != 0) {
            i |= 131072;
        }
        if ((i2 & 2) != 0) {
            i |= 4096;
        }
        if ((i2 & 4) != 0) {
            i |= 8192;
        }
        if ((i2 & 8) != 0) {
            i |= 16384;
        }
        if (i2 == 0) {
            i |= 524288;
        }
        HiLog.info(TAG, "the inputTypeTemp is %{public}d", Integer.valueOf(i));
        return i;
    }

    public static int convertToImeOption(int i, int i2) {
        int i3;
        switch (i) {
            case 1:
                i3 = 3;
                break;
            case 2:
                i3 = 2;
                break;
            case 3:
                i3 = 4;
                break;
            case 4:
                i3 = 6;
                break;
            case 5:
                i3 = 5;
                break;
            case 6:
                i3 = 7;
                break;
            default:
                i3 = 0;
                break;
        }
        HiLog.info(TAG, "the imeOption is %{public}d", Integer.valueOf(i3));
        return convertToImeOptionFlag(i3, i2);
    }

    private static int convertToImeOptionFlag(int i, int i2) {
        if ((i2 & 16) != 0) {
            i |= ProfileLevel.HEVC_HIGH_TIER_LEVEL_6_2;
        }
        if ((i2 & 32) != 0) {
            i |= Integer.MIN_VALUE;
        }
        HiLog.info(TAG, "the imeOptionTemp is %{public}d", Integer.valueOf(i));
        return i;
    }
}
