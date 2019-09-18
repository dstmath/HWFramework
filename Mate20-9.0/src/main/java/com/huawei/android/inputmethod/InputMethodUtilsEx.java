package com.huawei.android.inputmethod;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.inputmethod.InputMethodUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InputMethodUtilsEx {

    public static class InputMethodSettingsEx {
        public InputMethodUtils.InputMethodSettings mSettings;

        public InputMethodSettingsEx(Resources res, ContentResolver resolver, HashMap<String, InputMethodInfo> methodMap, ArrayList<InputMethodInfo> methodList, int userId, boolean copyOnWrite) {
            InputMethodUtils.InputMethodSettings inputMethodSettings = new InputMethodUtils.InputMethodSettings(res, resolver, methodMap, methodList, userId, copyOnWrite);
            this.mSettings = inputMethodSettings;
        }

        public String getSelectedInputMethod() {
            if (this.mSettings != null) {
                return this.mSettings.getSelectedInputMethod();
            }
            return null;
        }

        public int getSelectedInputMethodSubtypeId(String selectedImiId) {
            if (this.mSettings != null) {
                return this.mSettings.getSelectedInputMethodSubtypeId(selectedImiId);
            }
            return -1;
        }

        public HashMap<InputMethodInfo, List<InputMethodSubtype>> getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(Context context) {
            if (this.mSettings != null) {
                return this.mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(context);
            }
            return null;
        }

        public boolean isShowImeWithHardKeyboardEnabled() {
            if (this.mSettings != null) {
                return this.mSettings.isShowImeWithHardKeyboardEnabled();
            }
            return false;
        }

        public void setShowImeWithHardKeyboard(boolean show) {
            if (this.mSettings != null) {
                this.mSettings.setShowImeWithHardKeyboard(show);
            }
        }

        public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(Context context, InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
            if (this.mSettings != null) {
                return this.mSettings.getEnabledInputMethodSubtypeListLocked(context, imi, allowsImplicitlySelectedSubtypes);
            }
            return null;
        }
    }

    public static final int getNotASubtypeId() {
        return -1;
    }

    public static int getSubtypeIdFromHashCode(InputMethodInfo imi, int subtypeHashCode) {
        return InputMethodUtils.getSubtypeIdFromHashCode(imi, subtypeHashCode);
    }

    public static InputMethodSubtype findLastResortApplicableSubtypeLocked(Resources res, List<InputMethodSubtype> subtypes, String mode, String locale, boolean canIgnoreLocaleAsLastResort) {
        return InputMethodUtils.findLastResortApplicableSubtypeLocked(res, subtypes, mode, locale, canIgnoreLocaleAsLastResort);
    }

    public static final String getKeyboardModeType() {
        return "keyboard";
    }

    public static ArrayList<InputMethodSubtype> getSubtypes(InputMethodInfo imi) {
        return InputMethodUtils.getSubtypes(imi);
    }
}
