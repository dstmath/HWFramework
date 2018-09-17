package com.android.internal.inputmethod;

import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import java.util.Objects;

public class InputMethodSubtypeHandle {
    private final String mInputMethodId;
    private final int mSubtypeId;

    public InputMethodSubtypeHandle(InputMethodInfo info, InputMethodSubtype subtype) {
        this.mInputMethodId = info.getId();
        if (subtype != null) {
            this.mSubtypeId = subtype.hashCode();
        } else {
            this.mSubtypeId = -1;
        }
    }

    public InputMethodSubtypeHandle(String inputMethodId, int subtypeId) {
        this.mInputMethodId = inputMethodId;
        this.mSubtypeId = subtypeId;
    }

    public String getInputMethodId() {
        return this.mInputMethodId;
    }

    public int getSubtypeId() {
        return this.mSubtypeId;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null || ((o instanceof InputMethodSubtypeHandle) ^ 1) != 0) {
            return false;
        }
        InputMethodSubtypeHandle other = (InputMethodSubtypeHandle) o;
        if (TextUtils.equals(this.mInputMethodId, other.getInputMethodId()) && this.mSubtypeId == other.getSubtypeId()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (Objects.hashCode(this.mInputMethodId) * 31) + this.mSubtypeId;
    }

    public String toString() {
        return "InputMethodSubtypeHandle{mInputMethodId=" + this.mInputMethodId + ", mSubtypeId=" + this.mSubtypeId + "}";
    }
}
