package com.android.server.am;

import android.content.IntentFilter;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import java.io.PrintWriter;

final class BroadcastFilter extends IntentFilter {
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

    void dumpBroadcastFilterState(PrintWriter pw, String prefix) {
        if (this.requiredPermission != null) {
            pw.print(prefix);
            pw.print("requiredPermission=");
            pw.println(this.requiredPermission);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BroadcastFilter{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" u");
        sb.append(this.owningUserId);
        sb.append(' ');
        sb.append(this.receiverList);
        sb.append('}');
        return sb.toString();
    }
}
