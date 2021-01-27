package com.android.server;

import android.content.Context;
import android.hardware.ISerialManager;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.util.ArrayList;

public class SerialService extends ISerialManager.Stub {
    private final Context mContext;
    private final String[] mSerialPorts;

    private native ParcelFileDescriptor native_open(String str);

    public SerialService(Context context) {
        this.mContext = context;
        this.mSerialPorts = context.getResources().getStringArray(17236060);
    }

    public String[] getSerialPorts() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SERIAL_PORT", null);
        ArrayList<String> ports = new ArrayList<>();
        int i = 0;
        while (true) {
            String[] strArr = this.mSerialPorts;
            if (i < strArr.length) {
                String path = strArr[i];
                if (new File(path).exists()) {
                    ports.add(path);
                }
                i++;
            } else {
                String[] result = new String[ports.size()];
                ports.toArray(result);
                return result;
            }
        }
    }

    public ParcelFileDescriptor openSerialPort(String path) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SERIAL_PORT", null);
        int i = 0;
        while (true) {
            String[] strArr = this.mSerialPorts;
            if (i >= strArr.length) {
                throw new IllegalArgumentException("Invalid serial port " + path);
            } else if (strArr[i].equals(path)) {
                return native_open(path);
            } else {
                i++;
            }
        }
    }
}
