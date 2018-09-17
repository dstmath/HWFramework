package com.android.server.am;

import android.content.Intent.FilterComparison;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.ArraySet;
import java.io.PrintWriter;

final class IntentBindRecord {
    final ArrayMap<ProcessRecord, AppBindRecord> apps = new ArrayMap();
    IBinder binder;
    boolean doRebind;
    boolean hasBound;
    final FilterComparison intent;
    boolean received;
    boolean requested;
    final ServiceRecord service;
    String stringName;

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("service=");
        pw.println(this.service);
        dumpInService(pw, prefix);
    }

    void dumpInService(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("intent={");
        pw.print(this.intent.getIntent().toShortString(true, true, false, false));
        pw.println('}');
        pw.print(prefix);
        pw.print("binder=");
        pw.println(this.binder);
        pw.print(prefix);
        pw.print("requested=");
        pw.print(this.requested);
        pw.print(" received=");
        pw.print(this.received);
        pw.print(" hasBound=");
        pw.print(this.hasBound);
        pw.print(" doRebind=");
        pw.println(this.doRebind);
        for (int i = 0; i < this.apps.size(); i++) {
            AppBindRecord a = (AppBindRecord) this.apps.valueAt(i);
            pw.print(prefix);
            pw.print("* Client AppBindRecord{");
            pw.print(Integer.toHexString(System.identityHashCode(a)));
            pw.print(' ');
            pw.print(a.client);
            pw.println('}');
            a.dumpInIntentBind(pw, prefix + "  ");
        }
    }

    IntentBindRecord(ServiceRecord _service, FilterComparison _intent) {
        this.service = _service;
        this.intent = _intent;
    }

    int collectFlags() {
        int flags = 0;
        for (int i = this.apps.size() - 1; i >= 0; i--) {
            ArraySet<ConnectionRecord> connections = ((AppBindRecord) this.apps.valueAt(i)).connections;
            for (int j = connections.size() - 1; j >= 0; j--) {
                flags |= ((ConnectionRecord) connections.valueAt(j)).flags;
            }
        }
        return flags;
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentBindRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        if ((collectFlags() & 1) != 0) {
            sb.append("CR ");
        }
        sb.append(this.service.shortName);
        sb.append(':');
        if (this.intent != null) {
            this.intent.getIntent().toShortString(sb, true, false, false, false);
        }
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
