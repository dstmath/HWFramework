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

    public static abstract class FontCallback {
        public abstract void onFontRetrievalFailed(int i);

        public abstract void onFontRetrieved(@NonNull Typeface typeface);

        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public final void callbackSuccessAsync(final Typeface typeface, @Nullable Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
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
                public void run() {
                    FontCallback.this.onFontRetrievalFailed(reason);
                }
            });
        }
    }

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

    public static void getFont(@NonNull Context context, @FontRes int id, @NonNull FontCallback fontCallback, @Nullable Handler handler) throws Resources.NotFoundException {
        Preconditions.checkNotNull(fontCallback);
        if (context.isRestricted()) {
            fontCallback.callbackFailAsync(-4, handler);
            return;
        }
        loadFont(context, id, new TypedValue(), 0, fontCallback, handler, false);
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
        Resources resources = wrapper;
        TypedValue typedValue = value;
        int i2 = id;
        int i3 = style;
        FontCallback fontCallback2 = fontCallback;
        Handler handler2 = handler;
        if (typedValue.string != null) {
            String file = typedValue.string.toString();
            if (!file.startsWith("res/")) {
                if (fontCallback2 != null) {
                    fontCallback2.callbackFailAsync(-3, handler2);
                }
                return null;
            }
            Typeface typeface = TypefaceCompat.findFromCache(resources, i2, i3);
            if (typeface != null) {
                if (fontCallback2 != null) {
                    fontCallback2.callbackSuccessAsync(typeface, handler2);
                }
                return typeface;
            }
            try {
                if (file.toLowerCase().endsWith(".xml")) {
                    try {
                        FontResourcesParserCompat.FamilyResourceEntry familyEntry = FontResourcesParserCompat.parse(resources.getXml(i2), resources);
                        if (familyEntry == null) {
                            try {
                                Log.e(TAG, "Failed to find font-family tag");
                                if (fontCallback2 != null) {
                                    fontCallback2.callbackFailAsync(-3, handler2);
                                }
                                return null;
                            } catch (XmlPullParserException e) {
                                e = e;
                                Context context2 = context;
                                Typeface typeface2 = typeface;
                                i = -3;
                                Log.e(TAG, "Failed to parse xml resource " + file, e);
                                if (fontCallback2 != null) {
                                }
                                return null;
                            } catch (IOException e2) {
                                e = e2;
                                Context context3 = context;
                                Typeface typeface3 = typeface;
                                i = -3;
                                Log.e(TAG, "Failed to read xml resource " + file, e);
                                if (fontCallback2 != null) {
                                }
                                return null;
                            }
                        } else {
                            Typeface typeface4 = typeface;
                            i = -3;
                            try {
                                return TypefaceCompat.createFromResourcesFamilyXml(context, familyEntry, resources, i2, i3, fontCallback2, handler2, isRequestFromLayoutInflator);
                            } catch (XmlPullParserException e3) {
                                e = e3;
                                Context context4 = context;
                                Log.e(TAG, "Failed to parse xml resource " + file, e);
                                if (fontCallback2 != null) {
                                }
                                return null;
                            } catch (IOException e4) {
                                e = e4;
                                Context context5 = context;
                                Log.e(TAG, "Failed to read xml resource " + file, e);
                                if (fontCallback2 != null) {
                                }
                                return null;
                            }
                        }
                    } catch (XmlPullParserException e5) {
                        e = e5;
                        Typeface typeface5 = typeface;
                        i = -3;
                        Context context6 = context;
                        Log.e(TAG, "Failed to parse xml resource " + file, e);
                        if (fontCallback2 != null) {
                        }
                        return null;
                    } catch (IOException e6) {
                        e = e6;
                        Typeface typeface6 = typeface;
                        i = -3;
                        Context context7 = context;
                        Log.e(TAG, "Failed to read xml resource " + file, e);
                        if (fontCallback2 != null) {
                        }
                        return null;
                    }
                } else {
                    Typeface typeface7 = typeface;
                    i = -3;
                    try {
                        Typeface typeface8 = TypefaceCompat.createFromResourcesFontFile(context, resources, i2, file, i3);
                        if (fontCallback2 != null) {
                            if (typeface8 != null) {
                                try {
                                    fontCallback2.callbackSuccessAsync(typeface8, handler2);
                                } catch (XmlPullParserException e7) {
                                    e = e7;
                                    Typeface typeface9 = typeface8;
                                    Log.e(TAG, "Failed to parse xml resource " + file, e);
                                    if (fontCallback2 != null) {
                                    }
                                    return null;
                                } catch (IOException e8) {
                                    e = e8;
                                    Typeface typeface10 = typeface8;
                                    Log.e(TAG, "Failed to read xml resource " + file, e);
                                    if (fontCallback2 != null) {
                                    }
                                    return null;
                                }
                            } else {
                                fontCallback2.callbackFailAsync(-3, handler2);
                            }
                        }
                        return typeface8;
                    } catch (XmlPullParserException e9) {
                        e = e9;
                        Log.e(TAG, "Failed to parse xml resource " + file, e);
                        if (fontCallback2 != null) {
                        }
                        return null;
                    } catch (IOException e10) {
                        e = e10;
                        Log.e(TAG, "Failed to read xml resource " + file, e);
                        if (fontCallback2 != null) {
                        }
                        return null;
                    }
                }
            } catch (XmlPullParserException e11) {
                e = e11;
                Context context8 = context;
                Typeface typeface11 = typeface;
                i = -3;
                Log.e(TAG, "Failed to parse xml resource " + file, e);
                if (fontCallback2 != null) {
                    fontCallback2.callbackFailAsync(i, handler2);
                }
                return null;
            } catch (IOException e12) {
                e = e12;
                Context context9 = context;
                Typeface typeface12 = typeface;
                i = -3;
                Log.e(TAG, "Failed to read xml resource " + file, e);
                if (fontCallback2 != null) {
                }
                return null;
            }
        } else {
            Context context10 = context;
            throw new Resources.NotFoundException("Resource \"" + resources.getResourceName(i2) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + value);
        }
    }

    private ResourcesCompat() {
    }
}
