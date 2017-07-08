package android.view.inputmethod;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public interface InputMethodSession {

    public interface EventCallback {
        void finishedEvent(int i, boolean z);
    }

    void appPrivateCommand(String str, Bundle bundle);

    void dispatchGenericMotionEvent(int i, MotionEvent motionEvent, EventCallback eventCallback);

    void dispatchKeyEvent(int i, KeyEvent keyEvent, EventCallback eventCallback);

    void dispatchTrackballEvent(int i, MotionEvent motionEvent, EventCallback eventCallback);

    void displayCompletions(CompletionInfo[] completionInfoArr);

    void finishInput();

    void toggleSoftInput(int i, int i2);

    void updateCursor(Rect rect);

    void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo);

    void updateExtractedText(int i, ExtractedText extractedText);

    void updateSelection(int i, int i2, int i3, int i4, int i5, int i6);

    void viewClicked(boolean z);
}
