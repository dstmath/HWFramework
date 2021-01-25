package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Debug;
import android.os.Looper;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ViewTreeObserver {
    private static boolean sIllegalOnDrawModificationIsFatal;
    private boolean mAlive = true;
    private CopyOnWriteArray<Consumer<List<Rect>>> mGestureExclusionListeners;
    private boolean mInDispatchOnDraw;
    @UnsupportedAppUsage
    private CopyOnWriteArray<OnComputeInternalInsetsListener> mOnComputeInternalInsetsListeners;
    private ArrayList<OnDrawListener> mOnDrawListeners;
    private CopyOnWriteArrayList<OnEnterAnimationCompleteListener> mOnEnterAnimationCompleteListeners;
    private ArrayList<Runnable> mOnFrameCommitListeners;
    private CopyOnWriteArrayList<OnGlobalFocusChangeListener> mOnGlobalFocusListeners;
    @UnsupportedAppUsage
    private CopyOnWriteArray<OnGlobalLayoutListener> mOnGlobalLayoutListeners;
    private CopyOnWriteArray<OnPreDrawListener> mOnPreDrawListeners;
    @UnsupportedAppUsage
    private CopyOnWriteArray<OnScrollChangedListener> mOnScrollChangedListeners;
    @UnsupportedAppUsage
    private CopyOnWriteArrayList<OnTouchModeChangeListener> mOnTouchModeChangeListeners;
    private CopyOnWriteArrayList<OnWindowAttachListener> mOnWindowAttachListeners;
    private CopyOnWriteArrayList<OnWindowFocusChangeListener> mOnWindowFocusListeners;
    private CopyOnWriteArray<OnWindowShownListener> mOnWindowShownListeners;
    private boolean mWindowShown;

    public interface OnComputeInternalInsetsListener {
        void onComputeInternalInsets(InternalInsetsInfo internalInsetsInfo);
    }

    public interface OnDrawListener {
        void onDraw();
    }

    public interface OnEnterAnimationCompleteListener {
        void onEnterAnimationComplete();
    }

    public interface OnGlobalFocusChangeListener {
        void onGlobalFocusChanged(View view, View view2);
    }

    public interface OnGlobalLayoutListener {
        void onGlobalLayout();
    }

    public interface OnPreDrawListener {
        boolean onPreDraw();
    }

    public interface OnScrollChangedListener {
        void onScrollChanged();
    }

    public interface OnTouchModeChangeListener {
        void onTouchModeChanged(boolean z);
    }

    public interface OnWindowAttachListener {
        void onWindowAttached();

        void onWindowDetached();
    }

    public interface OnWindowFocusChangeListener {
        void onWindowFocusChanged(boolean z);
    }

    public interface OnWindowShownListener {
        void onWindowShown();
    }

    public static final class InternalInsetsInfo {
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        @UnsupportedAppUsage
        public static final int TOUCHABLE_INSETS_REGION = 3;
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;
        @UnsupportedAppUsage
        public final Rect contentInsets = new Rect();
        @UnsupportedAppUsage
        int mTouchableInsets;
        @UnsupportedAppUsage
        public final Region touchableRegion = new Region();
        @UnsupportedAppUsage
        public final Rect visibleInsets = new Rect();

        @UnsupportedAppUsage
        public void setTouchableInsets(int val) {
            this.mTouchableInsets = val;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.contentInsets.setEmpty();
            this.visibleInsets.setEmpty();
            this.touchableRegion.setEmpty();
            this.mTouchableInsets = 0;
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.contentInsets.isEmpty() && this.visibleInsets.isEmpty() && this.touchableRegion.isEmpty() && this.mTouchableInsets == 0;
        }

        public int hashCode() {
            return (((((this.contentInsets.hashCode() * 31) + this.visibleInsets.hashCode()) * 31) + this.touchableRegion.hashCode()) * 31) + this.mTouchableInsets;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InternalInsetsInfo other = (InternalInsetsInfo) o;
            if (this.mTouchableInsets != other.mTouchableInsets || !this.contentInsets.equals(other.contentInsets) || !this.visibleInsets.equals(other.visibleInsets) || !this.touchableRegion.equals(other.touchableRegion)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        @UnsupportedAppUsage
        public void set(InternalInsetsInfo other) {
            this.contentInsets.set(other.contentInsets);
            this.visibleInsets.set(other.visibleInsets);
            this.touchableRegion.set(other.touchableRegion);
            this.mTouchableInsets = other.mTouchableInsets;
        }
    }

    ViewTreeObserver(Context context) {
        boolean z = true;
        sIllegalOnDrawModificationIsFatal = context.getApplicationInfo().targetSdkVersion < 26 ? false : z;
    }

    /* access modifiers changed from: package-private */
    public void merge(ViewTreeObserver observer) {
        CopyOnWriteArrayList<OnWindowAttachListener> copyOnWriteArrayList = observer.mOnWindowAttachListeners;
        if (copyOnWriteArrayList != null) {
            CopyOnWriteArrayList<OnWindowAttachListener> copyOnWriteArrayList2 = this.mOnWindowAttachListeners;
            if (copyOnWriteArrayList2 != null) {
                copyOnWriteArrayList2.addAll(copyOnWriteArrayList);
            } else {
                this.mOnWindowAttachListeners = copyOnWriteArrayList;
            }
        }
        CopyOnWriteArrayList<OnWindowFocusChangeListener> copyOnWriteArrayList3 = observer.mOnWindowFocusListeners;
        if (copyOnWriteArrayList3 != null) {
            CopyOnWriteArrayList<OnWindowFocusChangeListener> copyOnWriteArrayList4 = this.mOnWindowFocusListeners;
            if (copyOnWriteArrayList4 != null) {
                copyOnWriteArrayList4.addAll(copyOnWriteArrayList3);
            } else {
                this.mOnWindowFocusListeners = copyOnWriteArrayList3;
            }
        }
        CopyOnWriteArrayList<OnGlobalFocusChangeListener> copyOnWriteArrayList5 = observer.mOnGlobalFocusListeners;
        if (copyOnWriteArrayList5 != null) {
            CopyOnWriteArrayList<OnGlobalFocusChangeListener> copyOnWriteArrayList6 = this.mOnGlobalFocusListeners;
            if (copyOnWriteArrayList6 != null) {
                copyOnWriteArrayList6.addAll(copyOnWriteArrayList5);
            } else {
                this.mOnGlobalFocusListeners = copyOnWriteArrayList5;
            }
        }
        CopyOnWriteArray<OnGlobalLayoutListener> copyOnWriteArray = observer.mOnGlobalLayoutListeners;
        if (copyOnWriteArray != null) {
            CopyOnWriteArray<OnGlobalLayoutListener> copyOnWriteArray2 = this.mOnGlobalLayoutListeners;
            if (copyOnWriteArray2 != null) {
                copyOnWriteArray2.addAll(copyOnWriteArray);
            } else {
                this.mOnGlobalLayoutListeners = copyOnWriteArray;
            }
        }
        CopyOnWriteArray<OnPreDrawListener> copyOnWriteArray3 = observer.mOnPreDrawListeners;
        if (copyOnWriteArray3 != null) {
            CopyOnWriteArray<OnPreDrawListener> copyOnWriteArray4 = this.mOnPreDrawListeners;
            if (copyOnWriteArray4 != null) {
                copyOnWriteArray4.addAll(copyOnWriteArray3);
            } else {
                this.mOnPreDrawListeners = copyOnWriteArray3;
            }
        }
        ArrayList<OnDrawListener> arrayList = observer.mOnDrawListeners;
        if (arrayList != null) {
            ArrayList<OnDrawListener> arrayList2 = this.mOnDrawListeners;
            if (arrayList2 != null) {
                arrayList2.addAll(arrayList);
            } else {
                this.mOnDrawListeners = arrayList;
            }
        }
        if (observer.mOnFrameCommitListeners != null) {
            ArrayList<Runnable> arrayList3 = this.mOnFrameCommitListeners;
            if (arrayList3 != null) {
                arrayList3.addAll(observer.captureFrameCommitCallbacks());
            } else {
                this.mOnFrameCommitListeners = observer.captureFrameCommitCallbacks();
            }
        }
        CopyOnWriteArrayList<OnTouchModeChangeListener> copyOnWriteArrayList7 = observer.mOnTouchModeChangeListeners;
        if (copyOnWriteArrayList7 != null) {
            CopyOnWriteArrayList<OnTouchModeChangeListener> copyOnWriteArrayList8 = this.mOnTouchModeChangeListeners;
            if (copyOnWriteArrayList8 != null) {
                copyOnWriteArrayList8.addAll(copyOnWriteArrayList7);
            } else {
                this.mOnTouchModeChangeListeners = copyOnWriteArrayList7;
            }
        }
        CopyOnWriteArray<OnComputeInternalInsetsListener> copyOnWriteArray5 = observer.mOnComputeInternalInsetsListeners;
        if (copyOnWriteArray5 != null) {
            CopyOnWriteArray<OnComputeInternalInsetsListener> copyOnWriteArray6 = this.mOnComputeInternalInsetsListeners;
            if (copyOnWriteArray6 != null) {
                copyOnWriteArray6.addAll(copyOnWriteArray5);
            } else {
                this.mOnComputeInternalInsetsListeners = copyOnWriteArray5;
            }
        }
        CopyOnWriteArray<OnScrollChangedListener> copyOnWriteArray7 = observer.mOnScrollChangedListeners;
        if (copyOnWriteArray7 != null) {
            CopyOnWriteArray<OnScrollChangedListener> copyOnWriteArray8 = this.mOnScrollChangedListeners;
            if (copyOnWriteArray8 != null) {
                copyOnWriteArray8.addAll(copyOnWriteArray7);
            } else {
                this.mOnScrollChangedListeners = copyOnWriteArray7;
            }
        }
        CopyOnWriteArray<OnWindowShownListener> copyOnWriteArray9 = observer.mOnWindowShownListeners;
        if (copyOnWriteArray9 != null) {
            CopyOnWriteArray<OnWindowShownListener> copyOnWriteArray10 = this.mOnWindowShownListeners;
            if (copyOnWriteArray10 != null) {
                copyOnWriteArray10.addAll(copyOnWriteArray9);
            } else {
                this.mOnWindowShownListeners = copyOnWriteArray9;
            }
        }
        CopyOnWriteArray<Consumer<List<Rect>>> copyOnWriteArray11 = observer.mGestureExclusionListeners;
        if (copyOnWriteArray11 != null) {
            CopyOnWriteArray<Consumer<List<Rect>>> copyOnWriteArray12 = this.mGestureExclusionListeners;
            if (copyOnWriteArray12 != null) {
                copyOnWriteArray12.addAll(copyOnWriteArray11);
            } else {
                this.mGestureExclusionListeners = copyOnWriteArray11;
            }
        }
        observer.kill();
    }

    public void addOnWindowAttachListener(OnWindowAttachListener listener) {
        checkIsAlive();
        if (this.mOnWindowAttachListeners == null) {
            this.mOnWindowAttachListeners = new CopyOnWriteArrayList<>();
        }
        this.mOnWindowAttachListeners.add(listener);
    }

    public void removeOnWindowAttachListener(OnWindowAttachListener victim) {
        checkIsAlive();
        CopyOnWriteArrayList<OnWindowAttachListener> copyOnWriteArrayList = this.mOnWindowAttachListeners;
        if (copyOnWriteArrayList != null) {
            copyOnWriteArrayList.remove(victim);
        }
    }

    public void addOnWindowFocusChangeListener(OnWindowFocusChangeListener listener) {
        checkIsAlive();
        if (this.mOnWindowFocusListeners == null) {
            this.mOnWindowFocusListeners = new CopyOnWriteArrayList<>();
        }
        this.mOnWindowFocusListeners.add(listener);
    }

    public void removeOnWindowFocusChangeListener(OnWindowFocusChangeListener victim) {
        checkIsAlive();
        CopyOnWriteArrayList<OnWindowFocusChangeListener> copyOnWriteArrayList = this.mOnWindowFocusListeners;
        if (copyOnWriteArrayList != null) {
            copyOnWriteArrayList.remove(victim);
        }
    }

    public void addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {
        checkIsAlive();
        if (this.mOnGlobalFocusListeners == null) {
            this.mOnGlobalFocusListeners = new CopyOnWriteArrayList<>();
        }
        this.mOnGlobalFocusListeners.add(listener);
    }

    public void removeOnGlobalFocusChangeListener(OnGlobalFocusChangeListener victim) {
        checkIsAlive();
        CopyOnWriteArrayList<OnGlobalFocusChangeListener> copyOnWriteArrayList = this.mOnGlobalFocusListeners;
        if (copyOnWriteArrayList != null) {
            copyOnWriteArrayList.remove(victim);
        }
    }

    public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {
        checkIsAlive();
        if (this.mOnGlobalLayoutListeners == null) {
            this.mOnGlobalLayoutListeners = new CopyOnWriteArray<>();
        }
        this.mOnGlobalLayoutListeners.add(listener);
    }

    @Deprecated
    public void removeGlobalOnLayoutListener(OnGlobalLayoutListener victim) {
        removeOnGlobalLayoutListener(victim);
    }

    public void removeOnGlobalLayoutListener(OnGlobalLayoutListener victim) {
        checkIsAlive();
        CopyOnWriteArray<OnGlobalLayoutListener> copyOnWriteArray = this.mOnGlobalLayoutListeners;
        if (copyOnWriteArray != null) {
            copyOnWriteArray.remove(victim);
        }
    }

    public void addOnPreDrawListener(OnPreDrawListener listener) {
        checkIsAlive();
        if (this.mOnPreDrawListeners == null) {
            this.mOnPreDrawListeners = new CopyOnWriteArray<>();
        }
        this.mOnPreDrawListeners.add(listener);
    }

    public void removeOnPreDrawListener(OnPreDrawListener victim) {
        checkIsAlive();
        CopyOnWriteArray<OnPreDrawListener> copyOnWriteArray = this.mOnPreDrawListeners;
        if (copyOnWriteArray != null) {
            copyOnWriteArray.remove(victim);
        }
    }

    public void addOnWindowShownListener(OnWindowShownListener listener) {
        checkIsAlive();
        if (this.mOnWindowShownListeners == null) {
            this.mOnWindowShownListeners = new CopyOnWriteArray<>();
        }
        this.mOnWindowShownListeners.add(listener);
        if (this.mWindowShown) {
            listener.onWindowShown();
        }
    }

    public void removeOnWindowShownListener(OnWindowShownListener victim) {
        checkIsAlive();
        CopyOnWriteArray<OnWindowShownListener> copyOnWriteArray = this.mOnWindowShownListeners;
        if (copyOnWriteArray != null) {
            copyOnWriteArray.remove(victim);
        }
    }

    public void addOnDrawListener(OnDrawListener listener) {
        checkIsAlive();
        if (this.mOnDrawListeners == null) {
            this.mOnDrawListeners = new ArrayList<>();
        }
        if (this.mInDispatchOnDraw) {
            IllegalStateException ex = new IllegalStateException("Cannot call addOnDrawListener inside of onDraw");
            if (!sIllegalOnDrawModificationIsFatal) {
                Log.e("ViewTreeObserver", ex.getMessage(), ex);
            } else {
                throw ex;
            }
        }
        this.mOnDrawListeners.add(listener);
    }

    public void removeOnDrawListener(OnDrawListener victim) {
        checkIsAlive();
        if (this.mOnDrawListeners != null) {
            if (this.mInDispatchOnDraw) {
                IllegalStateException ex = new IllegalStateException("Cannot call removeOnDrawListener inside of onDraw");
                if (!sIllegalOnDrawModificationIsFatal) {
                    Log.e("ViewTreeObserver", ex.getMessage(), ex);
                } else {
                    throw ex;
                }
            }
            this.mOnDrawListeners.remove(victim);
        }
    }

    public void registerFrameCommitCallback(Runnable callback) {
        checkIsAlive();
        if (this.mOnFrameCommitListeners == null) {
            this.mOnFrameCommitListeners = new ArrayList<>();
        }
        this.mOnFrameCommitListeners.add(callback);
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Runnable> captureFrameCommitCallbacks() {
        ArrayList<Runnable> ret = this.mOnFrameCommitListeners;
        this.mOnFrameCommitListeners = null;
        return ret;
    }

    public boolean unregisterFrameCommitCallback(Runnable callback) {
        checkIsAlive();
        ArrayList<Runnable> arrayList = this.mOnFrameCommitListeners;
        if (arrayList == null) {
            return false;
        }
        return arrayList.remove(callback);
    }

    public void addOnScrollChangedListener(OnScrollChangedListener listener) {
        checkIsAlive();
        if (this.mOnScrollChangedListeners == null) {
            this.mOnScrollChangedListeners = new CopyOnWriteArray<>();
        }
        if (listener == null) {
            Slog.e("ViewTreeObserver", "addOnScrollChangedListener listener is null, callers: " + Debug.getCallers(8));
        }
        this.mOnScrollChangedListeners.add(listener);
    }

    public void removeOnScrollChangedListener(OnScrollChangedListener victim) {
        checkIsAlive();
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Slog.e("ViewTreeObserver", "removeOnScrollChangedListener listener in other threads, callers: " + Debug.getCallers(8));
        }
        CopyOnWriteArray<OnScrollChangedListener> copyOnWriteArray = this.mOnScrollChangedListeners;
        if (copyOnWriteArray != null) {
            copyOnWriteArray.remove(victim);
        }
    }

    public void addOnTouchModeChangeListener(OnTouchModeChangeListener listener) {
        checkIsAlive();
        if (this.mOnTouchModeChangeListeners == null) {
            this.mOnTouchModeChangeListeners = new CopyOnWriteArrayList<>();
        }
        this.mOnTouchModeChangeListeners.add(listener);
    }

    public void removeOnTouchModeChangeListener(OnTouchModeChangeListener victim) {
        checkIsAlive();
        CopyOnWriteArrayList<OnTouchModeChangeListener> copyOnWriteArrayList = this.mOnTouchModeChangeListeners;
        if (copyOnWriteArrayList != null) {
            copyOnWriteArrayList.remove(victim);
        }
    }

    @UnsupportedAppUsage
    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
        checkIsAlive();
        if (this.mOnComputeInternalInsetsListeners == null) {
            this.mOnComputeInternalInsetsListeners = new CopyOnWriteArray<>();
        }
        this.mOnComputeInternalInsetsListeners.add(listener);
    }

    @UnsupportedAppUsage
    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener victim) {
        checkIsAlive();
        CopyOnWriteArray<OnComputeInternalInsetsListener> copyOnWriteArray = this.mOnComputeInternalInsetsListeners;
        if (copyOnWriteArray != null) {
            copyOnWriteArray.remove(victim);
        }
    }

    public void addOnEnterAnimationCompleteListener(OnEnterAnimationCompleteListener listener) {
        checkIsAlive();
        if (this.mOnEnterAnimationCompleteListeners == null) {
            this.mOnEnterAnimationCompleteListeners = new CopyOnWriteArrayList<>();
        }
        this.mOnEnterAnimationCompleteListeners.add(listener);
    }

    public void removeOnEnterAnimationCompleteListener(OnEnterAnimationCompleteListener listener) {
        checkIsAlive();
        CopyOnWriteArrayList<OnEnterAnimationCompleteListener> copyOnWriteArrayList = this.mOnEnterAnimationCompleteListeners;
        if (copyOnWriteArrayList != null) {
            copyOnWriteArrayList.remove(listener);
        }
    }

    public void addOnSystemGestureExclusionRectsChangedListener(Consumer<List<Rect>> listener) {
        checkIsAlive();
        if (this.mGestureExclusionListeners == null) {
            this.mGestureExclusionListeners = new CopyOnWriteArray<>();
        }
        this.mGestureExclusionListeners.add(listener);
    }

    public void removeOnSystemGestureExclusionRectsChangedListener(Consumer<List<Rect>> listener) {
        checkIsAlive();
        CopyOnWriteArray<Consumer<List<Rect>>> copyOnWriteArray = this.mGestureExclusionListeners;
        if (copyOnWriteArray != null) {
            copyOnWriteArray.remove(listener);
        }
    }

    private void checkIsAlive() {
        if (!this.mAlive) {
            throw new IllegalStateException("This ViewTreeObserver is not alive, call getViewTreeObserver() again");
        }
    }

    public boolean isAlive() {
        return this.mAlive;
    }

    private void kill() {
        this.mAlive = false;
    }

    /* access modifiers changed from: package-private */
    public final void dispatchOnWindowAttachedChange(boolean attached) {
        CopyOnWriteArrayList<OnWindowAttachListener> listeners = this.mOnWindowAttachListeners;
        if (listeners != null && listeners.size() > 0) {
            Iterator<OnWindowAttachListener> it = listeners.iterator();
            while (it.hasNext()) {
                OnWindowAttachListener listener = it.next();
                if (attached) {
                    listener.onWindowAttached();
                } else {
                    listener.onWindowDetached();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void dispatchOnWindowFocusChange(boolean hasFocus) {
        CopyOnWriteArrayList<OnWindowFocusChangeListener> listeners = this.mOnWindowFocusListeners;
        if (listeners != null && listeners.size() > 0) {
            Iterator<OnWindowFocusChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().onWindowFocusChanged(hasFocus);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void dispatchOnGlobalFocusChange(View oldFocus, View newFocus) {
        CopyOnWriteArrayList<OnGlobalFocusChangeListener> listeners = this.mOnGlobalFocusListeners;
        if (listeners != null && listeners.size() > 0) {
            Iterator<OnGlobalFocusChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().onGlobalFocusChanged(oldFocus, newFocus);
            }
        }
    }

    public final void dispatchOnGlobalLayout() {
        CopyOnWriteArray<OnGlobalLayoutListener> listeners = this.mOnGlobalLayoutListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnGlobalLayoutListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onGlobalLayout();
                }
            } finally {
                listeners.end();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean hasOnPreDrawListeners() {
        CopyOnWriteArray<OnPreDrawListener> copyOnWriteArray = this.mOnPreDrawListeners;
        return copyOnWriteArray != null && copyOnWriteArray.size() > 0;
    }

    public final boolean dispatchOnPreDraw() {
        boolean cancelDraw = false;
        CopyOnWriteArray<OnPreDrawListener> listeners = this.mOnPreDrawListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnPreDrawListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    cancelDraw |= !access.get(i).onPreDraw();
                }
            } finally {
                listeners.end();
            }
        }
        return cancelDraw;
    }

    public final void dispatchOnWindowShown() {
        this.mWindowShown = true;
        CopyOnWriteArray<OnWindowShownListener> listeners = this.mOnWindowShownListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnWindowShownListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onWindowShown();
                }
            } finally {
                listeners.end();
            }
        }
    }

    public final void dispatchOnDraw() {
        if (this.mOnDrawListeners != null) {
            this.mInDispatchOnDraw = true;
            ArrayList<OnDrawListener> listeners = this.mOnDrawListeners;
            int numListeners = listeners.size();
            for (int i = 0; i < numListeners; i++) {
                listeners.get(i).onDraw();
            }
            this.mInDispatchOnDraw = false;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void dispatchOnTouchModeChanged(boolean inTouchMode) {
        CopyOnWriteArrayList<OnTouchModeChangeListener> listeners = this.mOnTouchModeChangeListeners;
        if (listeners != null && listeners.size() > 0) {
            Iterator<OnTouchModeChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().onTouchModeChanged(inTouchMode);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void dispatchOnScrollChanged() {
        CopyOnWriteArray<OnScrollChangedListener> listeners = this.mOnScrollChangedListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnScrollChangedListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    if (access.get(i) != null) {
                        access.get(i).onScrollChanged();
                    } else {
                        Slog.e("ViewTreeObserver", "there is a null pointer in access");
                    }
                }
            } finally {
                listeners.end();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final boolean hasComputeInternalInsetsListeners() {
        CopyOnWriteArray<OnComputeInternalInsetsListener> listeners = this.mOnComputeInternalInsetsListeners;
        return listeners != null && listeners.size() > 0;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void dispatchOnComputeInternalInsets(InternalInsetsInfo inoutInfo) {
        CopyOnWriteArray<OnComputeInternalInsetsListener> listeners = this.mOnComputeInternalInsetsListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnComputeInternalInsetsListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onComputeInternalInsets(inoutInfo);
                }
            } finally {
                listeners.end();
            }
        }
    }

    public final void dispatchOnEnterAnimationComplete() {
        CopyOnWriteArrayList<OnEnterAnimationCompleteListener> listeners = this.mOnEnterAnimationCompleteListeners;
        if (listeners != null && !listeners.isEmpty()) {
            Iterator<OnEnterAnimationCompleteListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().onEnterAnimationComplete();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnSystemGestureExclusionRectsChanged(List<Rect> rects) {
        CopyOnWriteArray<Consumer<List<Rect>>> listeners = this.mGestureExclusionListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<Consumer<List<Rect>>> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).accept(rects);
                }
            } finally {
                listeners.end();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class CopyOnWriteArray<T> {
        private final Access<T> mAccess = new Access<>();
        private ArrayList<T> mData = new ArrayList<>();
        private ArrayList<T> mDataCopy;
        private boolean mStart;

        /* access modifiers changed from: package-private */
        public static class Access<T> {
            private ArrayList<T> mData;
            private int mSize;

            Access() {
            }

            /* access modifiers changed from: package-private */
            public T get(int index) {
                return this.mData.get(index);
            }

            /* access modifiers changed from: package-private */
            public int size() {
                return this.mSize;
            }
        }

        CopyOnWriteArray() {
        }

        private ArrayList<T> getArray() {
            if (!this.mStart) {
                return this.mData;
            }
            if (this.mDataCopy == null) {
                this.mDataCopy = new ArrayList<>(this.mData);
            }
            return this.mDataCopy;
        }

        /* access modifiers changed from: package-private */
        public Access<T> start() {
            if (!this.mStart) {
                this.mStart = true;
                this.mDataCopy = null;
                ((Access) this.mAccess).mData = this.mData;
                ((Access) this.mAccess).mSize = this.mData.size();
                return this.mAccess;
            }
            throw new IllegalStateException("Iteration already started");
        }

        /* access modifiers changed from: package-private */
        public void end() {
            if (this.mStart) {
                this.mStart = false;
                ArrayList<T> arrayList = this.mDataCopy;
                if (arrayList != null) {
                    this.mData = arrayList;
                    ((Access) this.mAccess).mData.clear();
                    ((Access) this.mAccess).mSize = 0;
                }
                this.mDataCopy = null;
                return;
            }
            throw new IllegalStateException("Iteration not started");
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return getArray().size();
        }

        /* access modifiers changed from: package-private */
        public void add(T item) {
            getArray().add(item);
        }

        /* access modifiers changed from: package-private */
        public void addAll(CopyOnWriteArray<T> array) {
            getArray().addAll(array.mData);
        }

        /* access modifiers changed from: package-private */
        public void remove(T item) {
            getArray().remove(item);
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            getArray().clear();
        }
    }
}
