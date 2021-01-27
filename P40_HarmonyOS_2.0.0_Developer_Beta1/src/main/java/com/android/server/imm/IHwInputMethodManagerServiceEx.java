package com.android.server.imm;

import android.content.Context;
import android.view.inputmethod.CursorAnchorInfo;
import com.huawei.android.inputmethod.IHwInputContentListener;
import com.huawei.android.inputmethod.IHwInputMethodListener;

public interface IHwInputMethodManagerServiceEx {
    void handleChangeInputMsg(int i);

    boolean isTriNavigationBar(Context context);

    boolean isTvMode();

    void onContentChanged(String str);

    void onFinishInput();

    void onReceivedComposingText(String str);

    void onReceivedInputContent(String str);

    void onShowInputRequested();

    void onStartInput();

    void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo);

    void raiseIawarePriority();

    void recoveryIawarePriority();

    void registerInputContentListener(IHwInputContentListener iHwInputContentListener);

    void registerInputMethodListener(IHwInputMethodListener iHwInputMethodListener);

    int sendEventData(int i, String str);

    void unregisterInputContentListener();

    void unregisterInputMethodListener();
}
