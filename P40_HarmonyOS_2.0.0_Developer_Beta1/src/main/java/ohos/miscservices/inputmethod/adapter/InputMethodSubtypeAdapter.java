package ohos.miscservices.inputmethod.adapter;

import android.view.inputmethod.InputMethodSubtype;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.KeyboardType;

public class InputMethodSubtypeAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodSubtypeAdapter");

    private InputMethodSubtypeAdapter() {
    }

    public static KeyboardType convertToKeyboardType(InputMethodSubtype inputMethodSubtype) {
        if (inputMethodSubtype == null) {
            HiLog.error(TAG, "inputMethodSubtype is null.", new Object[0]);
            return null;
        }
        KeyboardType keyboardType = new KeyboardType();
        keyboardType.setId(inputMethodSubtype.getSubtypeId());
        keyboardType.setLanguage(inputMethodSubtype.getLanguageTag());
        keyboardType.setInputSource(inputMethodSubtype.getMode());
        keyboardType.setCustomizedValue(inputMethodSubtype.getExtraValue());
        keyboardType.setAsciiCapability(inputMethodSubtype.isAsciiCapable());
        keyboardType.setLabelId(inputMethodSubtype.getNameResId());
        keyboardType.setIconId(inputMethodSubtype.getIconResId());
        return keyboardType;
    }

    public static InputMethodSubtype convertToInputMethodSubtype(KeyboardType keyboardType) {
        if (keyboardType == null) {
            HiLog.error(TAG, "type is null.", new Object[0]);
            return null;
        }
        int id = keyboardType.getId();
        String language = keyboardType.getLanguage();
        String inputSource = keyboardType.getInputSource();
        String customizedValue = keyboardType.getCustomizedValue();
        boolean supportsAscii = keyboardType.supportsAscii();
        int iconId = keyboardType.getIconId();
        return new InputMethodSubtype.InputMethodSubtypeBuilder().setSubtypeId(id).setLanguageTag(language).setSubtypeMode(inputSource).setSubtypeExtraValue(customizedValue).setIsAsciiCapable(supportsAscii).setSubtypeIconResId(iconId).setSubtypeNameResId(keyboardType.getLabelId()).setIsAuxiliary(false).setOverridesImplicitlyEnabledSubtype(true).build();
    }
}
