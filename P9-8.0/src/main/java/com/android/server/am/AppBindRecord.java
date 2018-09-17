package com.android.server.am;

import android.util.ArraySet;
import java.io.PrintWriter;

public final class AppBindRecord {
    public final ProcessRecord client;
    final ArraySet<ConnectionRecord> connections = new ArraySet();
    final IntentBindRecord intent;
    public final ServiceRecord service;

    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "service=" + this.service);
        pw.println(prefix + "client=" + this.client);
        dumpInIntentBind(pw, prefix);
    }

    void dumpInIntentBind(PrintWriter pw, String prefix) {
        int N = this.connections.size();
        if (N > 0) {
            pw.println(prefix + "Per-process Connections:");
            for (int i = 0; i < N; i++) {
                pw.println(prefix + "  " + ((ConnectionRecord) this.connections.valueAt(i)));
            }
        }
    }

    AppBindRecord(ServiceRecord _service, IntentBindRecord _intent, ProcessRecord _client) {
        this.service = _service;
        this.intent = _intent;
        this.client = _client;
    }

    public String toString() {
        return "AppBindRecord{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.service.shortName + ":" + this.client.processName + "}";
    }
}
