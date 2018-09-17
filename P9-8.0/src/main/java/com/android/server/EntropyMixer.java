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

    public EntropyMixer(Context context, String entropyFile, String randomDevice, String hwRandomDevice) {
        this.mHandler = new Handler() {
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
            public void onReceive(Context context, Intent intent) {
                EntropyMixer.this.writeEntropy();
            }
        };
        if (randomDevice == null) {
            throw new NullPointerException("randomDevice");
        } else if (hwRandomDevice == null) {
            throw new NullPointerException("hwRandomDevice");
        } else if (entropyFile == null) {
            throw new NullPointerException("entropyFile");
        } else {
            this.randomDevice = randomDevice;
            this.hwRandomDevice = hwRandomDevice;
            this.entropyFile = entropyFile;
            loadInitialEntropy();
            addDeviceSpecificEntropy();
            addHwRandomEntropy();
            writeEntropy();
            scheduleEntropyWriter();
            IntentFilter broadcastFilter = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
            broadcastFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            broadcastFilter.addAction("android.intent.action.REBOOT");
            context.registerReceiver(this.mBroadcastReceiver, broadcastFilter);
        }
    }

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

    private void writeEntropy() {
        try {
            Slog.i(TAG, "Writing entropy...");
            RandomBlock.fromFile(this.randomDevice).toFile(this.entropyFile, true);
        } catch (IOException e) {
            Slog.w(TAG, "Unable to write entropy", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x00a7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addDeviceSpecificEntropy() {
        IOException e;
        Throwable th;
        PrintWriter out = null;
        try {
            PrintWriter out2 = new PrintWriter(new FileOutputStream(this.randomDevice));
            try {
                out2.println("Copyright (C) 2009 The Android Open Source Project");
                out2.println("All Your Randomness Are Belong To Us");
                out2.println(START_TIME);
                out2.println(START_NANOTIME);
                out2.println(SystemProperties.get("ro.serialno"));
                out2.println(SystemProperties.get("ro.bootmode"));
                out2.println(SystemProperties.get("ro.baseband"));
                out2.println(SystemProperties.get("ro.carrier"));
                out2.println(SystemProperties.get("ro.bootloader"));
                out2.println(SystemProperties.get("ro.hardware"));
                out2.println(SystemProperties.get("ro.revision"));
                out2.println(SystemProperties.get("ro.build.fingerprint"));
                out2.println(new Object().hashCode());
                out2.println(System.currentTimeMillis());
                out2.println(System.nanoTime());
                if (out2 != null) {
                    out2.close();
                }
                out = out2;
            } catch (IOException e2) {
                e = e2;
                out = out2;
                try {
                    Slog.w(TAG, "Unable to add device specific data to the entropy pool", e);
                    if (out == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        out.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            Slog.w(TAG, "Unable to add device specific data to the entropy pool", e);
            if (out == null) {
                out.close();
            }
        }
    }

    private void addHwRandomEntropy() {
        try {
            RandomBlock.fromFile(this.hwRandomDevice).toFile(this.randomDevice, false);
            Slog.i(TAG, "Added HW RNG output to entropy pool");
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.w(TAG, "Failed to add HW RNG output to entropy pool", e2);
        }
    }

    private static String getSystemDir() {
        File systemDir = new File(Environment.getDataDirectory(), "system");
        systemDir.mkdirs();
        return systemDir.toString();
    }
}
