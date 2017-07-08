package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;

public class HwGnssDataCollector {
    private static final boolean DEBUG = false;
    private static final int GNSS_DATA_COLLECT_EVENT = 74;
    private static final int NTP_SERVER_IP_COLLECT = 0;
    private static final String TAG = "HwGnssLog_DataCollector";
    private static final int UPDATE_LOCATION = 1;
    private static final int UPDATE_NTP_SERVER_INFO = 0;
    private static final boolean VERBOSE = false;
    private Context mContext;
    private GeolocationCollectManager mGeolocationCollectManager;
    private HwGnssDataCollectorHandler mHandler;
    private HandlerThread mThread;

    class HwGnssDataCollectorHandler extends Handler {
        private ArrayList list;

        HwGnssDataCollectorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            this.list = (ArrayList) msg.obj;
            switch (msg.what) {
                case HwGnssDataCollector.NTP_SERVER_IP_COLLECT /*0*/:
                    HwGnssDataCollector.this.handlerNtpServerInfo((String) this.list.get(HwGnssDataCollector.NTP_SERVER_IP_COLLECT));
                case HwGnssDataCollector.UPDATE_LOCATION /*1*/:
                    HwGnssDataCollector.this.handlerGeoLocationInfo((Location) this.list.get(HwGnssDataCollector.NTP_SERVER_IP_COLLECT), ((Long) this.list.get(HwGnssDataCollector.UPDATE_LOCATION)).longValue(), (String) this.list.get(2));
                default:
                    if (HwGnssDataCollector.DEBUG) {
                        Log.d(HwGnssDataCollector.TAG, "====handleMessage: msg.what = " + msg.what + "====");
                    }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwGnssDataCollector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwGnssDataCollector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwGnssDataCollector.<clinit>():void");
    }

    HwGnssDataCollector(HandlerThread thread, Context context) {
        this.mGeolocationCollectManager = null;
        this.mThread = thread;
        this.mContext = context;
        this.mHandler = new HwGnssDataCollectorHandler(this.mThread.getLooper());
        this.mGeolocationCollectManager = new GeolocationCollectManager(this.mContext);
    }

    public void updateNtpServerInfo(String address) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(address);
        msg.what = NTP_SERVER_IP_COLLECT;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateLocation(Location location, long time, String provider) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(location);
        list.add(Long.valueOf(time));
        list.add(provider);
        msg.what = UPDATE_LOCATION;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    private void handlerNtpServerInfo(String address) {
        new NtpIpCollector(this.mContext).uploadNtpServerIp(GNSS_DATA_COLLECT_EVENT, NTP_SERVER_IP_COLLECT, address);
    }

    private void handlerGeoLocationInfo(Location location, long time, String provider) {
        if (this.mGeolocationCollectManager != null) {
            this.mGeolocationCollectManager.setGeoLocationInfo(location, time, provider);
        }
    }
}
