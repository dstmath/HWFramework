package com.android.internal.util;

import android.util.Slog;
import java.io.PrintWriter;
import java.util.ArrayList;

public class LocalLog {
    private final ArrayList<String> mLines = new ArrayList(20);
    private final int mMaxLines = 20;
    private final String mTag;

    public LocalLog(String tag) {
        this.mTag = tag;
    }

    public void w(String msg) {
        synchronized (this.mLines) {
            Slog.w(this.mTag, msg);
            if (this.mLines.size() >= 20) {
                this.mLines.remove(0);
            }
            this.mLines.add(msg);
        }
    }

    public boolean dump(PrintWriter pw, String header, String prefix) {
        synchronized (this.mLines) {
            if (this.mLines.size() <= 0) {
                return false;
            }
            if (header != null) {
                pw.println(header);
            }
            for (int i = 0; i < this.mLines.size(); i++) {
                if (prefix != null) {
                    pw.print(prefix);
                }
                pw.println((String) this.mLines.get(i));
            }
            return true;
        }
    }
}
