package com.android.server.am;

import android.content.IIntentReceiver;
import android.content.IIntentSender.Stub;
import android.content.Intent;
import android.os.Bundle;
import android.util.TimeUtils;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

final class PendingIntentRecord extends Stub {
    private static final String TAG = null;
    boolean canceled;
    final Key key;
    String lastTag;
    String lastTagPrefix;
    final ActivityManagerService owner;
    final WeakReference<PendingIntentRecord> ref;
    boolean sent;
    String stringName;
    final int uid;
    private long whitelistDuration;

    static final class Key {
        private static final int ODD_PRIME_NUMBER = 37;
        final ActivityRecord activity;
        Intent[] allIntents;
        String[] allResolvedTypes;
        final int flags;
        final int hashCode;
        final Bundle options;
        final String packageName;
        final int requestCode;
        final Intent requestIntent;
        final String requestResolvedType;
        final int type;
        final int userId;
        final String who;

        Key(int _t, String _p, ActivityRecord _a, String _w, int _r, Intent[] _i, String[] _it, int _f, Bundle _o, int _userId) {
            Intent intent;
            String str = null;
            this.type = _t;
            this.packageName = _p;
            this.activity = _a;
            this.who = _w;
            this.requestCode = _r;
            if (_i != null) {
                intent = _i[_i.length - 1];
            } else {
                intent = null;
            }
            this.requestIntent = intent;
            if (_it != null) {
                str = _it[_it.length - 1];
            }
            this.requestResolvedType = str;
            this.allIntents = _i;
            this.allResolvedTypes = _it;
            this.flags = _f;
            this.options = _o;
            this.userId = _userId;
            int hash = ((((_f + VoldResponseCode.VOLUME_LOW_SPEED_SD) * ODD_PRIME_NUMBER) + _r) * ODD_PRIME_NUMBER) + _userId;
            if (_w != null) {
                hash = (hash * ODD_PRIME_NUMBER) + _w.hashCode();
            }
            if (_a != null) {
                hash = (hash * ODD_PRIME_NUMBER) + _a.hashCode();
            }
            if (this.requestIntent != null) {
                hash = (hash * ODD_PRIME_NUMBER) + this.requestIntent.filterHashCode();
            }
            if (this.requestResolvedType != null) {
                hash = (hash * ODD_PRIME_NUMBER) + this.requestResolvedType.hashCode();
            }
            this.hashCode = (((hash * ODD_PRIME_NUMBER) + _p.hashCode()) * ODD_PRIME_NUMBER) + _t;
        }

