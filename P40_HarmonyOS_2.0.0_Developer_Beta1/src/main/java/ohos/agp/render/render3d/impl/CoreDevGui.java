package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreDevGui {
    private transient long agpCptrCoreDevGui;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreDevGui(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreDevGui = j;
    }

    static long getCptr(CoreDevGui coreDevGui) {
        if (coreDevGui == null) {
            return 0;
        }
        return coreDevGui.agpCptrCoreDevGui;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreDevGui != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreDevGui = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreDevGui coreDevGui, boolean z) {
        if (coreDevGui != null) {
            synchronized (coreDevGui.lock) {
                coreDevGui.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreDevGui);
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean z) {
        CoreJni.setEnabledInCoreDevGui(this.agpCptrCoreDevGui, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        return CoreJni.isEnabledInCoreDevGui(this.agpCptrCoreDevGui, this);
    }

    /* access modifiers changed from: package-private */
    public void frameStart() {
        CoreJni.frameStartInCoreDevGui(this.agpCptrCoreDevGui, this);
    }

    /* access modifiers changed from: package-private */
    public void setKey(int i, boolean z) {
        CoreJni.setKeyInCoreDevGui(this.agpCptrCoreDevGui, this, i, z);
    }

    /* access modifiers changed from: package-private */
    public void inputCharacter(long j) {
        CoreJni.inputCharacterInCoreDevGui(this.agpCptrCoreDevGui, this, j);
    }

    /* access modifiers changed from: package-private */
    public void setMousePos(float f, float f2) {
        CoreJni.setMousePosInCoreDevGui(this.agpCptrCoreDevGui, this, f, f2);
    }

    /* access modifiers changed from: package-private */
    public void setMouseButtonState(int i, boolean z) {
        CoreJni.setMouseButtonStateInCoreDevGui(this.agpCptrCoreDevGui, this, i, z);
    }

    /* access modifiers changed from: package-private */
    public void setScroll(float f, float f2) {
        CoreJni.setScrollInCoreDevGui(this.agpCptrCoreDevGui, this, f, f2);
    }

    /* access modifiers changed from: package-private */
    public boolean wantCaptureMouse() {
        return CoreJni.wantCaptureMouseInCoreDevGui(this.agpCptrCoreDevGui, this);
    }

    /* access modifiers changed from: package-private */
    public CoreMouseCursorState getMouseCursorState() {
        return CoreMouseCursorState.swigToEnum(CoreJni.getMouseCursorStateInCoreDevGui(this.agpCptrCoreDevGui, this));
    }

    /* access modifiers changed from: package-private */
    public long getImGuiContext() {
        return CoreJni.getImGuiContextInCoreDevGui(this.agpCptrCoreDevGui, this);
    }

    enum CoreMouseCursorState {
        MOUSE_CURSOR_NONE(0),
        MOUSE_CURSOR_ARROW,
        MOUSE_CURSOR_TEXT_INPUT,
        MOUSE_CURSOR_RESIZE_ALL,
        MOUSE_CURSOR_RESIZE_NS,
        MOUSE_CURSOR_RESIZE_EW,
        MOUSE_CURSOR_RESIZE_NESW,
        MOUSE_CURSOR_RESIZE_NWSE,
        MOUSE_CURSOR_HAND;
        
        private final int swigValue;

        /* access modifiers changed from: package-private */
        public final int swigValue() {
            return this.swigValue;
        }

        static CoreMouseCursorState swigToEnum(int i) {
            CoreMouseCursorState[] coreMouseCursorStateArr = (CoreMouseCursorState[]) CoreMouseCursorState.class.getEnumConstants();
            if (i < coreMouseCursorStateArr.length && i >= 0 && coreMouseCursorStateArr[i].swigValue == i) {
                return coreMouseCursorStateArr[i];
            }
            for (CoreMouseCursorState coreMouseCursorState : coreMouseCursorStateArr) {
                if (coreMouseCursorState.swigValue == i) {
                    return coreMouseCursorState;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreMouseCursorState.class + " with value " + i);
        }

        private CoreMouseCursorState() {
            this(SwigNext.next);
        }

        private CoreMouseCursorState(int i) {
            this.swigValue = i;
            int unused = SwigNext.next = i + 1;
        }

        private CoreMouseCursorState(CoreMouseCursorState coreMouseCursorState) {
            this(coreMouseCursorState.swigValue);
        }

        private static class SwigNext {
            private static int next;

            private SwigNext() {
            }
        }
    }
}
