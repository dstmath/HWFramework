package android.text.method;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;

public abstract class MetaKeyKeyListener {
    private static final Object ALT = null;
    private static final Object CAP = null;
    private static final int LOCKED = 67108881;
    private static final int LOCKED_RETURN_VALUE = 2;
    public static final int META_ALT_LOCKED = 512;
    private static final long META_ALT_MASK = 565157566611970L;
    public static final int META_ALT_ON = 2;
    private static final long META_ALT_PRESSED = 2199023255552L;
    private static final long META_ALT_RELEASED = 562949953421312L;
    private static final long META_ALT_USED = 8589934592L;
    public static final int META_CAP_LOCKED = 256;
    private static final long META_CAP_PRESSED = 1099511627776L;
    private static final long META_CAP_RELEASED = 281474976710656L;
    private static final long META_CAP_USED = 4294967296L;
    public static final int META_SELECTING = 2048;
    private static final long META_SHIFT_MASK = 282578783305985L;
    public static final int META_SHIFT_ON = 1;
    public static final int META_SYM_LOCKED = 1024;
    private static final long META_SYM_MASK = 1130315133223940L;
    public static final int META_SYM_ON = 4;
    private static final long META_SYM_PRESSED = 4398046511104L;
    private static final long META_SYM_RELEASED = 1125899906842624L;
    private static final long META_SYM_USED = 17179869184L;
    private static final int PRESSED = 16777233;
    private static final int PRESSED_RETURN_VALUE = 1;
    private static final int RELEASED = 33554449;
    private static final Object SELECTING = null;
    private static final Object SYM = null;
    private static final int USED = 50331665;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.MetaKeyKeyListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.MetaKeyKeyListener.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.MetaKeyKeyListener.<clinit>():void");
    }

    private static long press(long r1, int r3, long r4, long r6, long r8, long r10, long r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.MetaKeyKeyListener.press(long, int, long, long, long, long, long):long
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.MetaKeyKeyListener.press(long, int, long, long, long, long, long):long");
    }

    private static long release(long r1, int r3, long r4, long r6, long r8, long r10, android.view.KeyEvent r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.MetaKeyKeyListener.release(long, int, long, long, long, long, android.view.KeyEvent):long
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.MetaKeyKeyListener.release(long, int, long, long, long, long, android.view.KeyEvent):long");
    }

    public static void resetMetaState(Spannable text) {
        text.removeSpan(CAP);
        text.removeSpan(ALT);
        text.removeSpan(SYM);
        text.removeSpan(SELECTING);
    }

    public static final int getMetaState(CharSequence text) {
        return ((getActive(text, CAP, PRESSED_RETURN_VALUE, META_CAP_LOCKED) | getActive(text, ALT, META_ALT_ON, META_ALT_LOCKED)) | getActive(text, SYM, META_SYM_ON, META_SYM_LOCKED)) | getActive(text, SELECTING, META_SELECTING, META_SELECTING);
    }

    public static final int getMetaState(CharSequence text, KeyEvent event) {
        int metaState = event.getMetaState();
        if (event.getKeyCharacterMap().getModifierBehavior() == PRESSED_RETURN_VALUE) {
            return metaState | getMetaState(text);
        }
        return metaState;
    }

    public static final int getMetaState(CharSequence text, int meta) {
        switch (meta) {
            case PRESSED_RETURN_VALUE /*1*/:
                return getActive(text, CAP, PRESSED_RETURN_VALUE, META_ALT_ON);
            case META_ALT_ON /*2*/:
                return getActive(text, ALT, PRESSED_RETURN_VALUE, META_ALT_ON);
            case META_SYM_ON /*4*/:
                return getActive(text, SYM, PRESSED_RETURN_VALUE, META_ALT_ON);
            case META_SELECTING /*2048*/:
                return getActive(text, SELECTING, PRESSED_RETURN_VALUE, META_ALT_ON);
            default:
                return 0;
        }
    }

    public static final int getMetaState(CharSequence text, int meta, KeyEvent event) {
        int metaState = event.getMetaState();
        if (event.getKeyCharacterMap().getModifierBehavior() == PRESSED_RETURN_VALUE) {
            metaState |= getMetaState(text);
        }
        if (META_SELECTING == meta) {
            return (metaState & META_SELECTING) != 0 ? PRESSED_RETURN_VALUE : 0;
        } else {
            return getMetaState((long) metaState, meta);
        }
    }

    private static int getActive(CharSequence text, Object meta, int on, int lock) {
        if (!(text instanceof Spanned)) {
            return 0;
        }
        int flag = ((Spanned) text).getSpanFlags(meta);
        if (flag == LOCKED) {
            return lock;
        }
        if (flag != 0) {
            return on;
        }
        return 0;
    }

    public static void adjustMetaAfterKeypress(Spannable content) {
        adjust(content, CAP);
        adjust(content, ALT);
        adjust(content, SYM);
    }

    public static boolean isMetaTracker(CharSequence text, Object what) {
        if (what == CAP || what == ALT || what == SYM || what == SELECTING) {
            return true;
        }
        return false;
    }

    public static boolean isSelectingMetaTracker(CharSequence text, Object what) {
        return what == SELECTING;
    }

    private static void adjust(Spannable content, Object what) {
        int current = content.getSpanFlags(what);
        if (current == PRESSED) {
            content.setSpan(what, 0, 0, USED);
        } else if (current == RELEASED) {
            content.removeSpan(what);
        }
    }

    protected static void resetLockedMeta(Spannable content) {
        resetLock(content, CAP);
        resetLock(content, ALT);
        resetLock(content, SYM);
        resetLock(content, SELECTING);
    }

    private static void resetLock(Spannable content, Object what) {
        if (content.getSpanFlags(what) == LOCKED) {
            content.removeSpan(what);
        }
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        if (keyCode == 59 || keyCode == 60) {
            press(content, CAP);
            return true;
        } else if (keyCode == 57 || keyCode == 58 || keyCode == 78) {
            press(content, ALT);
            return true;
        } else if (keyCode != 63) {
            return false;
        } else {
            press(content, SYM);
            return true;
        }
    }

    private void press(Editable content, Object what) {
        int state = content.getSpanFlags(what);
        if (state == PRESSED) {
            return;
        }
        if (state == RELEASED) {
            content.setSpan(what, 0, 0, LOCKED);
        } else if (state == USED) {
        } else {
            if (state == LOCKED) {
                content.removeSpan(what);
            } else {
                content.setSpan(what, 0, 0, PRESSED);
            }
        }
    }

    public static void startSelecting(View view, Spannable content) {
        content.setSpan(SELECTING, 0, 0, PRESSED);
    }

    public static void stopSelecting(View view, Spannable content) {
        content.removeSpan(SELECTING);
    }

    public boolean onKeyUp(View view, Editable content, int keyCode, KeyEvent event) {
        if (keyCode == 59 || keyCode == 60) {
            release(content, CAP, event);
            return true;
        } else if (keyCode == 57 || keyCode == 58 || keyCode == 78) {
            release(content, ALT, event);
            return true;
        } else if (keyCode != 63) {
            return false;
        } else {
            release(content, SYM, event);
            return true;
        }
    }

    private void release(Editable content, Object what, KeyEvent event) {
        int current = content.getSpanFlags(what);
        switch (event.getKeyCharacterMap().getModifierBehavior()) {
            case PRESSED_RETURN_VALUE /*1*/:
                if (current == USED) {
                    content.removeSpan(what);
                } else if (current == PRESSED) {
                    content.setSpan(what, 0, 0, RELEASED);
                }
            default:
                content.removeSpan(what);
        }
    }

    public void clearMetaKeyState(View view, Editable content, int states) {
        clearMetaKeyState(content, states);
    }

    public static void clearMetaKeyState(Editable content, int states) {
        if ((states & PRESSED_RETURN_VALUE) != 0) {
            content.removeSpan(CAP);
        }
        if ((states & META_ALT_ON) != 0) {
            content.removeSpan(ALT);
        }
        if ((states & META_SYM_ON) != 0) {
            content.removeSpan(SYM);
        }
        if ((states & META_SELECTING) != 0) {
            content.removeSpan(SELECTING);
        }
    }

    public static long resetLockedMeta(long state) {
        if ((256 & state) != 0) {
            state &= -282578783305986L;
        }
        if ((512 & state) != 0) {
            state &= -565157566611971L;
        }
        if ((1024 & state) != 0) {
            return state & -1130315133223941L;
        }
        return state;
    }

    public static final int getMetaState(long state) {
        int result = 0;
        if ((256 & state) != 0) {
            result = META_CAP_LOCKED;
        } else if ((1 & state) != 0) {
            result = PRESSED_RETURN_VALUE;
        }
        if ((512 & state) != 0) {
            result |= META_ALT_LOCKED;
        } else if ((2 & state) != 0) {
            result |= META_ALT_ON;
        }
        if ((1024 & state) != 0) {
            return result | META_SYM_LOCKED;
        }
        if ((4 & state) != 0) {
            return result | META_SYM_ON;
        }
        return result;
    }

    public static final int getMetaState(long state, int meta) {
        switch (meta) {
            case PRESSED_RETURN_VALUE /*1*/:
                if ((256 & state) != 0) {
                    return META_ALT_ON;
                }
                return (1 & state) != 0 ? PRESSED_RETURN_VALUE : 0;
            case META_ALT_ON /*2*/:
                if ((512 & state) != 0) {
                    return META_ALT_ON;
                }
                return (2 & state) != 0 ? PRESSED_RETURN_VALUE : 0;
            case META_SYM_ON /*4*/:
                if ((1024 & state) != 0) {
                    return META_ALT_ON;
                }
                return (4 & state) != 0 ? PRESSED_RETURN_VALUE : 0;
            default:
                return 0;
        }
    }

    public static long adjustMetaAfterKeypress(long state) {
        if ((META_CAP_PRESSED & state) != 0) {
            state = ((state & -282578783305986L) | 1) | META_CAP_USED;
        } else if ((META_CAP_RELEASED & state) != 0) {
            state &= -282578783305986L;
        }
        if ((META_ALT_PRESSED & state) != 0) {
            state = ((state & -565157566611971L) | 2) | META_ALT_USED;
        } else if ((META_ALT_RELEASED & state) != 0) {
            state &= -565157566611971L;
        }
        if ((META_SYM_PRESSED & state) != 0) {
            return ((state & -1130315133223941L) | 4) | META_SYM_USED;
        }
        if ((META_SYM_RELEASED & state) != 0) {
            return state & -1130315133223941L;
        }
        return state;
    }

    public static long handleKeyDown(long state, int keyCode, KeyEvent event) {
        if (keyCode == 59 || keyCode == 60) {
            return press(state, PRESSED_RETURN_VALUE, META_SHIFT_MASK, 256, META_CAP_PRESSED, META_CAP_RELEASED, META_CAP_USED);
        }
        if (keyCode == 57 || keyCode == 58 || keyCode == 78) {
            return press(state, META_ALT_ON, META_ALT_MASK, 512, META_ALT_PRESSED, META_ALT_RELEASED, META_ALT_USED);
        }
        if (keyCode == 63) {
            return press(state, META_SYM_ON, META_SYM_MASK, 1024, META_SYM_PRESSED, META_SYM_RELEASED, META_SYM_USED);
        }
        return state;
    }

    public static long handleKeyUp(long state, int keyCode, KeyEvent event) {
        if (keyCode == 59 || keyCode == 60) {
            return release(state, PRESSED_RETURN_VALUE, META_SHIFT_MASK, META_CAP_PRESSED, META_CAP_RELEASED, META_CAP_USED, event);
        }
        if (keyCode == 57 || keyCode == 58 || keyCode == 78) {
            return release(state, META_ALT_ON, META_ALT_MASK, META_ALT_PRESSED, META_ALT_RELEASED, META_ALT_USED, event);
        }
        if (keyCode == 63) {
            return release(state, META_SYM_ON, META_SYM_MASK, META_SYM_PRESSED, META_SYM_RELEASED, META_SYM_USED, event);
        }
        return state;
    }

    public long clearMetaKeyState(long state, int which) {
        if (!((which & PRESSED_RETURN_VALUE) == 0 || (256 & state) == 0)) {
            state &= -282578783305986L;
        }
        if (!((which & META_ALT_ON) == 0 || (512 & state) == 0)) {
            state &= -565157566611971L;
        }
        if ((which & META_SYM_ON) == 0 || (1024 & state) == 0) {
            return state;
        }
        return state & -1130315133223941L;
    }
}
