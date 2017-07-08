package com.android.internal.location;

import android.location.LocationRequest;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public final class ProviderRequest implements Parcelable {
    public static final Creator<ProviderRequest> CREATOR = null;
    public long interval;
    public List<LocationRequest> locationRequests;
    public boolean reportLocation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.location.ProviderRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.location.ProviderRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.location.ProviderRequest.<clinit>():void");
    }

    public ProviderRequest() {
        this.reportLocation = false;
        this.interval = Long.MAX_VALUE;
        this.locationRequests = new ArrayList();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.reportLocation ? 1 : 0);
        parcel.writeLong(this.interval);
        parcel.writeInt(this.locationRequests.size());
        for (LocationRequest request : this.locationRequests) {
            request.writeToParcel(parcel, flags);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("ProviderRequest[");
        if (this.reportLocation) {
            s.append("ON");
            s.append(" interval=");
            TimeUtils.formatDuration(this.interval, s);
        } else {
            s.append("OFF");
        }
        s.append(']');
        return s.toString();
    }
}
