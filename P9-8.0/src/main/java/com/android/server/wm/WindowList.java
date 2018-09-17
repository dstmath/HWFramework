package com.android.server.wm;

import java.util.ArrayList;

class WindowList<E> extends ArrayList<E> {
    WindowList() {
    }

    void addFirst(E e) {
        add(0, e);
    }

    E peekLast() {
        return size() > 0 ? get(size() - 1) : null;
    }

    E peekFirst() {
        return size() > 0 ? get(0) : null;
    }
}
