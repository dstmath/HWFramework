package android.support.v4.view;

import android.view.KeyEvent;
import android.view.KeyEvent.Callback;
import android.view.KeyEvent.DispatcherState;
import android.view.View;

@Deprecated
public final class KeyEventCompat {
    @Deprecated
    public static int normalizeMetaState(int metaState) {
        return KeyEvent.normalizeMetaState(metaState);
    }

    @Deprecated
    public static boolean metaStateHasModifiers(int metaState, int modifiers) {
        return KeyEvent.metaStateHasModifiers(metaState, modifiers);
    }

    @Deprecated
    public static boolean metaStateHasNoModifiers(int metaState) {
        return KeyEvent.metaStateHasNoModifiers(metaState);
    }

    @Deprecated
    public static boolean hasModifiers(KeyEvent event, int modifiers) {
        return event.hasModifiers(modifiers);
    }

    @Deprecated
    public static boolean hasNoModifiers(KeyEvent event) {
        return event.hasNoModifiers();
    }

    @Deprecated
    public static void startTracking(KeyEvent event) {
        event.startTracking();
    }

    @Deprecated
    public static boolean isTracking(KeyEvent event) {
        return event.isTracking();
    }

    @Deprecated
    public static Object getKeyDispatcherState(View view) {
        return view.getKeyDispatcherState();
    }

    @Deprecated
    public static boolean dispatch(KeyEvent event, Callback receiver, Object state, Object target) {
        return event.dispatch(receiver, (DispatcherState) state, target);
    }

    @Deprecated
    public static boolean isCtrlPressed(KeyEvent event) {
        return event.isCtrlPressed();
    }

    private KeyEventCompat() {
    }
}
