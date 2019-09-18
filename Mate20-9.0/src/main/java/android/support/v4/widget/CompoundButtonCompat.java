package android.support.v4.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.CompoundButton;
import java.lang.reflect.Field;

public final class CompoundButtonCompat {
    private static final String TAG = "CompoundButtonCompat";
    private static Field sButtonDrawableField;
    private static boolean sButtonDrawableFieldFetched;

    private CompoundButtonCompat() {
    }

    public static void setButtonTintList(@NonNull CompoundButton button, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT >= 21) {
            button.setButtonTintList(tint);
        } else if (button instanceof TintableCompoundButton) {
            ((TintableCompoundButton) button).setSupportButtonTintList(tint);
        }
    }

    @Nullable
    public static ColorStateList getButtonTintList(@NonNull CompoundButton button) {
        if (Build.VERSION.SDK_INT >= 21) {
            return button.getButtonTintList();
        }
        if (button instanceof TintableCompoundButton) {
            return ((TintableCompoundButton) button).getSupportButtonTintList();
        }
        return null;
    }

    public static void setButtonTintMode(@NonNull CompoundButton button, @Nullable PorterDuff.Mode tintMode) {
        if (Build.VERSION.SDK_INT >= 21) {
            button.setButtonTintMode(tintMode);
        } else if (button instanceof TintableCompoundButton) {
            ((TintableCompoundButton) button).setSupportButtonTintMode(tintMode);
        }
    }

    @Nullable
    public static PorterDuff.Mode getButtonTintMode(@NonNull CompoundButton button) {
        if (Build.VERSION.SDK_INT >= 21) {
            return button.getButtonTintMode();
        }
        if (button instanceof TintableCompoundButton) {
            return ((TintableCompoundButton) button).getSupportButtonTintMode();
        }
        return null;
    }

    @Nullable
    public static Drawable getButtonDrawable(@NonNull CompoundButton button) {
        if (Build.VERSION.SDK_INT >= 23) {
            return button.getButtonDrawable();
        }
        if (!sButtonDrawableFieldFetched) {
            try {
                sButtonDrawableField = CompoundButton.class.getDeclaredField("mButtonDrawable");
                sButtonDrawableField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "Failed to retrieve mButtonDrawable field", e);
            }
            sButtonDrawableFieldFetched = true;
        }
        if (sButtonDrawableField != null) {
            try {
                return (Drawable) sButtonDrawableField.get(button);
            } catch (IllegalAccessException e2) {
                Log.i(TAG, "Failed to get button drawable via reflection", e2);
                sButtonDrawableField = null;
            }
        }
        return null;
    }
}
