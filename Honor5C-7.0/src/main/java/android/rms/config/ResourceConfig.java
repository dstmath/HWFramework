package android.rms.config;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ResourceConfig implements Parcelable {
    public static final Creator<ResourceConfig> CREATOR = null;
    private int loop_interval;
    private int resource_id;
    private int resource_max_peroid;
    private String resource_name;
    private int resource_normal_threshold;
    private int resource_strategy;
    private int resource_threshold;
    private int resource_urgent_threshold;
    private int resource_waring_threshold;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.config.ResourceConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.config.ResourceConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.config.ResourceConfig.<clinit>():void");
    }

    public ResourceConfig(int resource_id, int resource_threshold, int resource_strategy, int resource_max_peroid, int loop_interval, String resource_name, int resource_normal_threshold, int resource_waring_threshold, int resource_urgent_threshold) {
        this.resource_id = 0;
        this.resource_threshold = 0;
        this.resource_strategy = 0;
        this.resource_max_peroid = 0;
        this.loop_interval = 0;
        this.resource_name = "";
        this.resource_normal_threshold = 0;
        this.resource_waring_threshold = 0;
        this.resource_urgent_threshold = 0;
        this.resource_id = resource_id;
        this.resource_threshold = resource_threshold;
        this.resource_strategy = resource_strategy;
        this.resource_max_peroid = resource_max_peroid;
        this.loop_interval = loop_interval;
        this.resource_name = resource_name;
        this.resource_normal_threshold = resource_normal_threshold;
        this.resource_waring_threshold = resource_waring_threshold;
        this.resource_urgent_threshold = resource_urgent_threshold;
    }

    public ResourceConfig(Parcel source) {
        this.resource_id = 0;
        this.resource_threshold = 0;
        this.resource_strategy = 0;
        this.resource_max_peroid = 0;
        this.loop_interval = 0;
        this.resource_name = "";
        this.resource_normal_threshold = 0;
        this.resource_waring_threshold = 0;
        this.resource_urgent_threshold = 0;
        this.resource_id = source.readInt();
        this.resource_threshold = source.readInt();
        this.resource_strategy = source.readInt();
        this.resource_max_peroid = source.readInt();
        this.loop_interval = source.readInt();
        this.resource_name = source.readString();
        this.resource_normal_threshold = source.readInt();
        this.resource_waring_threshold = source.readInt();
        this.resource_urgent_threshold = source.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.resource_id);
        dest.writeInt(this.resource_threshold);
        dest.writeInt(this.resource_strategy);
        dest.writeInt(this.resource_max_peroid);
        dest.writeInt(this.loop_interval);
        dest.writeString(this.resource_name);
        dest.writeInt(this.resource_normal_threshold);
        dest.writeInt(this.resource_waring_threshold);
        dest.writeInt(this.resource_urgent_threshold);
    }

    public int getResourceID() {
        return this.resource_id;
    }

    public int getResourceThreshold() {
        return this.resource_threshold;
    }

    public int getResourceStrategy() {
        return this.resource_strategy;
    }

    public int getResourceMaxPeroid() {
        return this.resource_max_peroid;
    }

    public int getLoopInterval() {
        return this.loop_interval;
    }

    public String getResouceName() {
        return this.resource_name;
    }

    public int getResouceNormalThreshold() {
        return this.resource_normal_threshold;
    }

    public int getResouceWarningThreshold() {
        return this.resource_waring_threshold;
    }

    public int getResouceUrgentThreshold() {
        return this.resource_urgent_threshold;
    }

    public void setResourceID(int id) {
        this.resource_id = id;
    }

    public void setResourceThreshold(int threshold) {
        this.resource_threshold = threshold;
    }

    public void setResourceStrategy(int strategy) {
        this.resource_strategy = strategy;
    }

    public void setResourceMaxPeroid(int max_peroid) {
        this.resource_max_peroid = max_peroid;
    }

    public void setLoopInterval(int interval) {
        this.loop_interval = interval;
    }
}
