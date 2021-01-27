package ohos.miscservices.inputmethodability.adapter;

import android.view.inputmethod.EditorInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditorAttribute;

public class EditorInfoAdapter {
    private static final int IME_MASK_FLAG = -2097152;
    private static final int NOT_INCLUDED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "EditorInfoAdapter");

    public static int convertToAutoCapMode(int i) {
        int i2 = i == 4096 ? 2 : 0;
        if (i == 8192) {
            i2 = 4;
        }
        if (i == 16384) {
            return 8;
        }
        return i2;
    }

    public static int convertToCursorCapsMode(int i) {
        int i2 = i == 2 ? 4096 : 0;
        if (i == 4) {
            i2 = 8192;
        }
        if (i == 8) {
            return 16384;
        }
        return i2;
    }

    private static int getEnterKeyType(int i) {
        int i2 = i & 255;
        if (i2 == 3) {
            return 1;
        }
        if (i2 == 2) {
            return 2;
        }
        if (i2 == 4) {
            return 3;
        }
        if (i2 == 6) {
            return 4;
        }
        if (i2 == 5) {
            return 5;
        }
        return i2 == 7 ? 6 : 0;
    }

    private static int getInputOption(int i, int i2) {
        int i3 = i & 15;
        int i4 = i & 16773120;
        int i5 = i2 & IME_MASK_FLAG;
        int i6 = 1;
        int i7 = 0;
        if (i3 == 1) {
            if ((131072 & i4) == 0) {
                i6 = 0;
            }
            if ((i4 & 4096) != 0) {
                i6 |= 2;
            }
            i7 = (i4 & 8192) != 0 ? i6 | 4 : i6;
            if ((i4 & 16384) != 0) {
                i7 |= 8;
            }
        }
        if ((33554432 & i5) != 0) {
            i7 |= 16;
        }
        return (Integer.MIN_VALUE & i5) != 0 ? i7 | 32 : i7;
    }

    private static int getInputPattern(int i) {
        int i2 = i & 15;
        int i3 = i & 4080;
        if (i2 == 1) {
            if (i3 == 16) {
                return 5;
            }
            if (i3 == 32) {
                return 6;
            }
            return i3 == 128 ? 7 : 1;
        } else if (i2 == 2) {
            return 2;
        } else {
            if (i2 == 3) {
                return 3;
            }
            return i2 == 4 ? 4 : 0;
        }
    }

    private EditorInfoAdapter() {
    }

    public static EditorAttribute convertToEditorAttribute(EditorInfo editorInfo) {
        if (editorInfo == null) {
            HiLog.error(TAG, "info is null!", new Object[0]);
            return null;
        }
        int i = editorInfo.inputType;
        int i2 = editorInfo.imeOptions;
        EditorAttribute editorAttribute = new EditorAttribute();
        editorAttribute.setInputOption(getInputOption(i, i2));
        editorAttribute.setEnterKeyType(getEnterKeyType(i2));
        editorAttribute.setInputPattern(getInputPattern(i));
        editorAttribute.setClientPackage(editorInfo.packageName);
        return editorAttribute;
    }
}
