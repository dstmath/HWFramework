package android.os;

import android.content.Context;
import android.util.Log;

public class HardwarePropertiesManager {
    public static final int DEVICE_TEMPERATURE_BATTERY = 2;
    public static final int DEVICE_TEMPERATURE_CPU = 0;
    public static final int DEVICE_TEMPERATURE_GPU = 1;
    public static final int DEVICE_TEMPERATURE_SKIN = 3;
    private static final String TAG = null;
    public static final int TEMPERATURE_CURRENT = 0;
    public static final int TEMPERATURE_SHUTDOWN = 2;
    public static final int TEMPERATURE_THROTTLING = 1;
    public static final int TEMPERATURE_THROTTLING_BELOW_VR_MIN = 3;
    public static final float UNDEFINED_TEMPERATURE = -3.4028235E38f;
    private final Context mContext;
    private final IHardwarePropertiesManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.HardwarePropertiesManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.HardwarePropertiesManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.HardwarePropertiesManager.<clinit>():void");
    }

    public HardwarePropertiesManager(Context context, IHardwarePropertiesManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public float[] getDeviceTemperatures(int type, int source) {
        switch (type) {
            case TEMPERATURE_CURRENT /*0*/:
            case TEMPERATURE_THROTTLING /*1*/:
            case TEMPERATURE_SHUTDOWN /*2*/:
            case TEMPERATURE_THROTTLING_BELOW_VR_MIN /*3*/:
                switch (source) {
                    case TEMPERATURE_CURRENT /*0*/:
                    case TEMPERATURE_THROTTLING /*1*/:
                    case TEMPERATURE_SHUTDOWN /*2*/:
                    case TEMPERATURE_THROTTLING_BELOW_VR_MIN /*3*/:
                        try {
                            return this.mService.getDeviceTemperatures(this.mContext.getOpPackageName(), type, source);
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    default:
                        Log.w(TAG, "Unknown device temperature source.");
                        return new float[TEMPERATURE_CURRENT];
                }
            default:
                Log.w(TAG, "Unknown device temperature type.");
                return new float[TEMPERATURE_CURRENT];
        }
    }

    public CpuUsageInfo[] getCpuUsages() {
        try {
            return this.mService.getCpuUsages(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public float[] getFanSpeeds() {
        try {
            return this.mService.getFanSpeeds(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
