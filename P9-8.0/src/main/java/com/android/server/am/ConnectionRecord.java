package com.android.server.am;

import android.app.IServiceConnection;
import android.app.PendingIntent;
import java.io.PrintWriter;

final class ConnectionRecord {
    final ActivityRecord activity;
    final AppBindRecord binding;
    final PendingIntent clientIntent;
    final int clientLabel;
    final IServiceConnection conn;
    final int flags;
    boolean serviceDead;
    String stringName;

    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "binding=" + this.binding);
        if (this.activity != null) {
            pw.println(prefix + "activity=" + this.activity);
        }
        pw.println(prefix + "conn=" + this.conn.asBinder() + " flags=0x" + Integer.toHexString(this.flags));
    }

    ConnectionRecord(AppBindRecord _binding, ActivityRecord _activity, IServiceConnection _conn, int _flags, int _clientLabel, PendingIntent _clientIntent) {
        this.binding = _binding;
        this.activity = _activity;
        this.conn = _conn;
        this.flags = _flags;
        this.clientLabel = _clientLabel;
        this.clientIntent = _clientIntent;
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ConnectionRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" u");
        sb.append(this.binding.client.userId);
        sb.append(' ');
        if ((this.flags & 1) != 0) {
            sb.append("CR ");
        }
        if ((this.flags & 2) != 0) {
            sb.append("DBG ");
        }
        if ((this.flags & 4) != 0) {
            sb.append("!FG ");
        }
        if ((this.flags & 8388608) != 0) {
            sb.append("IMPB ");
        }
        if ((this.flags & 8) != 0) {
            sb.append("ABCLT ");
        }
        if ((this.flags & 16) != 0) {
            sb.append("OOM ");
        }
        if ((this.flags & 32) != 0) {
            sb.append("WPRI ");
        }
        if ((this.flags & 64) != 0) {
            sb.append("IMP ");
        }
        if ((this.flags & 128) != 0) {
            sb.append("WACT ");
        }
        if ((this.flags & 33554432) != 0) {
            sb.append("FGSA ");
        }
        if ((this.flags & 67108864) != 0) {
            sb.append("FGS ");
        }
        if ((this.flags & 134217728) != 0) {
            sb.append("LACT ");
        }
        if ((this.flags & 268435456) != 0) {
            sb.append("VIS ");
        }
        if ((this.flags & 536870912) != 0) {
            sb.append("UI ");
        }
        if ((this.flags & 1073741824) != 0) {
            sb.append("!VIS ");
        }
        if (this.serviceDead) {
            sb.append("DEAD ");
        }
        sb.append(this.binding.service.shortName);
        sb.append(":@");
        sb.append(Integer.toHexString(System.identityHashCode(this.conn.asBinder())));
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
