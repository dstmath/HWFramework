package android.text.method;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;

public interface KeyListener {
    void clearMetaKeyState(View view, Editable editable, int i);

    int getInputType();

    boolean onKeyDown(View view, Editable editable, int i, KeyEvent keyEvent);

    boolean onKeyOther(View view, Editable editable, KeyEvent keyEvent);

    boolean onKeyUp(View view, Editable editable, int i, KeyEvent keyEvent);
}
