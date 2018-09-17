package android.view;

public interface FallbackEventHandler {
    boolean dispatchKeyEvent(KeyEvent keyEvent);

    void preDispatchKeyEvent(KeyEvent keyEvent);

    void setView(View view);
}
