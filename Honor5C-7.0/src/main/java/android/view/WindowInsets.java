package android.view;

import android.graphics.Rect;

public final class WindowInsets {
    public static final WindowInsets CONSUMED = null;
    private static final Rect EMPTY_RECT = null;
    private boolean mAlwaysConsumeNavBar;
    private boolean mIsRound;
    private Rect mStableInsets;
    private boolean mStableInsetsConsumed;
    private Rect mSystemWindowInsets;
    private boolean mSystemWindowInsetsConsumed;
    private Rect mTempRect;
    private Rect mWindowDecorInsets;
    private boolean mWindowDecorInsetsConsumed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.WindowInsets.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.WindowInsets.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.WindowInsets.<clinit>():void");
    }

    public WindowInsets(Rect systemWindowInsets, Rect windowDecorInsets, Rect stableInsets, boolean isRound, boolean alwaysConsumeNavBar) {
        boolean z;
        boolean z2 = true;
        this.mSystemWindowInsetsConsumed = false;
        this.mWindowDecorInsetsConsumed = false;
        this.mStableInsetsConsumed = false;
        if (systemWindowInsets == null) {
            z = true;
        } else {
            z = false;
        }
        this.mSystemWindowInsetsConsumed = z;
        if (this.mSystemWindowInsetsConsumed) {
            systemWindowInsets = EMPTY_RECT;
        }
        this.mSystemWindowInsets = systemWindowInsets;
        if (windowDecorInsets == null) {
            z = true;
        } else {
            z = false;
        }
        this.mWindowDecorInsetsConsumed = z;
        if (this.mWindowDecorInsetsConsumed) {
            windowDecorInsets = EMPTY_RECT;
        }
        this.mWindowDecorInsets = windowDecorInsets;
        if (stableInsets != null) {
            z2 = false;
        }
        this.mStableInsetsConsumed = z2;
        if (this.mStableInsetsConsumed) {
            stableInsets = EMPTY_RECT;
        }
        this.mStableInsets = stableInsets;
        this.mIsRound = isRound;
        this.mAlwaysConsumeNavBar = alwaysConsumeNavBar;
    }

    public WindowInsets(WindowInsets src) {
        this.mSystemWindowInsetsConsumed = false;
        this.mWindowDecorInsetsConsumed = false;
        this.mStableInsetsConsumed = false;
        this.mSystemWindowInsets = src.mSystemWindowInsets;
        this.mWindowDecorInsets = src.mWindowDecorInsets;
        this.mStableInsets = src.mStableInsets;
        this.mSystemWindowInsetsConsumed = src.mSystemWindowInsetsConsumed;
        this.mWindowDecorInsetsConsumed = src.mWindowDecorInsetsConsumed;
        this.mStableInsetsConsumed = src.mStableInsetsConsumed;
        this.mIsRound = src.mIsRound;
        this.mAlwaysConsumeNavBar = src.mAlwaysConsumeNavBar;
    }

    public WindowInsets(Rect systemWindowInsets) {
        this(systemWindowInsets, null, null, false, false);
    }

    public Rect getSystemWindowInsets() {
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        if (this.mSystemWindowInsets != null) {
            this.mTempRect.set(this.mSystemWindowInsets);
        } else {
            this.mTempRect.setEmpty();
        }
        return this.mTempRect;
    }

    public int getSystemWindowInsetLeft() {
        return this.mSystemWindowInsets.left;
    }

    public int getSystemWindowInsetTop() {
        return this.mSystemWindowInsets.top;
    }

    public int getSystemWindowInsetRight() {
        return this.mSystemWindowInsets.right;
    }

    public int getSystemWindowInsetBottom() {
        return this.mSystemWindowInsets.bottom;
    }

    public int getWindowDecorInsetLeft() {
        return this.mWindowDecorInsets.left;
    }

    public int getWindowDecorInsetTop() {
        return this.mWindowDecorInsets.top;
    }

    public int getWindowDecorInsetRight() {
        return this.mWindowDecorInsets.right;
    }

    public int getWindowDecorInsetBottom() {
        return this.mWindowDecorInsets.bottom;
    }

    public boolean hasSystemWindowInsets() {
        if (this.mSystemWindowInsets.left == 0 && this.mSystemWindowInsets.top == 0 && this.mSystemWindowInsets.right == 0 && this.mSystemWindowInsets.bottom == 0) {
            return false;
        }
        return true;
    }

    public boolean hasWindowDecorInsets() {
        if (this.mWindowDecorInsets.left == 0 && this.mWindowDecorInsets.top == 0 && this.mWindowDecorInsets.right == 0 && this.mWindowDecorInsets.bottom == 0) {
            return false;
        }
        return true;
    }

    public boolean hasInsets() {
        return (hasSystemWindowInsets() || hasWindowDecorInsets()) ? true : hasStableInsets();
    }

    public boolean isConsumed() {
        return (this.mSystemWindowInsetsConsumed && this.mWindowDecorInsetsConsumed) ? this.mStableInsetsConsumed : false;
    }

    public boolean isRound() {
        return this.mIsRound;
    }

    public WindowInsets consumeSystemWindowInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = EMPTY_RECT;
        result.mSystemWindowInsetsConsumed = true;
        return result;
    }

    public WindowInsets consumeSystemWindowInsets(boolean left, boolean top, boolean right, boolean bottom) {
        int i = 0;
        if (!left && !top && !right && !bottom) {
            return this;
        }
        WindowInsets result = new WindowInsets(this);
        int i2 = left ? 0 : this.mSystemWindowInsets.left;
        int i3 = top ? 0 : this.mSystemWindowInsets.top;
        int i4 = right ? 0 : this.mSystemWindowInsets.right;
        if (!bottom) {
            i = this.mSystemWindowInsets.bottom;
        }
        result.mSystemWindowInsets = new Rect(i2, i3, i4, i);
        return result;
    }

    public WindowInsets replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = new Rect(left, top, right, bottom);
        return result;
    }

    public WindowInsets replaceSystemWindowInsets(Rect systemWindowInsets) {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = new Rect(systemWindowInsets);
        return result;
    }

    public WindowInsets consumeWindowDecorInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mWindowDecorInsets.set(0, 0, 0, 0);
        result.mWindowDecorInsetsConsumed = true;
        return result;
    }

    public WindowInsets consumeWindowDecorInsets(boolean left, boolean top, boolean right, boolean bottom) {
        int i = 0;
        if (!left && !top && !right && !bottom) {
            return this;
        }
        WindowInsets result = new WindowInsets(this);
        int i2 = left ? 0 : this.mWindowDecorInsets.left;
        int i3 = top ? 0 : this.mWindowDecorInsets.top;
        int i4 = right ? 0 : this.mWindowDecorInsets.right;
        if (!bottom) {
            i = this.mWindowDecorInsets.bottom;
        }
        result.mWindowDecorInsets = new Rect(i2, i3, i4, i);
        return result;
    }

    public WindowInsets replaceWindowDecorInsets(int left, int top, int right, int bottom) {
        WindowInsets result = new WindowInsets(this);
        result.mWindowDecorInsets = new Rect(left, top, right, bottom);
        return result;
    }

    public int getStableInsetTop() {
        return this.mStableInsets.top;
    }

    public int getStableInsetLeft() {
        return this.mStableInsets.left;
    }

    public int getStableInsetRight() {
        return this.mStableInsets.right;
    }

    public int getStableInsetBottom() {
        return this.mStableInsets.bottom;
    }

    public boolean hasStableInsets() {
        if (this.mStableInsets.top == 0 && this.mStableInsets.left == 0 && this.mStableInsets.right == 0 && this.mStableInsets.bottom == 0) {
            return false;
        }
        return true;
    }

    public WindowInsets consumeStableInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mStableInsets = EMPTY_RECT;
        result.mStableInsetsConsumed = true;
        return result;
    }

    public boolean shouldAlwaysConsumeNavBar() {
        return this.mAlwaysConsumeNavBar;
    }

    public String toString() {
        return "WindowInsets{systemWindowInsets=" + this.mSystemWindowInsets + " windowDecorInsets=" + this.mWindowDecorInsets + " stableInsets=" + this.mStableInsets + (isRound() ? " round}" : "}");
    }
}
