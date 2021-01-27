package com.android.server.am;

import android.util.ArraySet;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class AppBindRecord {
    final ProcessRecord client;
    final ArraySet<ConnectionRecord> connections = new ArraySet<>();
    final IntentBindRecord intent;
    final ServiceRecord service;

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "service=" + this.service);
        pw.println(prefix + "client=" + this.client);
        dumpInIntentBind(pw, prefix);
    }

    /* access modifiers changed from: package-private */
    public void dumpInIntentBind(PrintWriter pw, String prefix) {
        int N = this.connections.size();
        if (N > 0) {
            pw.println(prefix + "Per-process Connections:");
            for (int i = 0; i < N; i++) {
                pw.println(prefix + "  " + this.connections.valueAt(i));
            }
        }
    }

    AppBindRecord(ServiceRecord _service, IntentBindRecord _intent, ProcessRecord _client) {
        this.service = _service;
        this.intent = _intent;
        this.client = _client;
    }

    public String toString() {
        return "AppBindRecord{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.service.shortInstanceName + ":" + this.client.processName + "}";
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.service.shortInstanceName);
        proto.write(1138166333442L, this.client.processName);
        int N = this.connections.size();
        for (int i = 0; i < N; i++) {
            proto.write(2237677961219L, Integer.toHexString(System.identityHashCode(this.connections.valueAt(i))));
        }
        proto.end(token);
    }
}
