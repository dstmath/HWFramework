package ohos.agp.text;

import java.util.ArrayList;
import java.util.List;
import ohos.agp.components.Component;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.multimodalinput.event.TouchEvent;

public class RichText {
    private TouchEventListenerHelper mListenerHelper;
    private final List<TouchEventListener> mListenerList = new ArrayList();
    private final long mNativeRichText;

    public interface TouchEventListener {
        boolean onTouchEvent(Component component, TouchEvent touchEvent);
    }

    /* access modifiers changed from: private */
    public interface TouchEventListenerHelper {
        boolean onTouchEvent(int i, Component component, TouchEvent touchEvent);
    }

    private native void nativeAddTouchEventListener(long j, TouchEventListenerHelper touchEventListenerHelper, int i, int i2, int i3);

    private native int nativeGetLeadingMargin(long j, boolean z);

    private native void nativeSetLeadingMargin(long j, int i, int i2);

    private static class RichTextCleaner extends NativeMemoryCleanerHelper {
        private native void nativeRichTextRelease(long j);

        public RichTextCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            nativeRichTextRelease(j);
        }
    }

    RichText(long j) {
        this.mNativeRichText = j;
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new RichTextCleaner(this.mNativeRichText), this.mNativeRichText);
    }

    public long getNativeRichText() {
        return this.mNativeRichText;
    }

    public void addTouchEventListener(TouchEventListener touchEventListener, int i, int i2) {
        if (touchEventListener != null && i >= 0 && i2 > i) {
            this.mListenerList.add(touchEventListener);
            if (this.mListenerHelper == null) {
                this.mListenerHelper = new TouchEventListenerHelper() {
                    /* class ohos.agp.text.$$Lambda$RichText$Juosts_m2xw0miiZUyM9xF3C6k */

                    @Override // ohos.agp.text.RichText.TouchEventListenerHelper
                    public final boolean onTouchEvent(int i, Component component, TouchEvent touchEvent) {
                        return RichText.this.lambda$addTouchEventListener$0$RichText(i, component, touchEvent);
                    }
                };
            }
            nativeAddTouchEventListener(this.mNativeRichText, this.mListenerHelper, this.mListenerList.size() - 1, i, i2);
        }
    }

    public /* synthetic */ boolean lambda$addTouchEventListener$0$RichText(int i, Component component, TouchEvent touchEvent) {
        if (i < 0 || i >= this.mListenerList.size()) {
            return false;
        }
        return this.mListenerList.get(i).onTouchEvent(component, touchEvent);
    }

    public void setLeadingMargin(int i, int i2) {
        nativeSetLeadingMargin(this.mNativeRichText, i, i2);
    }

    public int getLeadingMargin(boolean z) {
        return nativeGetLeadingMargin(this.mNativeRichText, z);
    }
}
