package com.android.internal.util;

import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class LocalLog {
    private final ArrayList<String> mLines = new ArrayList<>(20);
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
                pw.println(this.mLines.get(i));
            }
            return true;
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        synchronized (this.mLines) {
            for (int i = 0; i < this.mLines.size(); i++) {
                proto.write(2237677961217L, this.mLines.get(i));
            }
        }
        proto.end(token);
    }
}
