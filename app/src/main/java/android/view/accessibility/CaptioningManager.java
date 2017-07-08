package android.view.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.View;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Locale;

public class CaptioningManager {
    private static final int DEFAULT_ENABLED = 0;
    private static final float DEFAULT_FONT_SCALE = 1.0f;
    private static final int DEFAULT_PRESET = 0;
    private final ContentObserver mContentObserver;
    private final ContentResolver mContentResolver;
    private final ArrayList<CaptioningChangeListener> mListeners;
    private final Runnable mStyleChangedRunnable;

    public static final class CaptionStyle {
        private static final CaptionStyle BLACK_ON_WHITE = null;
        private static final int COLOR_NONE_OPAQUE = 255;
        public static final int COLOR_UNSPECIFIED = 16777215;
        public static final CaptionStyle DEFAULT = null;
        private static final CaptionStyle DEFAULT_CUSTOM = null;
        public static final int EDGE_TYPE_DEPRESSED = 4;
        public static final int EDGE_TYPE_DROP_SHADOW = 2;
        public static final int EDGE_TYPE_NONE = 0;
        public static final int EDGE_TYPE_OUTLINE = 1;
        public static final int EDGE_TYPE_RAISED = 3;
        public static final int EDGE_TYPE_UNSPECIFIED = -1;
        public static final CaptionStyle[] PRESETS = null;
        public static final int PRESET_CUSTOM = -1;
        private static final CaptionStyle UNSPECIFIED = null;
        private static final CaptionStyle WHITE_ON_BLACK = null;
        private static final CaptionStyle YELLOW_ON_BLACK = null;
        private static final CaptionStyle YELLOW_ON_BLUE = null;
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

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.CaptioningManager.CaptionStyle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.CaptioningManager.CaptionStyle.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.CaptioningManager.CaptionStyle.<clinit>():void");
        }

        private CaptionStyle(int foregroundColor, int backgroundColor, int edgeType, int edgeColor, int windowColor, String rawTypeface) {
            boolean z;
            this.mHasForegroundColor = hasColor(foregroundColor);
            this.mHasBackgroundColor = hasColor(backgroundColor);
            if (edgeType != PRESET_CUSTOM) {
                z = true;
            } else {
                z = false;
            }
            this.mHasEdgeType = z;
            this.mHasEdgeColor = hasColor(edgeColor);
            this.mHasWindowColor = hasColor(windowColor);
            if (!this.mHasForegroundColor) {
                foregroundColor = PRESET_CUSTOM;
            }
            this.foregroundColor = foregroundColor;
            if (!this.mHasBackgroundColor) {
                backgroundColor = View.MEASURED_STATE_MASK;
            }
            this.backgroundColor = backgroundColor;
            if (!this.mHasEdgeType) {
                edgeType = EDGE_TYPE_NONE;
            }
            this.edgeType = edgeType;
            if (!this.mHasEdgeColor) {
                edgeColor = View.MEASURED_STATE_MASK;
            }
            this.edgeColor = edgeColor;
            if (!this.mHasWindowColor) {
                windowColor = COLOR_NONE_OPAQUE;
            }
            this.windowColor = windowColor;
            this.mRawTypeface = rawTypeface;
        }

        public static boolean hasColor(int packedColor) {
            return (packedColor >>> 24) != 0 || (16776960 & packedColor) == 0;
        }

