package com.android.server.am;

import android.content.IIntentReceiver;
import android.os.Binder;
import android.os.IBinder.DeathRecipient;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import java.io.PrintWriter;
import java.util.ArrayList;

final class ReceiverList extends ArrayList<BroadcastFilter> implements DeathRecipient {
    public final ProcessRecord app;
    BroadcastRecord curBroadcast = null;
    boolean linkedToDeath = false;
    final ActivityManagerService owner;
    public final int pid;
    public final IIntentReceiver receiver;
    String stringName;
    public final int uid;
    public final int userId;

    ReceiverList(ActivityManagerService _owner, ProcessRecord _app, int _pid, int _uid, int _userId, IIntentReceiver _receiver) {
        this.owner = _owner;
        this.receiver = _receiver;
        this.app = _app;
        this.pid = _pid;
        this.uid = _uid;
        this.userId = _userId;
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }

    public void binderDied() {
        this.linkedToDeath = false;
        this.owner.unregisterReceiver(this.receiver);
    }

    void dumpLocal(PrintWriter pw, String prefix) {
        String str = null;
        pw.print(prefix);
        pw.print("app=");
        if (this.app != null) {
            str = this.app.toShortString();
        }
        pw.print(str);
        pw.print(" pid=");
        pw.print(this.pid);
        pw.print(" uid=");
        pw.print(this.uid);
        pw.print(" user=");
        pw.println(this.userId);
        if (this.curBroadcast != null || this.linkedToDeath) {
            pw.print(prefix);
            pw.print("curBroadcast=");
            pw.print(this.curBroadcast);
            pw.print(" linkedToDeath=");
            pw.println(this.linkedToDeath);
        }
    }

    void dump(PrintWriter pw, String prefix) {
        Printer pr = new PrintWriterPrinter(pw);
        dumpLocal(pw, prefix);
        String p2 = prefix + "  ";
        int N = size();
        for (int i = 0; i < N; i++) {
            BroadcastFilter bf = (BroadcastFilter) get(i);
            pw.print(prefix);
            pw.print("Filter #");
            pw.print(i);
            pw.print(": BroadcastFilter{");
            pw.print(Integer.toHexString(System.identityHashCode(bf)));
            pw.println('}');
            bf.dumpInReceiverList(pw, pr, p2);
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ReceiverList{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.pid);
        sb.append(' ');
        sb.append(this.app != null ? this.app.processName : "(unknown name)");
        sb.append('/');
        sb.append(this.uid);
        sb.append("/u");
        sb.append(this.userId);
        sb.append(this.receiver.asBinder() instanceof Binder ? " local:" : " remote:");
        sb.append(Integer.toHexString(System.identityHashCode(this.receiver.asBinder())));
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
