package com.google.android.collect;

import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.Collections;

public class Lists {
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList();
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList(((elements.length * LogPower.ALL_DOWNLOAD_FINISH) / 100) + 5);
        Collections.addAll(list, elements);
        return list;
    }
}
