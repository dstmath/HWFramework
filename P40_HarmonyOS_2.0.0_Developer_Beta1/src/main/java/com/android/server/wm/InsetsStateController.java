package com.android.server.wm;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import android.view.InsetsSource;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

/* access modifiers changed from: package-private */
public class InsetsStateController {
    private final ArrayMap<Integer, InsetsSourceProvider> mControllers = new ArrayMap<>();
    private final Consumer<WindowState> mDispatchInsetsChanged = $$Lambda$InsetsStateController$sIYEJIR4ztgffCLMi5Z1RvdxyYs.INSTANCE;
    private final DisplayContent mDisplayContent;
    private final InsetsState mLastState = new InsetsState();
    private final ArraySet<WindowState> mPendingControlChanged = new ArraySet<>();
    private final InsetsState mState = new InsetsState();
    private final SparseArray<WindowState> mTypeWinControlMap = new SparseArray<>();
    private final ArrayMap<WindowState, ArrayList<Integer>> mWinControlTypeMap = new ArrayMap<>();

    static /* synthetic */ void lambda$new$0(WindowState w) {
        if (w.isVisible()) {
            w.notifyInsetsChanged();
        }
    }

    InsetsStateController(DisplayContent displayContent) {
        this.mDisplayContent = displayContent;
    }

    /* access modifiers changed from: package-private */
    public InsetsState getInsetsForDispatch(WindowState target) {
        InsetsSourceProvider provider = target.getInsetProvider();
        if (provider == null) {
            return this.mState;
        }
        InsetsState state = new InsetsState();
        state.set(this.mState);
        int type = provider.getSource().getType();
        state.removeSource(type);
        if (type == 1) {
            state.removeSource(10);
            state.removeSource(0);
        }
        return state;
    }

    /* access modifiers changed from: package-private */
    public InsetsSourceControl[] getControlsForDispatch(WindowState target) {
        ArrayList<Integer> controlled = this.mWinControlTypeMap.get(target);
        if (controlled == null) {
            return null;
        }
        int size = controlled.size();
        InsetsSourceControl[] result = new InsetsSourceControl[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.mControllers.get(controlled.get(i)).getControl();
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public InsetsSourceProvider getSourceProvider(int type) {
        return this.mControllers.computeIfAbsent(Integer.valueOf(type), new Function() {
            /* class com.android.server.wm.$$Lambda$InsetsStateController$pXoYGy4X5aPw1QFi0iIWKiTMlDg */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return InsetsStateController.this.lambda$getSourceProvider$1$InsetsStateController((Integer) obj);
            }
        });
    }

    public /* synthetic */ InsetsSourceProvider lambda$getSourceProvider$1$InsetsStateController(Integer key) {
        return new InsetsSourceProvider(this.mState.getSource(key.intValue()), this, this.mDisplayContent);
    }

    /* access modifiers changed from: package-private */
    public void onPostLayout() {
        this.mState.setDisplayFrame(this.mDisplayContent.getBounds());
        for (int i = this.mControllers.size() - 1; i >= 0; i--) {
            this.mControllers.valueAt(i).onPostLayout();
        }
        if (!this.mLastState.equals(this.mState)) {
            this.mLastState.set(this.mState, true);
            notifyInsetsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void onInsetsModified(WindowState windowState, InsetsState state) {
        boolean changed = false;
        for (int i = state.getSourcesCount() - 1; i >= 0; i--) {
            InsetsSource source = state.sourceAt(i);
            InsetsSourceProvider provider = this.mControllers.get(Integer.valueOf(source.getType()));
            if (provider != null) {
                changed |= provider.onInsetsModified(windowState, source);
            }
        }
        if (changed) {
            notifyInsetsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void onImeTargetChanged(WindowState imeTarget) {
        onControlChanged(10, imeTarget);
        notifyPendingInsetsControlChanged();
    }

    /* access modifiers changed from: package-private */
    public void onBarControllingWindowChanged(WindowState controllingWindow) {
        onControlChanged(0, controllingWindow);
        onControlChanged(1, controllingWindow);
        notifyPendingInsetsControlChanged();
    }

    /* access modifiers changed from: package-private */
    public void notifyControlRevoked(WindowState previousControllingWin, InsetsSourceProvider provider) {
        removeFromControlMaps(previousControllingWin, provider.getSource().getType());
    }

    private void onControlChanged(int type, WindowState win) {
        InsetsSourceProvider controller;
        WindowState previous = this.mTypeWinControlMap.get(type);
        if (win != previous && (controller = getSourceProvider(type)) != null && controller.isControllable()) {
            controller.updateControlForTarget(win, false);
            if (previous != null) {
                removeFromControlMaps(previous, type);
                this.mPendingControlChanged.add(previous);
            }
            if (win != null) {
                addToControlMaps(win, type);
                this.mPendingControlChanged.add(win);
            }
        }
    }

    private void removeFromControlMaps(WindowState win, int type) {
        ArrayList<Integer> array = this.mWinControlTypeMap.get(win);
        if (array != null) {
            array.remove(Integer.valueOf(type));
            if (array.isEmpty()) {
                this.mWinControlTypeMap.remove(win);
            }
            this.mTypeWinControlMap.remove(type);
        }
    }

    private void addToControlMaps(WindowState win, int type) {
        this.mWinControlTypeMap.computeIfAbsent(win, $$Lambda$InsetsStateController$Ofxsu0zvrpKfv2Cf4dVk1yXm2uI.INSTANCE).add(Integer.valueOf(type));
        this.mTypeWinControlMap.put(type, win);
    }

    static /* synthetic */ ArrayList lambda$addToControlMaps$2(WindowState key) {
        return new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public void notifyControlChanged(WindowState target) {
        this.mPendingControlChanged.add(target);
        notifyPendingInsetsControlChanged();
    }

    private void notifyPendingInsetsControlChanged() {
        if (!this.mPendingControlChanged.isEmpty()) {
            this.mDisplayContent.mWmService.mAnimator.addAfterPrepareSurfacesRunnable(new Runnable() {
                /* class com.android.server.wm.$$Lambda$InsetsStateController$GPqC21M0LSzcpOJhVE8RaWC9c1g */

                @Override // java.lang.Runnable
                public final void run() {
                    InsetsStateController.this.lambda$notifyPendingInsetsControlChanged$3$InsetsStateController();
                }
            });
        }
    }

    public /* synthetic */ void lambda$notifyPendingInsetsControlChanged$3$InsetsStateController() {
        for (int i = this.mPendingControlChanged.size() - 1; i >= 0; i--) {
            this.mPendingControlChanged.valueAt(i).notifyInsetsControlChanged();
        }
        this.mPendingControlChanged.clear();
    }

    private void notifyInsetsChanged() {
        this.mDisplayContent.forAllWindows(this.mDispatchInsetsChanged, true);
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "WindowInsetsStateController");
        InsetsState insetsState = this.mState;
        insetsState.dump(prefix + "  ", pw);
        pw.println(prefix + "  Control map:");
        for (int i = this.mTypeWinControlMap.size() + -1; i >= 0; i += -1) {
            pw.print(prefix + "  ");
            pw.println(InsetsState.typeToString(this.mTypeWinControlMap.keyAt(i)) + " -> " + this.mTypeWinControlMap.valueAt(i));
        }
    }
}
