package huawei.android.widget;

import android.view.KeyEvent;

public interface HwOnEditEventListener {
    boolean onCopy(int i, KeyEvent keyEvent);

    boolean onCut(int i, KeyEvent keyEvent);

    boolean onDelete(int i, KeyEvent keyEvent);

    boolean onPaste(int i, KeyEvent keyEvent);

    boolean onSelectAll(int i, KeyEvent keyEvent);

    boolean onUndo(int i, KeyEvent keyEvent);
}
