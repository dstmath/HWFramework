package com.android.server.wifi.wifipro;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import java.util.ArrayList;

public class HwDualBandManager {
    private static HwDualBandManager mHwDualBandManager;
    private HwDualBandStateMachine mHwDualBandStateMachine;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.HwDualBandManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.HwDualBandManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.HwDualBandManager.<clinit>():void");
    }

    private HwDualBandManager(Context context, IDualBandManagerCallback callBack) {
        this.mHwDualBandStateMachine = null;
        this.mHwDualBandStateMachine = new HwDualBandStateMachine(context, callBack);
    }

    public static HwDualBandManager createInstance(Context context, IDualBandManagerCallback callBack) {
        if (mHwDualBandManager == null) {
            mHwDualBandManager = new HwDualBandManager(context, callBack);
        }
        Log.d(HwDualBandMessageUtil.TAG, "HwDualBandManager init Complete!");
        return mHwDualBandManager;
    }

    public static HwDualBandManager getInstance() {
        return mHwDualBandManager;
    }

    public boolean startDualBandManger() {
        this.mHwDualBandStateMachine.onStart();
        return true;
    }

    public boolean stopDualBandManger() {
        this.mHwDualBandStateMachine.onStop();
        return true;
    }

    public boolean isDualbandScanning() {
        if (this.mHwDualBandStateMachine == null) {
            return false;
        }
        return this.mHwDualBandStateMachine.isDualbandScanning();
    }

    public boolean startMonitor(ArrayList<HwDualBandMonitorInfo> apList) {
        if (apList.size() == 0) {
            Log.e(HwDualBandMessageUtil.TAG, "startMonitor apList.size() == 0");
            return false;
        }
        Handler mHandler = this.mHwDualBandStateMachine.getStateMachineHandler();
        Bundle data = new Bundle();
        data.putParcelableArrayList(HwDualBandMessageUtil.MSG_KEY_APLIST, (ArrayList) apList.clone());
        Message msg = new Message();
        msg.what = MessageUtil.CMD_START_SCAN;
        msg.setData(data);
        mHandler.sendMessage(msg);
        return true;
    }

    public boolean stopMonitor() {
        this.mHwDualBandStateMachine.getStateMachineHandler().sendEmptyMessage(HwDualBandMessageUtil.CMD_STOP_MONITOR);
        return true;
    }

    public void updateCurrentRssi(int rssi) {
        Bundle data = new Bundle();
        data.putInt(HwDualBandMessageUtil.MSG_KEY_RSSI, rssi);
        Message msg = new Message();
        msg.what = 18;
        msg.setData(data);
        this.mHwDualBandStateMachine.getStateMachineHandler().sendMessage(msg);
    }
}
