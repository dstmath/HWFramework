package ohos.org.w3c.dom.events;

import ohos.org.w3c.dom.views.AbstractView;

public interface MouseEvent extends UIEvent {
    boolean getAltKey();

    short getButton();

    int getClientX();

    int getClientY();

    boolean getCtrlKey();

    boolean getMetaKey();

    EventTarget getRelatedTarget();

    int getScreenX();

    int getScreenY();

    boolean getShiftKey();

    void initMouseEvent(String str, boolean z, boolean z2, AbstractView abstractView, int i, int i2, int i3, int i4, int i5, boolean z3, boolean z4, boolean z5, boolean z6, short s, EventTarget eventTarget);
}
