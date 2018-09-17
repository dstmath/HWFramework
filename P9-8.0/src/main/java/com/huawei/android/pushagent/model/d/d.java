package com.huawei.android.pushagent.model.d;

import java.util.ArrayList;
import java.util.List;

public class d {
    private static List<String> cr = new ArrayList();
    private static d cs = new d();
    private final Object cq = new Object();

    private d() {
    }

    public static d kn() {
        return cs;
    }

    public void km(String str) {
        synchronized (this.cq) {
            if (cr.size() >= 50) {
                cr.remove(0);
            }
            cr.add(str);
        }
    }

    public boolean ko(String str) {
        boolean contains;
        synchronized (this.cq) {
            contains = cr.contains(str);
        }
        return contains;
    }
}
