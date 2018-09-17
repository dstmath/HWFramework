package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Method;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class DrawableCompat {
    static final DrawableCompatBaseImpl IMPL;

    static class DrawableCompatBaseImpl {
        DrawableCompatBaseImpl() {
        }

        public void jumpToCurrentState(Drawable drawable) {
            drawable.jumpToCurrentState();
        }

        public void setAutoMirrored(Drawable drawable, boolean mirrored) {
        }

        public boolean isAutoMirrored(Drawable drawable) {
            return false;
        }

        public void setHotspot(Drawable drawable, float x, float y) {
        }

        public void setHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
        }

        public void setTint(Drawable drawable, int tint) {
            if (drawable instanceof TintAwareDrawable) {
                ((TintAwareDrawable) drawable).setTint(tint);
            }
        }

        public void setTintList(Drawable drawable, ColorStateList tint) {
            if (drawable instanceof TintAwareDrawable) {
                ((TintAwareDrawable) drawable).setTintList(tint);
            }
        }

        public void setTintMode(Drawable drawable, Mode tintMode) {
            if (drawable instanceof TintAwareDrawable) {
                ((TintAwareDrawable) drawable).setTintMode(tintMode);
            }
        }

        public Drawable wrap(Drawable drawable) {
            if (drawable instanceof TintAwareDrawable) {
                return drawable;
            }
            return new DrawableWrapperApi14(drawable);
        }

        public boolean setLayoutDirection(Drawable drawable, int layoutDirection) {
            return false;
        }

        public int getLayoutDirection(Drawable drawable) {
            return 0;
        }

        public int getAlpha(Drawable drawable) {
            return 0;
        }

        public void applyTheme(Drawable drawable, Theme t) {
        }

        public boolean canApplyTheme(Drawable drawable) {
            return false;
        }

        public ColorFilter getColorFilter(Drawable drawable) {
            return null;
        }

        public void clearColorFilter(Drawable drawable) {
            drawable.clearColorFilter();
        }

        public void inflate(Drawable drawable, Resources res, XmlPullParser parser, AttributeSet attrs, Theme t) throws IOException, XmlPullParserException {
            drawable.inflate(res, parser, attrs);
        }
    }

    @RequiresApi(17)
    static class DrawableCompatApi17Impl extends DrawableCompatBaseImpl {
        private static final String TAG = "DrawableCompatApi17";
        private static Method sGetLayoutDirectionMethod;
        private static boolean sGetLayoutDirectionMethodFetched;
        private static Method sSetLayoutDirectionMethod;
        private static boolean sSetLayoutDirectionMethodFetched;

        DrawableCompatApi17Impl() {
        }

        public boolean setLayoutDirection(Drawable drawable, int layoutDirection) {
            if (!sSetLayoutDirectionMethodFetched) {
                try {
                    sSetLayoutDirectionMethod = Drawable.class.getDeclaredMethod("setLayoutDirection", new Class[]{Integer.TYPE});
                    sSetLayoutDirectionMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    Log.i(TAG, "Failed to retrieve setLayoutDirection(int) method", e);
                }
                sSetLayoutDirectionMethodFetched = true;
            }
            if (sSetLayoutDirectionMethod != null) {
                try {
                    sSetLayoutDirectionMethod.invoke(drawable, new Object[]{Integer.valueOf(layoutDirection)});
                    return true;
                } catch (Exception e2) {
                    Log.i(TAG, "Failed to invoke setLayoutDirection(int) via reflection", e2);
                    sSetLayoutDirectionMethod = null;
                }
            }
            return false;
        }

        public int getLayoutDirection(Drawable drawable) {
            if (!sGetLayoutDirectionMethodFetched) {
                try {
                    sGetLayoutDirectionMethod = Drawable.class.getDeclaredMethod("getLayoutDirection", new Class[0]);
                    sGetLayoutDirectionMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    Log.i(TAG, "Failed to retrieve getLayoutDirection() method", e);
                }
                sGetLayoutDirectionMethodFetched = true;
            }
            if (sGetLayoutDirectionMethod != null) {
                try {
                    return ((Integer) sGetLayoutDirectionMethod.invoke(drawable, new Object[0])).intValue();
                } catch (Exception e2) {
                    Log.i(TAG, "Failed to invoke getLayoutDirection() via reflection", e2);
                    sGetLayoutDirectionMethod = null;
                }
            }
            return 0;
        }
    }

    @RequiresApi(19)
    static class DrawableCompatApi19Impl extends DrawableCompatApi17Impl {
        DrawableCompatApi19Impl() {
        }

        public void setAutoMirrored(Drawable drawable, boolean mirrored) {
            drawable.setAutoMirrored(mirrored);
        }

        public boolean isAutoMirrored(Drawable drawable) {
            return drawable.isAutoMirrored();
        }

        public Drawable wrap(Drawable drawable) {
            if (drawable instanceof TintAwareDrawable) {
                return drawable;
            }
            return new DrawableWrapperApi19(drawable);
        }

        public int getAlpha(Drawable drawable) {
            return drawable.getAlpha();
        }
    }

    @RequiresApi(21)
    static class DrawableCompatApi21Impl extends DrawableCompatApi19Impl {
        DrawableCompatApi21Impl() {
        }

        public void setHotspot(Drawable drawable, float x, float y) {
            drawable.setHotspot(x, y);
        }

        public void setHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
            drawable.setHotspotBounds(left, top, right, bottom);
        }

        public void setTint(Drawable drawable, int tint) {
            drawable.setTint(tint);
        }

        public void setTintList(Drawable drawable, ColorStateList tint) {
            drawable.setTintList(tint);
        }

        public void setTintMode(Drawable drawable, Mode tintMode) {
            drawable.setTintMode(tintMode);
        }

        public Drawable wrap(Drawable drawable) {
            if (drawable instanceof TintAwareDrawable) {
                return drawable;
            }
            return new DrawableWrapperApi21(drawable);
        }

        public void applyTheme(Drawable drawable, Theme t) {
            drawable.applyTheme(t);
        }

        public boolean canApplyTheme(Drawable drawable) {
            return drawable.canApplyTheme();
        }

        public ColorFilter getColorFilter(Drawable drawable) {
            return drawable.getColorFilter();
        }

        public void clearColorFilter(Drawable drawable) {
            drawable.clearColorFilter();
            if (drawable instanceof InsetDrawable) {
                clearColorFilter(((InsetDrawable) drawable).getDrawable());
            } else if (drawable instanceof DrawableWrapper) {
                clearColorFilter(((DrawableWrapper) drawable).getWrappedDrawable());
            } else if (drawable instanceof DrawableContainer) {
                DrawableContainerState state = (DrawableContainerState) ((DrawableContainer) drawable).getConstantState();
                if (state != null) {
                    int count = state.getChildCount();
                    for (int i = 0; i < count; i++) {
                        Drawable child = state.getChild(i);
                        if (child != null) {
                            clearColorFilter(child);
                        }
                    }
                }
            }
        }

        public void inflate(Drawable drawable, Resources res, XmlPullParser parser, AttributeSet attrs, Theme t) throws IOException, XmlPullParserException {
            drawable.inflate(res, parser, attrs, t);
        }
    }

    @RequiresApi(23)
    static class DrawableCompatApi23Impl extends DrawableCompatApi21Impl {
        DrawableCompatApi23Impl() {
        }

        public boolean setLayoutDirection(Drawable drawable, int layoutDirection) {
            return drawable.setLayoutDirection(layoutDirection);
        }

        public int getLayoutDirection(Drawable drawable) {
            return drawable.getLayoutDirection();
        }

        public Drawable wrap(Drawable drawable) {
            return drawable;
        }

        public void clearColorFilter(Drawable drawable) {
            drawable.clearColorFilter();
        }
    }

    static {
        if (VERSION.SDK_INT >= 23) {
            IMPL = new DrawableCompatApi23Impl();
        } else if (VERSION.SDK_INT >= 21) {
            IMPL = new DrawableCompatApi21Impl();
        } else if (VERSION.SDK_INT >= 19) {
            IMPL = new DrawableCompatApi19Impl();
        } else if (VERSION.SDK_INT >= 17) {
            IMPL = new DrawableCompatApi17Impl();
        } else {
            IMPL = new DrawableCompatBaseImpl();
        }
    }

    public static void jumpToCurrentState(@NonNull Drawable drawable) {
        IMPL.jumpToCurrentState(drawable);
    }

    public static void setAutoMirrored(@NonNull Drawable drawable, boolean mirrored) {
        IMPL.setAutoMirrored(drawable, mirrored);
    }

    public static boolean isAutoMirrored(@NonNull Drawable drawable) {
        return IMPL.isAutoMirrored(drawable);
    }

    public static void setHotspot(@NonNull Drawable drawable, float x, float y) {
        IMPL.setHotspot(drawable, x, y);
    }

    public static void setHotspotBounds(@NonNull Drawable drawable, int left, int top, int right, int bottom) {
        IMPL.setHotspotBounds(drawable, left, top, right, bottom);
    }

    public static void setTint(@NonNull Drawable drawable, @ColorInt int tint) {
        IMPL.setTint(drawable, tint);
    }

    public static void setTintList(@NonNull Drawable drawable, @Nullable ColorStateList tint) {
        IMPL.setTintList(drawable, tint);
    }

    public static void setTintMode(@NonNull Drawable drawable, @Nullable Mode tintMode) {
        IMPL.setTintMode(drawable, tintMode);
    }

    public static int getAlpha(@NonNull Drawable drawable) {
        return IMPL.getAlpha(drawable);
    }

    public static void applyTheme(@NonNull Drawable drawable, @NonNull Theme t) {
        IMPL.applyTheme(drawable, t);
    }

    public static boolean canApplyTheme(@NonNull Drawable drawable) {
        return IMPL.canApplyTheme(drawable);
    }

    public static ColorFilter getColorFilter(@NonNull Drawable drawable) {
        return IMPL.getColorFilter(drawable);
    }

    public static void clearColorFilter(@NonNull Drawable drawable) {
        IMPL.clearColorFilter(drawable);
    }

    public static void inflate(@NonNull Drawable drawable, @NonNull Resources res, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Theme theme) throws XmlPullParserException, IOException {
        IMPL.inflate(drawable, res, parser, attrs, theme);
    }

    public static Drawable wrap(@NonNull Drawable drawable) {
        return IMPL.wrap(drawable);
    }

    public static <T extends Drawable> T unwrap(@NonNull Drawable drawable) {
        if (drawable instanceof DrawableWrapper) {
            return ((DrawableWrapper) drawable).getWrappedDrawable();
        }
        return drawable;
    }

    public static boolean setLayoutDirection(@NonNull Drawable drawable, int layoutDirection) {
        return IMPL.setLayoutDirection(drawable, layoutDirection);
    }

    public static int getLayoutDirection(@NonNull Drawable drawable) {
        return IMPL.getLayoutDirection(drawable);
    }

    private DrawableCompat() {
    }
}
