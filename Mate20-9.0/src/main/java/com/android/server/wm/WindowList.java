package com.android.server.wm;

import java.util.ArrayList;

class WindowList<E> extends ArrayList<E> {
    WindowList() {
    }

    /* access modifiers changed from: package-private */
    public void addFirst(E e) {
        add(0, e);
    }

    /* access modifiers changed from: package-private */
    public E peekLast() {
        if (size() > 0) {
            return get(size() - 1);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public E peekFirst() {
        if (size() > 0) {
            return get(0);
        }
        return null;
    }
}
