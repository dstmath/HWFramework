package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class EntropyMixer extends Binder {
    private static final int ENTROPY_WHAT = 1;
    private static final int ENTROPY_WRITE_PERIOD = 10800000;
    private static final long START_NANOTIME = System.nanoTime();
    private static final long START_TIME = System.currentTimeMillis();
    private static final String TAG = "EntropyMixer";
    private final String entropyFile;
    private final String hwRandomDevice;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Handler mHandler;
    private final String randomDevice;

    public EntropyMixer(Context context) {
        this(context, getSystemDir() + "/entropy.dat", "/dev/urandom", "/dev/hw_random");
    }

    public EntropyMixer(Context context, String entropyFile2, String randomDevice2, String hwRandomDevice2) {
        this.mHandler = new Handler(IoThread.getHandler().getLooper()) {
            /* class com.android.server.EntropyMixer.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what != 1) {
                    Slog.e(EntropyMixer.TAG, "Will not process invalid message");
                    return;
                }
                EntropyMixer.this.addHwRandomEntropy();
                EntropyMixer.this.writeEntropy();
                EntropyMixer.this.scheduleEntropyWriter();
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.EntropyMixer.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                EntropyMixer.this.writeEntropy();
            }
        };
        if (randomDevice2 == null) {
            throw new NullPointerException("randomDevice");
        } else if (hwRandomDevice2 == null) {
            throw new NullPointerException("hwRandomDevice");
        } else if (entropyFile2 != null) {
            this.randomDevice = randomDevice2;
            this.hwRandomDevice = hwRandomDevice2;
            this.entropyFile = entropyFile2;
            loadInitialEntropy();
            addDeviceSpecificEntropy();
            addHwRandomEntropy();
            writeEntropy();
            scheduleEntropyWriter();
            IntentFilter broadcastFilter = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
            broadcastFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            broadcastFilter.addAction("android.intent.action.REBOOT");
            context.registerReceiver(this.mBroadcastReceiver, broadcastFilter, null, this.mHandler);
        } else {
            throw new NullPointerException("entropyFile");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleEntropyWriter() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 10800000);
    }

    private void loadInitialEntropy() {
        try {
            RandomBlock.fromFile(this.entropyFile).toFile(this.randomDevice, false);
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "No existing entropy file -- first boot?");
        } catch (IOException e2) {
            Slog.w(TAG, "Failure loading existing entropy file", e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeEntropy() {
        try {
            Slog.i(TAG, "Writing entropy...");
            RandomBlock.fromFile(this.randomDevice).toFile(this.entropyFile, true);
        } catch (IOException e) {
            Slog.w(TAG, "Unable to write entropy", e);
        }
    }

    private void addDeviceSpecificEntropy() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(this.randomDevice));
            out.println("Copyright (C) 2009 The Android Open Source Project");
            out.println("All Your Randomness Are Belong To Us");
            out.println(START_TIME);
            out.println(START_NANOTIME);
            out.println(SystemProperties.get("ro.serialno"));
            out.println(SystemProperties.get("ro.bootmode"));
            out.println(SystemProperties.get("ro.baseband"));
            out.println(SystemProperties.get("ro.carrier"));
            out.println(SystemProperties.get("ro.bootloader"));
            out.println(SystemProperties.get("ro.hardware"));
            out.println(SystemProperties.get("ro.revision"));
            out.println(SystemProperties.get("ro.build.fingerprint"));
            out.println(new Object().hashCode());
            out.println(System.currentTimeMillis());
            out.println(System.nanoTime());
        } catch (IOException e) {
            Slog.w(TAG, "Unable to add device specific data to the entropy pool", e);
            if (out == null) {
                return;
            }
        } catch (Throwable th) {
            if (out != null) {
                out.close();
            }
            throw th;
        }
        out.close();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addHwRandomEntropy() {
        if (new File(this.hwRandomDevice).exists()) {
            try {
                RandomBlock.fromFile(this.hwRandomDevice).toFile(this.randomDevice, false);
                Slog.i(TAG, "Added HW RNG output to entropy pool");
            } catch (IOException e) {
                Slog.w(TAG, "Failed to add HW RNG output to entropy pool", e);
            }
        }
    }

    private static String getSystemDir() {
        File systemDir = new File(Environment.getDataDirectory(), "system");
        systemDir.mkdirs();
        return systemDir.toString();
    }
}
