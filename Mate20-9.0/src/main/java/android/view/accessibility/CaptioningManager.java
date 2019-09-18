package android.view.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.text.style.QuoteSpan;
import android.view.InputDevice;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class CaptioningManager {
    private static final int DEFAULT_ENABLED = 0;
    private static final float DEFAULT_FONT_SCALE = 1.0f;
    private static final int DEFAULT_PRESET = 0;
    private final ContentObserver mContentObserver;
    private final ContentResolver mContentResolver;
    private final ArrayList<CaptioningChangeListener> mListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Runnable mStyleChangedRunnable = new Runnable() {
        public void run() {
            CaptioningManager.this.notifyUserStyleChanged();
        }
    };

    public static final class CaptionStyle {
        private static final CaptionStyle BLACK_ON_WHITE;
        private static final int COLOR_NONE_OPAQUE = 255;
        public static final int COLOR_UNSPECIFIED = 16777215;
        public static final CaptionStyle DEFAULT = WHITE_ON_BLACK;
        private static final CaptionStyle DEFAULT_CUSTOM = WHITE_ON_BLACK;
        public static final int EDGE_TYPE_DEPRESSED = 4;
        public static final int EDGE_TYPE_DROP_SHADOW = 2;
        public static final int EDGE_TYPE_NONE = 0;
        public static final int EDGE_TYPE_OUTLINE = 1;
        public static final int EDGE_TYPE_RAISED = 3;
        public static final int EDGE_TYPE_UNSPECIFIED = -1;
        public static final CaptionStyle[] PRESETS = {WHITE_ON_BLACK, BLACK_ON_WHITE, YELLOW_ON_BLACK, YELLOW_ON_BLUE, UNSPECIFIED};
        public static final int PRESET_CUSTOM = -1;
        private static final CaptionStyle UNSPECIFIED;
        private static final CaptionStyle WHITE_ON_BLACK;
        private static final CaptionStyle YELLOW_ON_BLACK;
        private static final CaptionStyle YELLOW_ON_BLUE;
        public final int backgroundColor;
        public final int edgeColor;
        public final int edgeType;
        public final int foregroundColor;
        private final boolean mHasBackgroundColor;
        private final boolean mHasEdgeColor;
        private final boolean mHasEdgeType;
        private final boolean mHasForegroundColor;
        private final boolean mHasWindowColor;
        private Typeface mParsedTypeface;
        public final String mRawTypeface;
        public final int windowColor;

        private CaptionStyle(int foregroundColor2, int backgroundColor2, int edgeType2, int edgeColor2, int windowColor2, String rawTypeface) {
            this.mHasForegroundColor = hasColor(foregroundColor2);
            this.mHasBackgroundColor = hasColor(backgroundColor2);
            int i = 0;
            int i2 = -1;
            this.mHasEdgeType = edgeType2 != -1;
            this.mHasEdgeColor = hasColor(edgeColor2);
            this.mHasWindowColor = hasColor(windowColor2);
            this.foregroundColor = this.mHasForegroundColor ? foregroundColor2 : i2;
            int i3 = -16777216;
            this.backgroundColor = this.mHasBackgroundColor ? backgroundColor2 : -16777216;
            this.edgeType = this.mHasEdgeType ? edgeType2 : i;
            this.edgeColor = this.mHasEdgeColor ? edgeColor2 : i3;
            this.windowColor = this.mHasWindowColor ? windowColor2 : 255;
            this.mRawTypeface = rawTypeface;
        }

        public static boolean hasColor(int packedColor) {
            return (packedColor >>> 24) != 0 || (16776960 & packedColor) == 0;
        }

        public CaptionStyle applyStyle(CaptionStyle overlay) {
            CaptionStyle captionStyle = new CaptionStyle(overlay.hasForegroundColor() ? overlay.foregroundColor : this.foregroundColor, overlay.hasBackgroundColor() ? overlay.backgroundColor : this.backgroundColor, overlay.hasEdgeType() ? overlay.edgeType : this.edgeType, overlay.hasEdgeColor() ? overlay.edgeColor : this.edgeColor, overlay.hasWindowColor() ? overlay.windowColor : this.windowColor, overlay.mRawTypeface != null ? overlay.mRawTypeface : this.mRawTypeface);
            return captionStyle;
        }

        public boolean hasBackgroundColor() {
            return this.mHasBackgroundColor;
        }

        public boolean hasForegroundColor() {
            return this.mHasForegroundColor;
        }

        public boolean hasEdgeType() {
            return this.mHasEdgeType;
        }

        public boolean hasEdgeColor() {
            return this.mHasEdgeColor;
        }

        public boolean hasWindowColor() {
            return this.mHasWindowColor;
        }

        public Typeface getTypeface() {
            if (this.mParsedTypeface == null && !TextUtils.isEmpty(this.mRawTypeface)) {
                this.mParsedTypeface = Typeface.create(this.mRawTypeface, 0);
            }
            return this.mParsedTypeface;
        }

        public static CaptionStyle getCustomStyle(ContentResolver cr) {
            CaptionStyle defStyle = DEFAULT_CUSTOM;
            int foregroundColor2 = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR, defStyle.foregroundColor);
            int backgroundColor2 = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR, defStyle.backgroundColor);
            int edgeType2 = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_TYPE, defStyle.edgeType);
            int edgeColor2 = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_COLOR, defStyle.edgeColor);
            int windowColor2 = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_WINDOW_COLOR, defStyle.windowColor);
            String rawTypeface = Settings.Secure.getString(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_TYPEFACE);
            if (rawTypeface == null) {
                rawTypeface = defStyle.mRawTypeface;
            }
            CaptionStyle captionStyle = new CaptionStyle(foregroundColor2, backgroundColor2, edgeType2, edgeColor2, windowColor2, rawTypeface);
            return captionStyle;
        }

        static {
            CaptionStyle captionStyle = new CaptionStyle(-1, -16777216, 0, -16777216, 255, null);
            WHITE_ON_BLACK = captionStyle;
            CaptionStyle captionStyle2 = new CaptionStyle(-16777216, -1, 0, -16777216, 255, null);
            BLACK_ON_WHITE = captionStyle2;
            CaptionStyle captionStyle3 = new CaptionStyle(InputDevice.SOURCE_ANY, -16777216, 0, -16777216, 255, null);
            YELLOW_ON_BLACK = captionStyle3;
            CaptionStyle captionStyle4 = new CaptionStyle(InputDevice.SOURCE_ANY, QuoteSpan.STANDARD_COLOR, 0, -16777216, 255, null);
            YELLOW_ON_BLUE = captionStyle4;
            CaptionStyle captionStyle5 = new CaptionStyle(16777215, 16777215, -1, 16777215, 16777215, null);
            UNSPECIFIED = captionStyle5;
        }
    }

    public static abstract class CaptioningChangeListener {
        public void onEnabledChanged(boolean enabled) {
        }

        public void onUserStyleChanged(CaptionStyle userStyle) {
        }

        public void onLocaleChanged(Locale locale) {
        }

        public void onFontScaleChanged(float fontScale) {
        }
    }

    private class MyContentObserver extends ContentObserver {
        private final Handler mHandler;

        public MyContentObserver(Handler handler) {
            super(handler);
            this.mHandler = handler;
        }

        public void onChange(boolean selfChange, Uri uri) {
            String uriPath = uri.getPath();
            String name = uriPath.substring(uriPath.lastIndexOf(47) + 1);
            if (Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED.equals(name)) {
                CaptioningManager.this.notifyEnabledChanged();
            } else if (Settings.Secure.ACCESSIBILITY_CAPTIONING_LOCALE.equals(name)) {
                CaptioningManager.this.notifyLocaleChanged();
            } else if (Settings.Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE.equals(name)) {
                CaptioningManager.this.notifyFontScaleChanged();
            } else {
                this.mHandler.removeCallbacks(CaptioningManager.this.mStyleChangedRunnable);
                this.mHandler.post(CaptioningManager.this.mStyleChangedRunnable);
            }
        }
    }

    public CaptioningManager(Context context) {
        this.mContentResolver = context.getContentResolver();
        this.mContentObserver = new MyContentObserver(new Handler(context.getMainLooper()));
    }

    public final boolean isEnabled() {
        return Settings.Secure.getInt(this.mContentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED, 0) == 1;
    }

    public final String getRawLocale() {
        return Settings.Secure.getString(this.mContentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_LOCALE);
    }

    public final Locale getLocale() {
        String rawLocale = getRawLocale();
        if (!TextUtils.isEmpty(rawLocale)) {
            String[] splitLocale = rawLocale.split(Session.SESSION_SEPARATION_CHAR_CHILD);
            switch (splitLocale.length) {
                case 1:
                    return new Locale(splitLocale[0]);
                case 2:
                    return new Locale(splitLocale[0], splitLocale[1]);
                case 3:
                    return new Locale(splitLocale[0], splitLocale[1], splitLocale[2]);
            }
        }
        return null;
    }

    public final float getFontScale() {
        return Settings.Secure.getFloat(this.mContentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE, 1.0f);
    }

    public int getRawUserStyle() {
        return Settings.Secure.getInt(this.mContentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_PRESET, 0);
    }

    public CaptionStyle getUserStyle() {
        int preset = getRawUserStyle();
        if (preset == -1) {
            return CaptionStyle.getCustomStyle(this.mContentResolver);
        }
        return CaptionStyle.PRESETS[preset];
    }

    public void addCaptioningChangeListener(CaptioningChangeListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.isEmpty()) {
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_WINDOW_COLOR);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_TYPE);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_COLOR);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_TYPEFACE);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_LOCALE);
                registerObserver(Settings.Secure.ACCESSIBILITY_CAPTIONING_PRESET);
            }
            this.mListeners.add(listener);
        }
    }

    private void registerObserver(String key) {
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(key), false, this.mContentObserver);
    }

    public void removeCaptioningChangeListener(CaptioningChangeListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
            if (this.mListeners.isEmpty()) {
                this.mContentResolver.unregisterContentObserver(this.mContentObserver);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyEnabledChanged() {
        boolean enabled = isEnabled();
        synchronized (this.mListeners) {
            Iterator<CaptioningChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onEnabledChanged(enabled);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyUserStyleChanged() {
        CaptionStyle userStyle = getUserStyle();
        synchronized (this.mListeners) {
            Iterator<CaptioningChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onUserStyleChanged(userStyle);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyLocaleChanged() {
        Locale locale = getLocale();
        synchronized (this.mListeners) {
            Iterator<CaptioningChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onLocaleChanged(locale);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyFontScaleChanged() {
        float fontScale = getFontScale();
        synchronized (this.mListeners) {
            Iterator<CaptioningChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onFontScaleChanged(fontScale);
            }
        }
    }
}
