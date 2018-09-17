package com.android.server.location;

import android.location.ILocationManager;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class PassiveProvider implements LocationProviderInterface {
    private static final ProviderProperties PROPERTIES = null;
    private static final String TAG = "PassiveProvider";
    private final ILocationManager mLocationManager;
    private boolean mReportLocation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.PassiveProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.PassiveProvider.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.PassiveProvider.<clinit>():void");
    }

    public PassiveProvider(ILocationManager locationManager) {
        this.mLocationManager = locationManager;
    }

    public String getName() {
        return "passive";
    }

    public ProviderProperties getProperties() {
        return PROPERTIES;
    }

    public boolean isEnabled() {
        return true;
    }

    public void enable() {
    }

    public void disable() {
    }

    public int getStatus(Bundle extras) {
        if (this.mReportLocation) {
            return 2;
        }
        return 1;
    }

    public long getStatusUpdateTime() {
        return -1;
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        this.mReportLocation = request.reportLocation;
    }

    public void updateLocation(Location location) {
        if (this.mReportLocation) {
            try {
                this.mLocationManager.reportLocation(location, true);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling reportLocation");
            }
        }
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        return false;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mReportLocation=" + this.mReportLocation);
    }
}
