package com.android.server.am;

import android.content.IIntentReceiver;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.proto.ProtoOutputStream;
import com.android.server.IntentResolver;
import java.io.PrintWriter;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public final class ReceiverList extends ArrayList<BroadcastFilter> implements IBinder.DeathRecipient {
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

    @Override // java.util.List, java.util.Collection, java.util.AbstractList, java.lang.Object
    public boolean equals(Object o) {
        return this == o;
    }

    @Override // java.util.List, java.util.Collection, java.util.AbstractList, java.lang.Object
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        this.linkedToDeath = false;
        this.owner.unregisterReceiver(this.receiver);
    }

    public boolean containsFilter(IntentFilter filter) {
        int N = size();
        for (int i = 0; i < N; i++) {
            if (IntentResolver.filterEquals((BroadcastFilter) get(i), filter)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        this.app.writeToProto(proto, 1146756268033L);
        proto.write(1120986464258L, this.pid);
        proto.write(1120986464259L, this.uid);
        proto.write(1120986464260L, this.userId);
        BroadcastRecord broadcastRecord = this.curBroadcast;
        if (broadcastRecord != null) {
            broadcastRecord.writeToProto(proto, 1146756268037L);
        }
        proto.write(1133871366150L, this.linkedToDeath);
        int N = size();
        for (int i = 0; i < N; i++) {
            ((BroadcastFilter) get(i)).writeToProto(proto, 2246267895815L);
        }
        proto.write(1138166333448L, Integer.toHexString(System.identityHashCode(this)));
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void dumpLocal(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("app=");
        ProcessRecord processRecord = this.app;
        pw.print(processRecord != null ? processRecord.toShortString() : null);
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

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
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

    @Override // java.util.AbstractCollection, java.lang.Object
    public String toString() {
        String str = this.stringName;
        if (str != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ReceiverList{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.pid);
        sb.append(' ');
        ProcessRecord processRecord = this.app;
        sb.append(processRecord != null ? processRecord.processName : "(unknown name)");
        sb.append('/');
        sb.append(this.uid);
        sb.append("/u");
        sb.append(this.userId);
        sb.append(this.receiver.asBinder() instanceof Binder ? " local:" : " remote:");
        sb.append(Integer.toHexString(System.identityHashCode(this.receiver.asBinder())));
        sb.append('}');
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }
}