        public boolean equals(Object otherObj) {
            if (otherObj == null) {
                return false;
            }
            try {
                Key other = (Key) otherObj;
                if (this.type != other.type || this.userId != other.userId || !this.packageName.equals(other.packageName) || this.activity != other.activity) {
                    return false;
                }
                if (this.who != other.who) {
                    if (this.who != null) {
                        if (!this.who.equals(other.who)) {
                            return false;
                        }
                    } else if (other.who != null) {
                        return false;
                    }
                }
                if (this.requestCode != other.requestCode) {
                    return false;
                }
                if (this.requestIntent != other.requestIntent) {
                    if (this.requestIntent != null) {
                        if (!(this.requestIntent.filterEquals(other.requestIntent) && (this.requestIntent.getHwFlags() & 1) == (other.requestIntent.getHwFlags() & 1))) {
                            return false;
                        }
                    } else if (other.requestIntent != null) {
                        return false;
                    }
                }
                if (this.requestResolvedType != other.requestResolvedType) {
                    if (this.requestResolvedType != null) {
                        if (!this.requestResolvedType.equals(other.requestResolvedType)) {
                            return false;
                        }
                    } else if (other.requestResolvedType != null) {
                        return false;
                    }
                }
                if (this.flags != other.flags) {
                    return false;
                }
                return true;
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String toString() {
            return "Key{" + typeName() + " pkg=" + this.packageName + " intent=" + (this.requestIntent != null ? this.requestIntent.toShortString(true, true, false, false) : "<null>") + " flags=0x" + Integer.toHexString(this.flags) + " u=" + this.userId + "}";
        }

        String typeName() {
            switch (this.type) {
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    return "broadcastIntent";
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    return "startActivity";
                case H.REPORT_LOSING_FOCUS /*3*/:
                    return "activityResult";
                case H.DO_TRAVERSAL /*4*/:
                    return "startService";
                default:
                    return Integer.toString(this.type);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.PendingIntentRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.PendingIntentRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.PendingIntentRecord.<clinit>():void");
    }

    int sendInner(int r1, android.content.Intent r2, java.lang.String r3, android.content.IIntentReceiver r4, java.lang.String r5, android.os.IBinder r6, java.lang.String r7, int r8, int r9, int r10, android.os.Bundle r11, android.app.IActivityContainer r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.PendingIntentRecord.sendInner(int, android.content.Intent, java.lang.String, android.content.IIntentReceiver, java.lang.String, android.os.IBinder, java.lang.String, int, int, int, android.os.Bundle, android.app.IActivityContainer):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.PendingIntentRecord.sendInner(int, android.content.Intent, java.lang.String, android.content.IIntentReceiver, java.lang.String, android.os.IBinder, java.lang.String, int, int, int, android.os.Bundle, android.app.IActivityContainer):int");
    }

    PendingIntentRecord(ActivityManagerService _owner, Key _k, int _u) {
        this.sent = false;
        this.canceled = false;
        this.whitelistDuration = 0;
        this.owner = _owner;
        this.key = _k;
        this.uid = _u;
        this.ref = new WeakReference(this);
    }

    void setWhitelistDuration(long duration) {
        this.whitelistDuration = duration;
        this.stringName = null;
    }

    public void send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        sendInner(code, intent, resolvedType, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options, null);
    }

    public int sendWithResult(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        return sendInner(code, intent, resolvedType, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options, null);
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.canceled) {
                this.owner.mHandler.sendMessage(this.owner.mHandler.obtainMessage(23, this));
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void completeFinalize() {
        synchronized (this.owner) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (((WeakReference) this.owner.mIntentSenderRecords.get(this.key)) == this.ref) {
                    this.owner.mIntentSenderRecords.remove(this.key);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("uid=");
        pw.print(this.uid);
        pw.print(" packageName=");
        pw.print(this.key.packageName);
        pw.print(" type=");
        pw.print(this.key.typeName());
        pw.print(" flags=0x");
        pw.println(Integer.toHexString(this.key.flags));
        if (!(this.key.activity == null && this.key.who == null)) {
            pw.print(prefix);
            pw.print("activity=");
            pw.print(this.key.activity);
            pw.print(" who=");
            pw.println(this.key.who);
        }
        if (!(this.key.requestCode == 0 && this.key.requestResolvedType == null)) {
            pw.print(prefix);
            pw.print("requestCode=");
            pw.print(this.key.requestCode);
            pw.print(" requestResolvedType=");
            pw.println(this.key.requestResolvedType);
        }
        if (this.key.requestIntent != null) {
            pw.print(prefix);
            pw.print("requestIntent=");
            pw.println(this.key.requestIntent.toShortString(true, true, true, true));
        }
        if (this.sent || this.canceled) {
            pw.print(prefix);
            pw.print("sent=");
            pw.print(this.sent);
            pw.print(" canceled=");
            pw.println(this.canceled);
        }
        if (this.whitelistDuration != 0) {
            pw.print(prefix);
            pw.print("whitelistDuration=");
            TimeUtils.formatDuration(this.whitelistDuration, pw);
            pw.println();
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
        sb.append("PendingIntentRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.key.packageName);
        sb.append(' ');
        sb.append(this.key.typeName());
        if (this.whitelistDuration > 0) {
            sb.append(" (whitelist: ");
            TimeUtils.formatDuration(this.whitelistDuration, sb);
            sb.append(")");
        }
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
