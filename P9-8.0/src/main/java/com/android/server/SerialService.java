package com.android.server;

import android.content.Context;
import android.hardware.ISerialManager.Stub;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.util.ArrayList;

public class SerialService extends Stub {
    private final Context mContext;
    private final String[] mSerialPorts;

    private native ParcelFileDescriptor native_open(String str);

    public SerialService(Context context) {
        this.mContext = context;
        this.mSerialPorts = context.getResources().getStringArray(17236030);
    }

    public String[] getSerialPorts() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SERIAL_PORT", null);
        ArrayList<String> ports = new ArrayList();
        for (String path : this.mSerialPorts) {
            if (new File(path).exists()) {
                ports.add(path);
            }
        }
        String[] result = new String[ports.size()];
        ports.toArray(result);
        return result;
    }

    public ParcelFileDescriptor openSerialPort(String path) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SERIAL_PORT", null);
        for (String equals : this.mSerialPorts) {
            if (equals.equals(path)) {
                return native_open(path);
            }
        }
        throw new IllegalArgumentException("Invalid serial port " + path);
    }
}
