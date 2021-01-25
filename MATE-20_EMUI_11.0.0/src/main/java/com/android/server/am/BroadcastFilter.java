package com.android.server.am;

import android.content.IntentFilter;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class BroadcastFilter extends IntentFilter {
    final boolean instantApp;
    final int owningUid;
    final int owningUserId;
    final String packageName;
    final ReceiverList receiverList;
    final String requiredPermission;
    final boolean visibleToInstantApp;

    BroadcastFilter(IntentFilter _filter, ReceiverList _receiverList, String _packageName, String _requiredPermission, int _owningUid, int _userId, boolean _instantApp, boolean _visibleToInstantApp) {
        super(_filter);
        this.receiverList = _receiverList;
        this.packageName = _packageName;
        this.requiredPermission = _requiredPermission;
        this.owningUid = _owningUid;
        this.owningUserId = _userId;
        this.instantApp = _instantApp;
        this.visibleToInstantApp = _visibleToInstantApp;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L);
        String str = this.requiredPermission;
        if (str != null) {
            proto.write(1138166333442L, str);
        }
        proto.write(1138166333443L, Integer.toHexString(System.identityHashCode(this)));
        proto.write(1120986464260L, this.owningUserId);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix) {
        dumpInReceiverList(pw, new PrintWriterPrinter(pw), prefix);
        this.receiverList.dumpLocal(pw, prefix);
    }

    public void dumpBrief(PrintWriter pw, String prefix) {
        dumpBroadcastFilterState(pw, prefix);
    }

    public void dumpInReceiverList(PrintWriter pw, Printer pr, String prefix) {
        super.dump(pr, prefix);
        dumpBroadcastFilterState(pw, prefix);
    }

    /* access modifiers changed from: package-private */
    public void dumpBroadcastFilterState(PrintWriter pw, String prefix) {
        if (this.requiredPermission != null) {
            pw.print(prefix);
            pw.print("requiredPermission=");
            pw.println(this.requiredPermission);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "BroadcastFilter{" + Integer.toHexString(System.identityHashCode(this)) + ' ' + this.owningUid + "/u" + this.owningUserId + ' ' + this.receiverList + '}';
    }
}
