package android.text.method;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import java.lang.ref.WeakReference;
import javax.microedition.khronos.opengles.GL10;

public class TextKeyListener extends BaseKeyListener implements SpanWatcher {
    static final Object ACTIVE = null;
    static final int AUTO_CAP = 1;
    static final int AUTO_PERIOD = 4;
    static final int AUTO_TEXT = 2;
    static final Object CAPPED = null;
    static final Object INHIBIT_REPLACEMENT = null;
    static final Object LAST_TYPED = null;
    static final int SHOW_PASSWORD = 8;
    private static TextKeyListener[] sInstance;
    private Capitalize mAutoCap;
    private boolean mAutoText;
    private SettingsObserver mObserver;
    private int mPrefs;
    private boolean mPrefsInited;
    private WeakReference<ContentResolver> mResolver;

    public enum Capitalize {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.TextKeyListener.Capitalize.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.TextKeyListener.Capitalize.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.method.TextKeyListener.Capitalize.<clinit>():void");
        }
    }

    private static class NullKeyListener implements KeyListener {
        private static NullKeyListener sInstance;

        private NullKeyListener() {
        }

        public int getInputType() {
            return 0;
        }

        public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
            return false;
        }

        public boolean onKeyUp(View view, Editable content, int keyCode, KeyEvent event) {
            return false;
        }

        public boolean onKeyOther(View view, Editable content, KeyEvent event) {
            return false;
        }

        public void clearMetaKeyState(View view, Editable content, int states) {
        }

        public static NullKeyListener getInstance() {
            if (sInstance != null) {
                return sInstance;
            }
            sInstance = new NullKeyListener();
            return sInstance;
        }
    }

    private class SettingsObserver extends ContentObserver {
        final /* synthetic */ TextKeyListener this$0;

        public SettingsObserver(TextKeyListener this$0) {
            this.this$0 = this$0;
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (this.this$0.mResolver != null) {
                ContentResolver contentResolver = (ContentResolver) this.this$0.mResolver.get();
                if (contentResolver == null) {
                    this.this$0.mPrefsInited = false;
                    return;
                } else {
                    this.this$0.updatePrefs(contentResolver);
                    return;
                }
            }
            this.this$0.mPrefsInited = false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.TextKeyListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.TextKeyListener.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.TextKeyListener.<clinit>():void");
    }

    public TextKeyListener(Capitalize cap, boolean autotext) {
        this.mAutoCap = cap;
        this.mAutoText = autotext;
    }

    public static TextKeyListener getInstance(boolean autotext, Capitalize cap) {
        int off = (cap.ordinal() * AUTO_TEXT) + (autotext ? AUTO_CAP : 0);
        if (sInstance[off] == null) {
            sInstance[off] = new TextKeyListener(cap, autotext);
        }
        return sInstance[off];
    }

    public static TextKeyListener getInstance() {
        return getInstance(false, Capitalize.NONE);
    }

    public static boolean shouldCap(Capitalize cap, CharSequence cs, int off) {
        if (cap == Capitalize.NONE) {
            return false;
        }
        if (cap == Capitalize.CHARACTERS) {
            return true;
        }
        boolean z;
        if (TextUtils.getCapsMode(cs, off, cap == Capitalize.WORDS ? AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD : GL10.GL_LIGHT0) != 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public int getInputType() {
        return BaseKeyListener.makeTextContentType(this.mAutoCap, this.mAutoText);
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        return getKeyListener(event).onKeyDown(view, content, keyCode, event);
    }

    public boolean onKeyUp(View view, Editable content, int keyCode, KeyEvent event) {
        return getKeyListener(event).onKeyUp(view, content, keyCode, event);
    }

    public boolean onKeyOther(View view, Editable content, KeyEvent event) {
        return getKeyListener(event).onKeyOther(view, content, event);
    }

    public static void clear(Editable e) {
        e.clear();
        e.removeSpan(ACTIVE);
        e.removeSpan(CAPPED);
        e.removeSpan(INHIBIT_REPLACEMENT);
        e.removeSpan(LAST_TYPED);
        Replaced[] repl = (Replaced[]) e.getSpans(0, e.length(), Replaced.class);
        int count = repl.length;
        for (int i = 0; i < count; i += AUTO_CAP) {
            e.removeSpan(repl[i]);
        }
    }

    public void onSpanAdded(Spannable s, Object what, int start, int end) {
    }

    public void onSpanRemoved(Spannable s, Object what, int start, int end) {
    }

    public void onSpanChanged(Spannable s, Object what, int start, int end, int st, int en) {
        if (what == Selection.SELECTION_END) {
            s.removeSpan(ACTIVE);
        }
    }

    private KeyListener getKeyListener(KeyEvent event) {
        int kind = event.getKeyCharacterMap().getKeyboardType();
        if (kind == 3) {
            return QwertyKeyListener.getInstance(this.mAutoText, this.mAutoCap);
        }
        if (kind == AUTO_CAP) {
            return MultiTapKeyListener.getInstance(this.mAutoText, this.mAutoCap);
        }
        if (kind == AUTO_PERIOD || kind == 5) {
            return QwertyKeyListener.getInstanceForFullKeyboard();
        }
        return NullKeyListener.getInstance();
    }

    public void release() {
        if (this.mResolver != null) {
            ContentResolver contentResolver = (ContentResolver) this.mResolver.get();
            if (contentResolver != null) {
                contentResolver.unregisterContentObserver(this.mObserver);
                this.mResolver.clear();
            }
            this.mObserver = null;
            this.mResolver = null;
            this.mPrefsInited = false;
        }
    }

    private void initPrefs(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        this.mResolver = new WeakReference(contentResolver);
        if (this.mObserver == null) {
            this.mObserver = new SettingsObserver(this);
            contentResolver.registerContentObserver(System.CONTENT_URI, true, this.mObserver);
        }
        updatePrefs(contentResolver);
        this.mPrefsInited = true;
    }

    private void updatePrefs(ContentResolver resolver) {
        int i;
        int i2;
        int i3 = 0;
        boolean cap = System.getInt(resolver, "auto_caps", AUTO_CAP) > 0;
        boolean text = System.getInt(resolver, "auto_replace", AUTO_CAP) > 0;
        boolean period = System.getInt(resolver, "auto_punctuate", AUTO_CAP) > 0;
        boolean pw = System.getInt(resolver, "show_password", AUTO_CAP) > 0;
        if (cap) {
            i = AUTO_CAP;
        } else {
            i = 0;
        }
        if (text) {
            i2 = AUTO_TEXT;
        } else {
            i2 = 0;
        }
        i |= i2;
        if (period) {
            i2 = AUTO_PERIOD;
        } else {
            i2 = 0;
        }
        i2 |= i;
        if (pw) {
            i3 = SHOW_PASSWORD;
        }
        this.mPrefs = i3 | i2;
    }

    int getPrefs(Context context) {
        synchronized (this) {
            if (!this.mPrefsInited || this.mResolver.get() == null) {
                initPrefs(context);
            }
        }
        return this.mPrefs;
    }
}
