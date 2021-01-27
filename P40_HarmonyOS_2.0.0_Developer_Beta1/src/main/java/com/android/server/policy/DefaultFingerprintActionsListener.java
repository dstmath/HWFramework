package com.android.server.policy;

import android.content.Context;
import android.view.MotionEvent;
import com.android.server.am.PointerEventListenerEx;

public class DefaultFingerprintActionsListener extends PointerEventListenerEx {
    public DefaultFingerprintActionsListener(Context context, PhoneWindowManagerEx policy) {
    }

    public void setCurrentUser(int newUserId) {
    }

    public void createSearchPanelView() {
    }

    public void destroySearchPanelView() {
    }

    public void createMultiWinArrowView() {
    }

    public void destroyMultiWinArrowView() {
    }

    @Override // com.android.server.am.PointerEventListenerEx
    public void onPointerEvent(MotionEvent motionEvent) {
    }
}
