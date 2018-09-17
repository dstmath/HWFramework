package com.google.android.collect;

import java.util.ArrayList;
import java.util.Collections;

public class Lists {
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList();
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList(((elements.length * 110) / 100) + 5);
        Collections.addAll(list, elements);
        return list;
    }
}
