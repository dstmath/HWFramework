package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.EditorInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.InputAttribute;

public class EditorInfoAdapter {
    private static final int DEFAULT_IME_OPTION_FLAG = 0;
    private static final int NOT_INCLUDED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "EditorInfoAdapter");

    public static EditorInfo convertToEditorInfo(InputAttribute inputAttribute) {
        int inputOption = inputAttribute.getInputOption();
        int enterKeyType = inputAttribute.getEnterKeyType();
        int inputPattern = inputAttribute.getInputPattern();
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.imeOptions = convertToImeOption(enterKeyType, inputOption);
        editorInfo.inputType = convertToInputType(inputPattern, inputOption);
        return editorInfo;
    }

    public static int convertToInputType(int i, int i2) {
        int i3;
        switch (i) {
            case 1:
            default:
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
                i3 = 129;
                break;
        }
        HiLog.info(TAG, "the inputType is %{public}d", Integer.valueOf(i3));
        return convertToInputTypeOfFlag(i3, i2);
    }

    private static int convertToInputTypeOfFlag(int i, int i2) {
        int i3;
        int i4;
        if ((i2 & 1) != 0) {
            i4 = 131072;
        } else {
            if ((i2 & 2) != 0) {
                i3 = i | 4096;
            } else if ((i2 & 4) != 0) {
                i3 = i | 8192;
            } else if ((i2 & 8) != 0) {
                i3 = i | 16384;
            } else {
                i4 = 524288;
            }
            HiLog.info(TAG, "the inputTypeTemp is %{public}d", Integer.valueOf(i3));
            return i3;
        }
        i3 = i | i4;
        HiLog.info(TAG, "the inputTypeTemp is %{public}d", Integer.valueOf(i3));
        return i3;
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
        int i3;
        int i4;
        if ((i2 & 16) != 0) {
            i4 = 33554432;
        } else if ((i2 & 32) != 0) {
            i4 = Integer.MIN_VALUE;
        } else {
            i3 = i | 0;
            HiLog.info(TAG, "the imeOptionTemp is %{public}d", Integer.valueOf(i3));
            return i3;
        }
        i3 = i | i4;
        HiLog.info(TAG, "the imeOptionTemp is %{public}d", Integer.valueOf(i3));
        return i3;
    }
}