        public CaptionStyle applyStyle(CaptionStyle overlay) {
            return new CaptionStyle(overlay.hasForegroundColor() ? overlay.foregroundColor : this.foregroundColor, overlay.hasBackgroundColor() ? overlay.backgroundColor : this.backgroundColor, overlay.hasEdgeType() ? overlay.edgeType : this.edgeType, overlay.hasEdgeColor() ? overlay.edgeColor : this.edgeColor, overlay.hasWindowColor() ? overlay.windowColor : this.windowColor, overlay.mRawTypeface != null ? overlay.mRawTypeface : this.mRawTypeface);
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
                this.mParsedTypeface = Typeface.create(this.mRawTypeface, EDGE_TYPE_NONE);
            }
            return this.mParsedTypeface;
        }

        public static CaptionStyle getCustomStyle(ContentResolver cr) {
            CaptionStyle defStyle = DEFAULT_CUSTOM;
            int foregroundColor = Secure.getInt(cr, "accessibility_captioning_foreground_color", defStyle.foregroundColor);
            int backgroundColor = Secure.getInt(cr, "accessibility_captioning_background_color", defStyle.backgroundColor);
            int edgeType = Secure.getInt(cr, "accessibility_captioning_edge_type", defStyle.edgeType);
            int edgeColor = Secure.getInt(cr, "accessibility_captioning_edge_color", defStyle.edgeColor);
            int windowColor = Secure.getInt(cr, "accessibility_captioning_window_color", defStyle.windowColor);
            String rawTypeface = Secure.getString(cr, "accessibility_captioning_typeface");
            if (rawTypeface == null) {
                rawTypeface = defStyle.mRawTypeface;
            }
            return new CaptionStyle(foregroundColor, backgroundColor, edgeType, edgeColor, windowColor, rawTypeface);
        }
    }

    public static abstract class CaptioningChangeListener {
        public CaptioningChangeListener() {
        }

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
        final /* synthetic */ CaptioningManager this$0;

        public MyContentObserver(CaptioningManager this$0, Handler handler) {
            this.this$0 = this$0;
            super(handler);
            this.mHandler = handler;
        }

        public void onChange(boolean selfChange, Uri uri) {
            String uriPath = uri.getPath();
            String name = uriPath.substring(uriPath.lastIndexOf(47) + 1);
            if ("accessibility_captioning_enabled".equals(name)) {
                this.this$0.notifyEnabledChanged();
            } else if ("accessibility_captioning_locale".equals(name)) {
                this.this$0.notifyLocaleChanged();
            } else if ("accessibility_captioning_font_scale".equals(name)) {
                this.this$0.notifyFontScaleChanged();
            } else {
                this.mHandler.removeCallbacks(this.this$0.mStyleChangedRunnable);
                this.mHandler.post(this.this$0.mStyleChangedRunnable);
            }
        }
    }

    public CaptioningManager(Context context) {
        this.mListeners = new ArrayList();
        this.mStyleChangedRunnable = new Runnable() {
            public void run() {
                CaptioningManager.this.notifyUserStyleChanged();
            }
        };
        this.mContentResolver = context.getContentResolver();
        this.mContentObserver = new MyContentObserver(this, new Handler(context.getMainLooper()));
    }

    public final boolean isEnabled() {
        if (Secure.getInt(this.mContentResolver, "accessibility_captioning_enabled", DEFAULT_ENABLED) == 1) {
            return true;
        }
        return false;
    }

    public final String getRawLocale() {
        return Secure.getString(this.mContentResolver, "accessibility_captioning_locale");
    }

    public final Locale getLocale() {
        String rawLocale = getRawLocale();
        if (!TextUtils.isEmpty(rawLocale)) {
            String[] splitLocale = rawLocale.split("_");
            switch (splitLocale.length) {
                case HwCfgFilePolicy.EMUI /*1*/:
                    return new Locale(splitLocale[DEFAULT_ENABLED]);
                case HwCfgFilePolicy.PC /*2*/:
                    return new Locale(splitLocale[DEFAULT_ENABLED], splitLocale[1]);
                case HwCfgFilePolicy.BASE /*3*/:
                    return new Locale(splitLocale[DEFAULT_ENABLED], splitLocale[1], splitLocale[2]);
            }
        }
        return null;
    }

    public final float getFontScale() {
        return Secure.getFloat(this.mContentResolver, "accessibility_captioning_font_scale", DEFAULT_FONT_SCALE);
    }

    public int getRawUserStyle() {
        return Secure.getInt(this.mContentResolver, "accessibility_captioning_preset", DEFAULT_ENABLED);
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
                registerObserver("accessibility_captioning_enabled");
                registerObserver("accessibility_captioning_foreground_color");
                registerObserver("accessibility_captioning_background_color");
                registerObserver("accessibility_captioning_window_color");
                registerObserver("accessibility_captioning_edge_type");
                registerObserver("accessibility_captioning_edge_color");
                registerObserver("accessibility_captioning_typeface");
                registerObserver("accessibility_captioning_font_scale");
                registerObserver("accessibility_captioning_locale");
                registerObserver("accessibility_captioning_preset");
            }
            this.mListeners.add(listener);
        }
    }

    private void registerObserver(String key) {
        this.mContentResolver.registerContentObserver(Secure.getUriFor(key), false, this.mContentObserver);
    }

    public void removeCaptioningChangeListener(CaptioningChangeListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
            if (this.mListeners.isEmpty()) {
                this.mContentResolver.unregisterContentObserver(this.mContentObserver);
            }
        }
    }

    private void notifyEnabledChanged() {
        boolean enabled = isEnabled();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onEnabledChanged(enabled);
            }
        }
    }

    private void notifyUserStyleChanged() {
        CaptionStyle userStyle = getUserStyle();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onUserStyleChanged(userStyle);
            }
        }
    }

    private void notifyLocaleChanged() {
        Locale locale = getLocale();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onLocaleChanged(locale);
            }
        }
    }

    private void notifyFontScaleChanged() {
        float fontScale = getFontScale();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onFontScaleChanged(fontScale);
            }
        }
    }
}
