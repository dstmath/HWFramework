package com.android.commands.hid;

import android.util.Log;
import android.util.SparseArray;
import com.android.commands.hid.Event.Reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import libcore.io.IoUtils;

public class Hid {
    private static final String TAG = "HID";
    private final SparseArray<Device> mDevices;
    private final Reader mReader;

    private static void usage() {
        error("Usage: hid [FILE]");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            System.exit(1);
        }
        AutoCloseable autoCloseable = null;
        try {
            if (args[0].equals("-")) {
                autoCloseable = System.in;
            } else {
                Object stream = new FileInputStream(new File(args[0]));
            }
            new Hid(autoCloseable).run();
        } catch (Exception e) {
            error("HID injection failed.", e);
            System.exit(1);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private Hid(InputStream in) {
        this.mDevices = new SparseArray();
        try {
            this.mReader = new Reader(new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() {
        while (true) {
            try {
                Event e = this.mReader.getNextEvent();
                if (e == null) {
                    break;
                }
                process(e);
            } catch (IOException ex) {
                error("Error reading in events.", ex);
            }
        }
        for (int i = 0; i < this.mDevices.size(); i++) {
            ((Device) this.mDevices.valueAt(i)).close();
        }
    }

    private void process(Event e) {
        int index = this.mDevices.indexOfKey(e.getId());
        if (index >= 0) {
            Device d = (Device) this.mDevices.valueAt(index);
            if (Event.COMMAND_DELAY.equals(e.getCommand())) {
                d.addDelay(e.getDuration());
                return;
            } else if (Event.COMMAND_REPORT.equals(e.getCommand())) {
                d.sendReport(e.getReport());
                return;
            } else {
                error("Unknown command \"" + e.getCommand() + "\". Ignoring event.");
                return;
            }
        }
        registerDevice(e);
    }

    private void registerDevice(Event e) {
        if (Event.COMMAND_REGISTER.equals(e.getCommand())) {
            int id = e.getId();
            this.mDevices.append(id, new Device(id, e.getName(), e.getVendorId(), e.getProductId(), e.getDescriptor(), e.getReport()));
            return;
        }
        throw new IllegalStateException("Tried to send command \"" + e.getCommand() + "\" to an unregistered device!");
    }

    private static void error(String msg) {
        error(msg, null);
    }

    private static void error(String msg, Exception e) {
        System.out.println(msg);
        Log.e(TAG, msg);
        if (e != null) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
