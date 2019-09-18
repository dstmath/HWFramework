package com.android.server.am;

import android.content.Intent;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedRef;

final class IntentBindRecord {
    final ArrayMap<ProcessRecord, AppBindRecord> apps = new ArrayMap<>();
    IBinder binder;
    boolean doRebind;
    boolean hasBound;
    final Intent.FilterComparison intent;
    boolean received;
    boolean requested;
    @RCUnownedRef
    final ServiceRecord service;
    String stringName;

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("service=");
        pw.println(this.service);
        dumpInService(pw, prefix);
    }

    /* access modifiers changed from: package-private */
    public void dumpInService(PrintWriter pw, String prefix) {
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
            AppBindRecord a = this.apps.valueAt(i);
            pw.print(prefix);
            pw.print("* Client AppBindRecord{");
            pw.print(Integer.toHexString(System.identityHashCode(a)));
            pw.print(' ');
            pw.print(a.client);
            pw.println('}');
            a.dumpInIntentBind(pw, prefix + "  ");
        }
    }

    IntentBindRecord(ServiceRecord _service, Intent.FilterComparison _intent) {
        this.service = _service;
        this.intent = _intent;
    }

    /* access modifiers changed from: package-private */
    public int collectFlags() {
        int flags = 0;
        for (int i = this.apps.size() - 1; i >= 0; i--) {
            ArraySet<ConnectionRecord> connections = this.apps.valueAt(i).connections;
            for (int j = connections.size() - 1; j >= 0; j--) {
                flags |= connections.valueAt(j).flags;
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
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        if (this.intent != null) {
            this.intent.getIntent().writeToProto(proto, 1146756268033L, false, true, false, false);
        }
        if (this.binder != null) {
            proto.write(1138166333442L, this.binder.toString());
        }
        boolean z = true;
        int i = 0;
        if ((collectFlags() & 1) == 0) {
            z = false;
        }
        proto.write(1133871366147L, z);
        proto.write(1133871366148L, this.requested);
        proto.write(1133871366149L, this.received);
        proto.write(1133871366150L, this.hasBound);
        proto.write(1133871366151L, this.doRebind);
        int N = this.apps.size();
        while (true) {
            int i2 = i;
            if (i2 < N) {
                AppBindRecord a = this.apps.valueAt(i2);
                if (a != null) {
                    a.writeToProto(proto, 2246267895816L);
                }
                i = i2 + 1;
            } else {
                proto.end(token);
                return;
            }
        }
    }
}
