package ohos.agp.components;

import java.util.ArrayList;
import java.util.List;

public final class ComponentTreeObserver {
    private List<GlobalFocusChangedListener> mGlobalFocusChangedListeners;
    private List<GlobalLayoutListener> mGlobalLayoutListeners;
    private long mNativePtr = 0;
    private List<ScrollChangedListener> mScrollChangedListeners;
    private List<WindowAttachedListener> mWindowAttachedListeners;
    private List<WindowFocusChangedListener> mWindowFocusChangedListeners;

    public interface GlobalFocusChangedListener {
        void onGlobalFocusChanged(Component component, Component component2);
    }

    public interface GlobalLayoutListener {
        void onGlobalLayoutChanged();
    }

    public interface ScrollChangedListener {
        void onScrollChanged();
    }

    public interface WindowAttachedListener {
        void onWindowAttached();

        void onWindowDetached();
    }

    public interface WindowFocusChangedListener {
        void onWindowFocusChanged(boolean z);
    }

    private native long nativeGetComponentTreeObserverHandle(long j);

    ComponentTreeObserver(long j) {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetComponentTreeObserverHandle(j);
        }
    }

    public void addWindowFocusChangedListener(WindowFocusChangedListener windowFocusChangedListener) {
        if (this.mWindowFocusChangedListeners == null) {
            this.mWindowFocusChangedListeners = new ArrayList();
        }
        this.mWindowFocusChangedListeners.add(windowFocusChangedListener);
    }

    public void removeWindowFocusChangedListener(WindowFocusChangedListener windowFocusChangedListener) {
        List<WindowFocusChangedListener> list = this.mWindowFocusChangedListeners;
        if (list != null) {
            list.remove(windowFocusChangedListener);
        }
    }

    public void addWindowAttachedListener(WindowAttachedListener windowAttachedListener) {
        if (this.mWindowAttachedListeners == null) {
            this.mWindowAttachedListeners = new ArrayList();
        }
        this.mWindowAttachedListeners.add(windowAttachedListener);
    }

    public void removeWindowAttachedListener(WindowAttachedListener windowAttachedListener) {
        List<WindowAttachedListener> list = this.mWindowAttachedListeners;
        if (list != null) {
            list.remove(windowAttachedListener);
        }
    }

    public void addScrollChangedListener(ScrollChangedListener scrollChangedListener) {
        if (this.mScrollChangedListeners == null) {
            this.mScrollChangedListeners = new ArrayList();
        }
        this.mScrollChangedListeners.add(scrollChangedListener);
    }

    public void removeScrollChangedListener(ScrollChangedListener scrollChangedListener) {
        List<ScrollChangedListener> list = this.mScrollChangedListeners;
        if (list != null) {
            list.remove(scrollChangedListener);
        }
    }

    public void addGlobalFocusChangedListener(GlobalFocusChangedListener globalFocusChangedListener) {
        if (this.mGlobalFocusChangedListeners == null) {
            this.mGlobalFocusChangedListeners = new ArrayList();
        }
        this.mGlobalFocusChangedListeners.add(globalFocusChangedListener);
    }

    public void removeGlobalFocusChangeListener(GlobalFocusChangedListener globalFocusChangedListener) {
        List<GlobalFocusChangedListener> list = this.mGlobalFocusChangedListeners;
        if (list != null) {
            list.remove(globalFocusChangedListener);
        }
    }

    public void addGlobalLayoutListener(GlobalLayoutListener globalLayoutListener) {
        if (this.mGlobalLayoutListeners == null) {
            this.mGlobalLayoutListeners = new ArrayList();
        }
        this.mGlobalLayoutListeners.add(globalLayoutListener);
    }

    public void removeGlobalLayoutListener(GlobalLayoutListener globalLayoutListener) {
        List<GlobalLayoutListener> list = this.mGlobalLayoutListeners;
        if (list != null) {
            list.remove(globalLayoutListener);
        }
    }

    private void dispatchOnWindowFocusChanged(boolean z) {
        List<WindowFocusChangedListener> list = this.mWindowFocusChangedListeners;
        if (list != null) {
            for (WindowFocusChangedListener windowFocusChangedListener : list) {
                windowFocusChangedListener.onWindowFocusChanged(z);
            }
        }
    }

    private void dispatchOnWindowAttached() {
        List<WindowAttachedListener> list = this.mWindowAttachedListeners;
        if (list != null) {
            for (WindowAttachedListener windowAttachedListener : list) {
                windowAttachedListener.onWindowAttached();
            }
        }
    }

    private void dispatchOnWindowDetached() {
        List<WindowAttachedListener> list = this.mWindowAttachedListeners;
        if (list != null) {
            for (WindowAttachedListener windowAttachedListener : list) {
                windowAttachedListener.onWindowDetached();
            }
        }
    }

    private void dispatchOnScrollChanged() {
        List<ScrollChangedListener> list = this.mScrollChangedListeners;
        if (list != null) {
            for (ScrollChangedListener scrollChangedListener : list) {
                scrollChangedListener.onScrollChanged();
            }
        }
    }

    private void dispatchOnGlobalFocusChanged(Component component, Component component2) {
        List<GlobalFocusChangedListener> list = this.mGlobalFocusChangedListeners;
        if (list != null) {
            for (GlobalFocusChangedListener globalFocusChangedListener : list) {
                globalFocusChangedListener.onGlobalFocusChanged(component, component2);
            }
        }
    }

    private void dispatchOnGlobalLayout() {
        List<GlobalLayoutListener> list = this.mGlobalLayoutListeners;
        if (list != null) {
            for (GlobalLayoutListener globalLayoutListener : list) {
                globalLayoutListener.onGlobalLayoutChanged();
            }
        }
    }
}
