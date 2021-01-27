package ohos.agp.components;

import java.util.ArrayList;
import java.util.List;

public final class ComponentTreeObserver {
    private List<GlobalFocusUpdatedListener> mGlobalFocusUpdatedListeners;
    private List<GlobalLayoutListener> mGlobalLayoutListeners;
    private long mNativePtr = 0;
    private List<ScrollChangedListener> mScrollChangedListeners;
    private List<WindowBoundListener> mWindowBoundListeners;
    private List<WindowFocusUpdatedListener> mWindowFocusUpdatedListeners;

    public interface GlobalFocusUpdatedListener {
        void onGlobalFocusUpdated(Component component, Component component2);
    }

    public interface GlobalLayoutListener {
        void onGlobalLayoutUpdated();
    }

    public interface ScrollChangedListener {
        void onScrolled();
    }

    public interface WindowBoundListener {
        void onWindowBound();

        void onWindowUnbound();
    }

    public interface WindowFocusUpdatedListener {
        void onWindowFocusUpdated(boolean z);
    }

    private native long nativeGetComponentTreeObserverHandle(Component component, long j);

    ComponentTreeObserver(Component component) {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetComponentTreeObserverHandle(component, component.getNativeViewPtr());
        }
    }

    public void addWindowFocusUpdatedListener(WindowFocusUpdatedListener windowFocusUpdatedListener) {
        if (this.mWindowFocusUpdatedListeners == null) {
            this.mWindowFocusUpdatedListeners = new ArrayList();
        }
        this.mWindowFocusUpdatedListeners.add(windowFocusUpdatedListener);
    }

    public void removeWindowFocusUpdatedListener(WindowFocusUpdatedListener windowFocusUpdatedListener) {
        List<WindowFocusUpdatedListener> list = this.mWindowFocusUpdatedListeners;
        if (list != null) {
            list.remove(windowFocusUpdatedListener);
        }
    }

    public void addWindowBoundListener(WindowBoundListener windowBoundListener) {
        if (this.mWindowBoundListeners == null) {
            this.mWindowBoundListeners = new ArrayList();
        }
        this.mWindowBoundListeners.add(windowBoundListener);
    }

    public void removeWindowBoundListener(WindowBoundListener windowBoundListener) {
        List<WindowBoundListener> list = this.mWindowBoundListeners;
        if (list != null) {
            list.remove(windowBoundListener);
        }
    }

    public void addScrolledListener(ScrollChangedListener scrollChangedListener) {
        if (this.mScrollChangedListeners == null) {
            this.mScrollChangedListeners = new ArrayList();
        }
        this.mScrollChangedListeners.add(scrollChangedListener);
    }

    public void removeScrolledListener(ScrollChangedListener scrollChangedListener) {
        List<ScrollChangedListener> list = this.mScrollChangedListeners;
        if (list != null) {
            list.remove(scrollChangedListener);
        }
    }

    public void addGlobalFocusUpdatedListener(GlobalFocusUpdatedListener globalFocusUpdatedListener) {
        if (this.mGlobalFocusUpdatedListeners == null) {
            this.mGlobalFocusUpdatedListeners = new ArrayList();
        }
        this.mGlobalFocusUpdatedListeners.add(globalFocusUpdatedListener);
    }

    public void removeGlobalFocusUpdatedListener(GlobalFocusUpdatedListener globalFocusUpdatedListener) {
        List<GlobalFocusUpdatedListener> list = this.mGlobalFocusUpdatedListeners;
        if (list != null) {
            list.remove(globalFocusUpdatedListener);
        }
    }

    public void addTreeLayoutChangedListener(GlobalLayoutListener globalLayoutListener) {
        if (this.mGlobalLayoutListeners == null) {
            this.mGlobalLayoutListeners = new ArrayList();
        }
        this.mGlobalLayoutListeners.add(globalLayoutListener);
    }

    public void removeTreeLayoutChangedListener(GlobalLayoutListener globalLayoutListener) {
        List<GlobalLayoutListener> list = this.mGlobalLayoutListeners;
        if (list != null) {
            list.remove(globalLayoutListener);
        }
    }

    private void dispatchOnWindowFocusChanged(boolean z) {
        List<WindowFocusUpdatedListener> list = this.mWindowFocusUpdatedListeners;
        if (list != null) {
            for (WindowFocusUpdatedListener windowFocusUpdatedListener : list) {
                windowFocusUpdatedListener.onWindowFocusUpdated(z);
            }
        }
    }

    private void dispatchOnWindowAttached() {
        List<WindowBoundListener> list = this.mWindowBoundListeners;
        if (list != null) {
            for (WindowBoundListener windowBoundListener : list) {
                windowBoundListener.onWindowBound();
            }
        }
    }

    private void dispatchOnWindowDetached() {
        List<WindowBoundListener> list = this.mWindowBoundListeners;
        if (list != null) {
            for (WindowBoundListener windowBoundListener : list) {
                windowBoundListener.onWindowUnbound();
            }
        }
    }

    private void dispatchOnScrollChanged() {
        List<ScrollChangedListener> list = this.mScrollChangedListeners;
        if (list != null) {
            for (ScrollChangedListener scrollChangedListener : list) {
                scrollChangedListener.onScrolled();
            }
        }
    }

    private void dispatchOnGlobalFocusChanged(Component component, Component component2) {
        List<GlobalFocusUpdatedListener> list = this.mGlobalFocusUpdatedListeners;
        if (list != null) {
            for (GlobalFocusUpdatedListener globalFocusUpdatedListener : list) {
                globalFocusUpdatedListener.onGlobalFocusUpdated(component, component2);
            }
        }
    }

    private void dispatchOnGlobalLayout() {
        List<GlobalLayoutListener> list = this.mGlobalLayoutListeners;
        if (list != null) {
            for (GlobalLayoutListener globalLayoutListener : list) {
                globalLayoutListener.onGlobalLayoutUpdated();
            }
        }
    }
}
