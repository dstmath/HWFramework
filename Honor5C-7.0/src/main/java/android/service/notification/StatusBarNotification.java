package android.service.notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.UserHandle;

public class StatusBarNotification implements Parcelable {
    public static final Creator<StatusBarNotification> CREATOR = null;
    private String groupKey;
    private final int id;
    private final int initialPid;
    private final String key;
    private Context mContext;
    private final Notification notification;
    private final String opPkg;
    private String overrideGroupKey;
    private final String pkg;
    private final long postTime;
    private final String tag;
    private final int uid;
    private final UserHandle user;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.notification.StatusBarNotification.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.notification.StatusBarNotification.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.service.notification.StatusBarNotification.<clinit>():void");
    }

    public StatusBarNotification(String pkg, String opPkg, int id, String tag, int uid, int initialPid, int score, Notification notification, UserHandle user) {
        this(pkg, opPkg, id, tag, uid, initialPid, score, notification, user, System.currentTimeMillis());
    }

    public StatusBarNotification(String pkg, String opPkg, int id, String tag, int uid, int initialPid, Notification notification, UserHandle user, String overrideGroupKey, long postTime) {
        if (pkg == null) {
            throw new NullPointerException();
        } else if (notification == null) {
            throw new NullPointerException();
        } else {
            this.pkg = pkg;
            this.opPkg = opPkg;
            this.id = id;
            this.tag = tag;
            this.uid = uid;
            this.initialPid = initialPid;
            this.notification = notification;
            this.user = user;
            this.postTime = postTime;
            this.overrideGroupKey = overrideGroupKey;
            this.key = key();
            this.groupKey = groupKey();
        }
    }

    public StatusBarNotification(String pkg, String opPkg, int id, String tag, int uid, int initialPid, int score, Notification notification, UserHandle user, long postTime) {
        if (pkg == null) {
            throw new NullPointerException();
        } else if (notification == null) {
            throw new NullPointerException();
        } else {
            this.pkg = pkg;
            this.opPkg = opPkg;
            this.id = id;
            this.tag = tag;
            this.uid = uid;
            this.initialPid = initialPid;
            this.notification = notification;
            this.user = user;
            this.postTime = postTime;
            this.key = key();
            this.groupKey = groupKey();
        }
    }

    public StatusBarNotification(Parcel in) {
        this.pkg = in.readString();
        this.opPkg = in.readString();
        this.id = in.readInt();
        if (in.readInt() != 0) {
            this.tag = in.readString();
        } else {
            this.tag = null;
        }
        this.uid = in.readInt();
        this.initialPid = in.readInt();
        this.notification = new Notification(in);
        this.user = UserHandle.readFromParcel(in);
        this.postTime = in.readLong();
        if (in.readInt() != 0) {
            this.overrideGroupKey = in.readString();
        } else {
            this.overrideGroupKey = null;
        }
        this.key = key();
        this.groupKey = groupKey();
    }

    private String key() {
        String sbnKey = this.user.getIdentifier() + "|" + this.pkg + "|" + this.id + "|" + this.tag + "|" + this.uid;
        if (this.overrideGroupKey == null || !getNotification().isGroupSummary()) {
            return sbnKey;
        }
        return sbnKey + "|" + this.overrideGroupKey;
    }

    private String groupKey() {
        if (this.overrideGroupKey != null) {
            return this.user.getIdentifier() + "|" + this.pkg + "|" + "g:" + this.overrideGroupKey;
        }
        String group = getNotification().getGroup();
        String sortKey = getNotification().getSortKey();
        if (group == null && sortKey == null) {
            return this.key;
        }
        String str;
        StringBuilder append = new StringBuilder().append(this.user.getIdentifier()).append("|").append(this.pkg).append("|");
        if (group == null) {
            str = "p:" + this.notification.priority;
        } else {
            str = "g:" + group;
        }
        return append.append(str).toString();
    }

    public boolean isGroup() {
        if (this.overrideGroupKey != null || isAppGroup()) {
            return true;
        }
        return false;
    }

    public boolean isAppGroup() {
        if (getNotification().getGroup() == null && getNotification().getSortKey() == null) {
            return false;
        }
        return true;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.pkg);
        out.writeString(this.opPkg);
        out.writeInt(this.id);
        if (this.tag != null) {
            out.writeInt(1);
            out.writeString(this.tag);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.uid);
        out.writeInt(this.initialPid);
        this.notification.writeToParcel(out, flags);
        this.user.writeToParcel(out, flags);
        out.writeLong(this.postTime);
        if (this.overrideGroupKey != null) {
            out.writeInt(1);
            out.writeString(this.overrideGroupKey);
            return;
        }
        out.writeInt(0);
    }

    public int describeContents() {
        return 0;
    }

    public StatusBarNotification cloneLight() {
        Notification no = new Notification();
        this.notification.cloneInto(no, false);
        return new StatusBarNotification(this.pkg, this.opPkg, this.id, this.tag, this.uid, this.initialPid, no, this.user, this.overrideGroupKey, this.postTime);
    }

    public StatusBarNotification clone() {
        return new StatusBarNotification(this.pkg, this.opPkg, this.id, this.tag, this.uid, this.initialPid, this.notification.clone(), this.user, this.overrideGroupKey, this.postTime);
    }

    public String toString() {
        return String.format("StatusBarNotification(pkg=%s user=%s id=%d tag=%s key=%s: %s)", new Object[]{this.pkg, this.user, Integer.valueOf(this.id), this.tag, this.key, this.notification});
    }

    public boolean isOngoing() {
        return (this.notification.flags & 2) != 0;
    }

    public boolean isClearable() {
        if ((this.notification.flags & 2) == 0 && (this.notification.flags & 32) == 0) {
            return true;
        }
        return false;
    }

    public int getUserId() {
        return this.user.getIdentifier();
    }

    public String getPackageName() {
        return this.pkg;
    }

    public int getId() {
        return this.id;
    }

    public String getTag() {
        return this.tag;
    }

    public int getUid() {
        return this.uid;
    }

    public String getOpPkg() {
        return this.opPkg;
    }

    public int getInitialPid() {
        return this.initialPid;
    }

    public Notification getNotification() {
        return this.notification;
    }

    public UserHandle getUser() {
        return this.user;
    }

    public long getPostTime() {
        return this.postTime;
    }

    public String getKey() {
        return this.key;
    }

    public String getGroupKey() {
        return this.groupKey;
    }

    public void setOverrideGroupKey(String overrideGroupKey) {
        this.overrideGroupKey = overrideGroupKey;
        this.groupKey = groupKey();
    }

    public String getOverrideGroupKey() {
        return this.overrideGroupKey;
    }

    public Context getPackageContext(Context context) {
        if (this.mContext == null) {
            try {
                this.mContext = context.createApplicationContext(context.getPackageManager().getApplicationInfo(this.pkg, Process.PROC_OUT_LONG), 4);
            } catch (NameNotFoundException e) {
                this.mContext = null;
            }
        }
        if (this.mContext == null) {
            this.mContext = context;
        }
        return this.mContext;
    }
}
