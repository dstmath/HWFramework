package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreDevGui extends CoreInterface {
    private transient long agpCptr;

    CoreDevGui(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreDevGui(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreDevGui obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.agpengine.impl.CoreInterface
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean isEnabled) {
        CoreJni.setEnabledInCoreDevGui(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        return CoreJni.isEnabledInCoreDevGui(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void frameStart() {
        CoreJni.frameStartInCoreDevGui(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setKey(int key, boolean isDown) {
        CoreJni.setKeyInCoreDevGui(this.agpCptr, this, key, isDown);
    }

    /* access modifiers changed from: package-private */
    public void inputCharacter(long character) {
        CoreJni.inputCharacterInCoreDevGui(this.agpCptr, this, character);
    }

    /* access modifiers changed from: package-private */
    public void setMousePos(float posX, float posY) {
        CoreJni.setMousePosInCoreDevGui(this.agpCptr, this, posX, posY);
    }

    /* access modifiers changed from: package-private */
    public void setMouseButtonState(int buttonIndex, boolean isPressed) {
        CoreJni.setMouseButtonStateInCoreDevGui(this.agpCptr, this, buttonIndex, isPressed);
    }

    /* access modifiers changed from: package-private */
    public void setScroll(float horizontal, float vertical) {
        CoreJni.setScrollInCoreDevGui(this.agpCptr, this, horizontal, vertical);
    }

    /* access modifiers changed from: package-private */
    public boolean wantCaptureMouse() {
        return CoreJni.wantCaptureMouseInCoreDevGui(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreMouseCursorState getMouseCursorState() {
        return CoreMouseCursorState.swigToEnum(CoreJni.getMouseCursorStateInCoreDevGui(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public long getImGuiContext() {
        return CoreJni.getImGuiContextInCoreDevGui(this.agpCptr, this);
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

        static CoreMouseCursorState swigToEnum(int swigValue2) {
            CoreMouseCursorState[] swigValues = (CoreMouseCursorState[]) CoreMouseCursorState.class.getEnumConstants();
            if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
                return swigValues[swigValue2];
            }
            for (CoreMouseCursorState swigEnum : swigValues) {
                if (swigEnum.swigValue == swigValue2) {
                    return swigEnum;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreMouseCursorState.class + " with value " + swigValue2);
        }

        private CoreMouseCursorState() {
            this.swigValue = SwigNext.next;
            SwigNext.access$008();
        }

        private CoreMouseCursorState(int swigValue2) {
            this.swigValue = swigValue2;
            int unused = SwigNext.next = swigValue2 + 1;
        }

        private CoreMouseCursorState(CoreMouseCursorState swigEnum) {
            this.swigValue = swigEnum.swigValue;
            int unused = SwigNext.next = this.swigValue + 1;
        }

        private static class SwigNext {
            private static int next = 0;

            private SwigNext() {
            }

            static /* synthetic */ int access$008() {
                int i = next;
                next = i + 1;
                return i;
            }
        }
    }
}
