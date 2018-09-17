package com.huawei.android.pushagent.utils.e;

import android.net.Uri;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class d {
    String ge;
    String gf;
    final List<String> gg = new ArrayList();
    int gh;
    List<String> gi = new ArrayList();
    String gj;

    public d sz(String str) {
        if (TextUtils.isEmpty(str)) {
            return this;
        }
        Uri parse = Uri.parse(str);
        this.gj = parse.getScheme();
        this.gf = parse.getHost();
        this.gh = parse.getPort();
        Collection pathSegments = parse.getPathSegments();
        if (pathSegments != null) {
            this.gg.addAll(pathSegments);
        }
        Object query = parse.getQuery();
        if (!TextUtils.isEmpty(query)) {
            String[] split = query.split("&");
            for (Object add : split) {
                this.gi.add(add);
            }
        }
        this.ge = parse.getFragment();
        return this;
    }

    public d tb(int i) {
        if (i != 0) {
            this.gh = i;
        }
        return this;
    }

    public d ta(List<String> list) {
        if (list != null) {
            this.gi.addAll(list);
        }
        return this;
    }

    public c tc() {
        return new c(this);
    }
}
