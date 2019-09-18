package com.android.server.security.panpay.blackbox;

import android.text.TextUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PanPayBlackBox {
    private static PanPayBlackBox mSelf;
    private HashMap<String, ArrayList<String>> box = new HashMap<>();

    private PanPayBlackBox() {
    }

    public static PanPayBlackBox getInstance() {
        if (mSelf != null) {
            return mSelf;
        }
        mSelf = new PanPayBlackBox();
        return mSelf;
    }

    public ArrayList<String> getLastInfo(String packageName) {
        return this.box.get(packageName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0042, code lost:
        return;
     */
    public void appendInfo(String packageName, String info) {
        synchronized (mSelf) {
            if (!TextUtils.isEmpty(info)) {
                String info2 = getCurTime() + "--> " + info;
                ArrayList<String> infoList = this.box.computeIfAbsent(packageName, $$Lambda$PanPayBlackBox$gac9GYdScY1H4JXql__o52EPDk.INSTANCE);
                if (infoList != null) {
                    infoList.add(info2);
                    if (infoList.size() > 32) {
                        infoList.remove(0);
                    }
                }
            }
        }
    }

    static /* synthetic */ ArrayList lambda$appendInfo$0(String k) {
        return new ArrayList();
    }

    private String getCurTime() {
        return new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date(System.currentTimeMillis()));
    }
}
