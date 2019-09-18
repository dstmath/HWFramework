package com.android.server.am;

import android.app.IServiceConnection;
import android.app.PendingIntent;
import android.util.proto.ProtoOutputStream;
import android.util.proto.ProtoUtils;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;

final class ConnectionRecord {
    private static int[] BIND_ORIG_ENUMS = {1, 2, 4, DumpState.DUMP_VOLUMES, 8, 16, 32, 64, 128, DumpState.DUMP_HANDLE, 67108864, 134217728, 268435456, 536870912, 1073741824};
    private static int[] BIND_PROTO_ENUMS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    final ActivityRecord activity;
    final AppBindRecord binding;
    final PendingIntent clientIntent;
    final int clientLabel;
    final IServiceConnection conn;
    final int flags;
    boolean serviceDead;
    String stringName;

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
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
        if ((this.flags & DumpState.DUMP_VOLUMES) != 0) {
            sb.append("IMPB ");
        }
        if ((this.flags & 8) != 0) {
            sb.append("ABCLT ");
        }
        if ((this.flags & 16) != 0) {
            sb.append("OOM ");
        }
        if ((32 & this.flags) != 0) {
            sb.append("WPRI ");
        }
        if ((this.flags & 64) != 0) {
            sb.append("IMP ");
        }
        if ((128 & this.flags) != 0) {
            sb.append("WACT ");
        }
        if ((this.flags & DumpState.DUMP_HANDLE) != 0) {
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
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        if (this.binding != null) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, Integer.toHexString(System.identityHashCode(this)));
            if (this.binding.client != null) {
                proto.write(1120986464258L, this.binding.client.userId);
            }
            ProtoUtils.writeBitWiseFlagsToProtoEnum(proto, 2259152797699L, this.flags, BIND_ORIG_ENUMS, BIND_PROTO_ENUMS);
            if (this.serviceDead) {
                proto.write(2259152797699L, 15);
            }
            if (this.binding.service != null) {
                proto.write(1138166333444L, this.binding.service.shortName);
            }
            proto.end(token);
        }
    }
}
