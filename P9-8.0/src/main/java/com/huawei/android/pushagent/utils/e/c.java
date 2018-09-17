package com.huawei.android.pushagent.utils.e;

import android.text.TextUtils;
import java.util.List;

public class c {
    final String fy;
    final String fz;
    final List<String> ga;
    final int gb;
    final List<String> gc;
    final String gd;

    c(d dVar) {
        this.gd = dVar.gj;
        this.fz = dVar.gf;
        this.gb = dVar.gh;
        this.ga = dVar.gg;
        this.gc = dVar.gi;
        this.fy = dVar.ge;
    }

    public String sx() {
        int i;
        int i2 = 0;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.gd).append("://").append(this.fz);
        if (this.gb > 0) {
            stringBuilder.append(':').append(this.gb);
        }
        stringBuilder.append('/');
        if (this.ga != null) {
            int size = this.ga.size();
            for (i = 0; i < size; i++) {
                stringBuilder.append((String) this.ga.get(i)).append('/');
            }
        }
        sy(stringBuilder, '/');
        if (this.gc != null) {
            i = this.gc.size();
            if (i > 0) {
                stringBuilder.append('?');
                while (i2 < i) {
                    stringBuilder.append((String) this.gc.get(i2)).append('&');
                    i2++;
                }
                sy(stringBuilder, '&');
            }
        }
        if (!TextUtils.isEmpty(this.fy)) {
            stringBuilder.append('#').append(this.fy);
        }
        return stringBuilder.toString();
    }

    private static void sy(StringBuilder stringBuilder, char c) {
        if (stringBuilder != null && stringBuilder.lastIndexOf(String.valueOf(c)) == stringBuilder.length() - 1) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
    }
}
