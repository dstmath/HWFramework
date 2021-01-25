package android.support.v4.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FontRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.content.res.FontResourcesParserCompat;
import android.support.v4.graphics.TypefaceCompat;
import android.support.v4.util.Preconditions;
import android.util.Log;
import android.util.TypedValue;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public final class ResourcesCompat {
    private static final String TAG = "ResourcesCompat";

    @Nullable
    public static Drawable getDrawable(@NonNull Resources res, @DrawableRes int id, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawable(id, theme);
        }
        return res.getDrawable(id);
    }

    @Nullable
    public static Drawable getDrawableForDensity(@NonNull Resources res, @DrawableRes int id, int density, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawableForDensity(id, density, theme);
        }
        if (Build.VERSION.SDK_INT >= 15) {
            return res.getDrawableForDensity(id, density);
        }
        return res.getDrawable(id);
    }

    @ColorInt
    public static int getColor(@NonNull Resources res, @ColorRes int id, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 23) {
            return res.getColor(id, theme);
        }
        return res.getColor(id);
    }

    @Nullable
    public static ColorStateList getColorStateList(@NonNull Resources res, @ColorRes int id, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 23) {
            return res.getColorStateList(id, theme);
        }
        return res.getColorStateList(id);
    }

    @Nullable
    public static Typeface getFont(@NonNull Context context, @FontRes int id) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, new TypedValue(), 0, null, null, false);
    }

    public static abstract class FontCallback {
        public abstract void onFontRetrievalFailed(int i);

        public abstract void onFontRetrieved(@NonNull Typeface typeface);

        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public final void callbackSuccessAsync(final Typeface typeface, @Nullable Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                /* class android.support.v4.content.res.ResourcesCompat.FontCallback.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    FontCallback.this.onFontRetrieved(typeface);
                }
            });
        }

        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public final void callbackFailAsync(final int reason, @Nullable Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                /* class android.support.v4.content.res.ResourcesCompat.FontCallback.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    FontCallback.this.onFontRetrievalFailed(reason);
                }
            });
        }
    }

    public static void getFont(@NonNull Context context, @FontRes int id, @NonNull FontCallback fontCallback, @Nullable Handler handler) throws Resources.NotFoundException {
        Preconditions.checkNotNull(fontCallback);
        if (context.isRestricted()) {
            fontCallback.callbackFailAsync(-4, handler);
        } else {
            loadFont(context, id, new TypedValue(), 0, fontCallback, handler, false);
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static Typeface getFont(@NonNull Context context, @FontRes int id, TypedValue value, int style, @Nullable FontCallback fontCallback) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, value, style, fontCallback, null, true);
    }

    private static Typeface loadFont(@NonNull Context context, int id, TypedValue value, int style, @Nullable FontCallback fontCallback, @Nullable Handler handler, boolean isRequestFromLayoutInflator) {
        Resources resources = context.getResources();
        resources.getValue(id, value, true);
        Typeface typeface = loadFont(context, resources, value, id, style, fontCallback, handler, isRequestFromLayoutInflator);
        if (typeface != null || fontCallback != null) {
            return typeface;
        }
        throw new Resources.NotFoundException("Font resource ID #0x" + Integer.toHexString(id) + " could not be retrieved.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x00f1  */
    private static Typeface loadFont(@NonNull Context context, Resources wrapper, TypedValue value, int id, int style, @Nullable FontCallback fontCallback, @Nullable Handler handler, boolean isRequestFromLayoutInflator) {
        int i;
        XmlPullParserException e;
        IOException e2;
        if (value.string != null) {
            String file = value.string.toString();
            if (!file.startsWith("res/")) {
                if (fontCallback != null) {
                    fontCallback.callbackFailAsync(-3, handler);
                }
                return null;
            }
            Typeface typeface = TypefaceCompat.findFromCache(wrapper, id, style);
            if (typeface != null) {
                if (fontCallback != null) {
                    fontCallback.callbackSuccessAsync(typeface, handler);
                }
                return typeface;
            }
            try {
                if (file.toLowerCase().endsWith(".xml")) {
                    try {
                        FontResourcesParserCompat.FamilyResourceEntry familyEntry = FontResourcesParserCompat.parse(wrapper.getXml(id), wrapper);
                        if (familyEntry == null) {
                            try {
                                Log.e(TAG, "Failed to find font-family tag");
                                if (fontCallback != null) {
                                    fontCallback.callbackFailAsync(-3, handler);
                                }
                                return null;
                            } catch (XmlPullParserException e3) {
                                e = e3;
                                i = -3;
                                Log.e(TAG, "Failed to parse xml resource " + file, e);
                                if (fontCallback != null) {
                                }
                                return null;
                            } catch (IOException e4) {
                                e2 = e4;
                                i = -3;
                                Log.e(TAG, "Failed to read xml resource " + file, e2);
                                if (fontCallback != null) {
                                }
                                return null;
                            }
                        } else {
                            i = -3;
                            try {
                                return TypefaceCompat.createFromResourcesFamilyXml(context, familyEntry, wrapper, id, style, fontCallback, handler, isRequestFromLayoutInflator);
                            } catch (XmlPullParserException e5) {
                                e = e5;
                                Log.e(TAG, "Failed to parse xml resource " + file, e);
                                if (fontCallback != null) {
                                }
                                return null;
                            } catch (IOException e6) {
                                e2 = e6;
                                Log.e(TAG, "Failed to read xml resource " + file, e2);
                                if (fontCallback != null) {
                                }
                                return null;
                            }
                        }
                    } catch (XmlPullParserException e7) {
                        e = e7;
                        i = -3;
                        Log.e(TAG, "Failed to parse xml resource " + file, e);
                        if (fontCallback != null) {
                        }
                        return null;
                    } catch (IOException e8) {
                        e2 = e8;
                        i = -3;
                        Log.e(TAG, "Failed to read xml resource " + file, e2);
                        if (fontCallback != null) {
                        }
                        return null;
                    }
                } else {
                    i = -3;
                    try {
                        Typeface typeface2 = TypefaceCompat.createFromResourcesFontFile(context, wrapper, id, file, style);
                        if (fontCallback != null) {
                            if (typeface2 != null) {
                                try {
                                    fontCallback.callbackSuccessAsync(typeface2, handler);
                                } catch (XmlPullParserException e9) {
                                    e = e9;
                                    Log.e(TAG, "Failed to parse xml resource " + file, e);
                                    if (fontCallback != null) {
                                    }
                                    return null;
                                } catch (IOException e10) {
                                    e2 = e10;
                                    Log.e(TAG, "Failed to read xml resource " + file, e2);
                                    if (fontCallback != null) {
                                    }
                                    return null;
                                }
                            } else {
                                fontCallback.callbackFailAsync(-3, handler);
                            }
                        }
                        return typeface2;
                    } catch (XmlPullParserException e11) {
                        e = e11;
                        Log.e(TAG, "Failed to parse xml resource " + file, e);
                        if (fontCallback != null) {
                        }
                        return null;
                    } catch (IOException e12) {
                        e2 = e12;
                        Log.e(TAG, "Failed to read xml resource " + file, e2);
                        if (fontCallback != null) {
                        }
                        return null;
                    }
                }
            } catch (XmlPullParserException e13) {
                e = e13;
                i = -3;
                Log.e(TAG, "Failed to parse xml resource " + file, e);
                if (fontCallback != null) {
                    fontCallback.callbackFailAsync(i, handler);
                }
                return null;
            } catch (IOException e14) {
                e2 = e14;
                i = -3;
                Log.e(TAG, "Failed to read xml resource " + file, e2);
                if (fontCallback != null) {
                }
                return null;
            }
        } else {
            throw new Resources.NotFoundException("Resource \"" + wrapper.getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + value);
        }
    }

    private ResourcesCompat() {
    }
}
