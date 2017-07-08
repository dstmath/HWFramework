package android.app;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class AutomaticZenRule implements Parcelable {
    public static final Creator<AutomaticZenRule> CREATOR = null;
    private Uri conditionId;
    private long creationTime;
    private boolean enabled;
    private int interruptionFilter;
    private String name;
    private ComponentName owner;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.AutomaticZenRule.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.AutomaticZenRule.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.AutomaticZenRule.<clinit>():void");
    }

    public AutomaticZenRule(String name, ComponentName owner, Uri conditionId, int interruptionFilter, boolean enabled) {
        this.enabled = false;
        this.name = name;
        this.owner = owner;
        this.conditionId = conditionId;
        this.interruptionFilter = interruptionFilter;
        this.enabled = enabled;
    }

    public AutomaticZenRule(String name, ComponentName owner, Uri conditionId, int interruptionFilter, boolean enabled, long creationTime) {
        this(name, owner, conditionId, interruptionFilter, enabled);
        this.creationTime = creationTime;
    }

    public AutomaticZenRule(Parcel source) {
        boolean z = false;
        this.enabled = false;
        if (source.readInt() == 1) {
            z = true;
        }
        this.enabled = z;
        if (source.readInt() == 1) {
            this.name = source.readString();
        }
        this.interruptionFilter = source.readInt();
        this.conditionId = (Uri) source.readParcelable(null);
        this.owner = (ComponentName) source.readParcelable(null);
        this.creationTime = source.readLong();
    }

    public ComponentName getOwner() {
        return this.owner;
    }

    public Uri getConditionId() {
        return this.conditionId;
    }

    public int getInterruptionFilter() {
        return this.interruptionFilter;
    }

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public void setConditionId(Uri conditionId) {
        this.conditionId = conditionId;
    }

    public void setInterruptionFilter(int interruptionFilter) {
        this.interruptionFilter = interruptionFilter;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        if (this.enabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.name != null) {
            dest.writeInt(1);
            dest.writeString(this.name);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.interruptionFilter);
        dest.writeParcelable(this.conditionId, 0);
        dest.writeParcelable(this.owner, 0);
        dest.writeLong(this.creationTime);
    }

    public String toString() {
        return new StringBuilder(AutomaticZenRule.class.getSimpleName()).append('[').append("enabled=").append(this.enabled).append(",name=").append(this.name).append(",interruptionFilter=").append(this.interruptionFilter).append(",conditionId=").append(this.conditionId).append(",owner=").append(this.owner).append(",creationTime=").append(this.creationTime).append(']').toString();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (!(o instanceof AutomaticZenRule)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        AutomaticZenRule other = (AutomaticZenRule) o;
        if (other.enabled != this.enabled || !Objects.equals(other.name, this.name) || other.interruptionFilter != this.interruptionFilter || !Objects.equals(other.conditionId, this.conditionId) || !Objects.equals(other.owner, this.owner)) {
            z = false;
        } else if (other.creationTime != this.creationTime) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.enabled), this.name, Integer.valueOf(this.interruptionFilter), this.conditionId, this.owner, Long.valueOf(this.creationTime)});
    }
}
