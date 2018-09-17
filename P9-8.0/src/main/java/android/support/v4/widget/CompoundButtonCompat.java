package android.support.v4.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.CompoundButton;
import java.lang.reflect.Field;

public final class CompoundButtonCompat {
    private static final CompoundButtonCompatBaseImpl IMPL;

    static class CompoundButtonCompatBaseImpl {
        private static final String TAG = "CompoundButtonCompat";
        private static Field sButtonDrawableField;
        private static boolean sButtonDrawableFieldFetched;

        CompoundButtonCompatBaseImpl() {
        }

        public void setButtonTintList(CompoundButton button, ColorStateList tint) {
            if (button instanceof TintableCompoundButton) {
                ((TintableCompoundButton) button).setSupportButtonTintList(tint);
            }
        }

        public ColorStateList getButtonTintList(CompoundButton button) {
            if (button instanceof TintableCompoundButton) {
                return ((TintableCompoundButton) button).getSupportButtonTintList();
            }
            return null;
        }

        public void setButtonTintMode(CompoundButton button, Mode tintMode) {
            if (button instanceof TintableCompoundButton) {
                ((TintableCompoundButton) button).setSupportButtonTintMode(tintMode);
            }
        }

        public Mode getButtonTintMode(CompoundButton button) {
            if (button instanceof TintableCompoundButton) {
                return ((TintableCompoundButton) button).getSupportButtonTintMode();
            }
            return null;
        }

        public Drawable getButtonDrawable(CompoundButton button) {
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

    @RequiresApi(21)
    static class CompoundButtonCompatApi21Impl extends CompoundButtonCompatBaseImpl {
        CompoundButtonCompatApi21Impl() {
        }

        public void setButtonTintList(CompoundButton button, ColorStateList tint) {
            button.setButtonTintList(tint);
        }

        public ColorStateList getButtonTintList(CompoundButton button) {
            return button.getButtonTintList();
        }

        public void setButtonTintMode(CompoundButton button, Mode tintMode) {
            button.setButtonTintMode(tintMode);
        }

        public Mode getButtonTintMode(CompoundButton button) {
            return button.getButtonTintMode();
        }
    }

    @RequiresApi(23)
    static class CompoundButtonCompatApi23Impl extends CompoundButtonCompatApi21Impl {
        CompoundButtonCompatApi23Impl() {
        }

        public Drawable getButtonDrawable(CompoundButton button) {
            return button.getButtonDrawable();
        }
    }

    static {
        if (VERSION.SDK_INT >= 23) {
            IMPL = new CompoundButtonCompatApi23Impl();
        } else if (VERSION.SDK_INT >= 21) {
            IMPL = new CompoundButtonCompatApi21Impl();
        } else {
            IMPL = new CompoundButtonCompatBaseImpl();
        }
    }

    private CompoundButtonCompat() {
    }

    public static void setButtonTintList(@NonNull CompoundButton button, @Nullable ColorStateList tint) {
        IMPL.setButtonTintList(button, tint);
    }

    @Nullable
    public static ColorStateList getButtonTintList(@NonNull CompoundButton button) {
        return IMPL.getButtonTintList(button);
    }

    public static void setButtonTintMode(@NonNull CompoundButton button, @Nullable Mode tintMode) {
        IMPL.setButtonTintMode(button, tintMode);
    }

    @Nullable
    public static Mode getButtonTintMode(@NonNull CompoundButton button) {
        return IMPL.getButtonTintMode(button);
    }

    @Nullable
    public static Drawable getButtonDrawable(@NonNull CompoundButton button) {
        return IMPL.getButtonDrawable(button);
    }
}
