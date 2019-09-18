package defpackage;

import android.content.Context;

/* renamed from: be  reason: default package */
public final class be extends bd {
    private static be as = null;

    private be(Context context) {
        super(context, "pushConfig");
        l();
    }

    public static synchronized be a(Context context) {
        be beVar;
        synchronized (be.class) {
            if (as != null) {
                beVar = as;
            } else {
                beVar = new be(context);
                as = beVar;
            }
        }
        return beVar;
    }
}
