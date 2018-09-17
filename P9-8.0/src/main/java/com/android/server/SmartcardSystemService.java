package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import org.simalliance.openmobileapi.service.ISmartcardService;
import org.simalliance.openmobileapi.service.ISmartcardServiceCallback;
import org.simalliance.openmobileapi.service.ISmartcardSystemService.Stub;
import org.simalliance.openmobileapi.service.SmartcardError;

public class SmartcardSystemService extends Stub {
    private static final int AID_APP = 10000;
    private static final int CONNECTED = 2;
    private static final int CONNECTING = 1;
    private static final int DISCONNECTED = 0;
    public static final String SMARTCARD_SERVICE_TAG = "SmartcardSystemService";
    private final ISmartcardServiceCallback callback = new ISmartcardServiceCallback.Stub() {
    };
    public volatile Exception lastException;
    private int mBindStatus = 0;
    private ServiceConnection mConnection;
    private Context mContext;
    private boolean mIccCardReady;
    private SmartcardSystemHandler mSmartcardSystemHandler;
    private volatile ISmartcardService smartcardService;

    private static class SmartcardSystemHandler extends Handler {
        public SmartcardSystemHandler(Looper looper) {
            super(looper, null, true);
        }
    }

    private static String bytesToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", new Object[]{Integer.valueOf(bytes[i] & 255)}));
        }
        return sb.toString();
    }

    private byte[] stringToByteArray(String s) {
        byte[] b = new byte[(s.length() / 2)];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16);
        }
        return b;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public SmartcardSystemService(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.mContext = context;
        this.mSmartcardSystemHandler = new SmartcardSystemHandler(BackgroundThread.get().getLooper());
        this.mConnection = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                SmartcardSystemService.this.smartcardService = ISmartcardService.Stub.asInterface(service);
                Log.i(SmartcardSystemService.SMARTCARD_SERVICE_TAG, "Smartcard system service onServiceConnected");
                SmartcardSystemService.this.mBindStatus = 2;
            }

            public void onServiceDisconnected(ComponentName className) {
                SmartcardSystemService.this.smartcardService = null;
                Log.i(SmartcardSystemService.SMARTCARD_SERVICE_TAG, "Smartcard system service onServiceDisconnected");
                SmartcardSystemService.this.mBindStatus = 0;
            }
        };
        BroadcastReceiver apduServiceLaunchedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && AwareJobSchedulerConstants.SIM_STATUS_READY.equals(intent.getStringExtra("ss"))) {
                    Log.i(SmartcardSystemService.SMARTCARD_SERVICE_TAG, "INTENT_VALUE_ICC_READY");
                    SmartcardSystemService.this.mIccCardReady = true;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(apduServiceLaunchedReceiver, intentFilter);
    }

    public void closeChannel(long hChannel) throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "called: closeChannel(" + hChannel + ")");
            SmartcardError error = new SmartcardError();
            this.smartcardService.closeChannel(hChannel, error);
            this.lastException = error.createException();
            Log.i(SMARTCARD_SERVICE_TAG, "SmartcardError: " + error.toString());
            return;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public String getReaders() throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            int i;
            Log.i(SMARTCARD_SERVICE_TAG, "called: getReaders()");
            SmartcardError error = new SmartcardError();
            String[] result = this.smartcardService.getReaders(error);
            for (i = 0; i < result.length; i++) {
                Log.i(SMARTCARD_SERVICE_TAG, "getReaders(" + i + ") returned: " + result[i]);
            }
            this.lastException = error.createException();
            StringBuffer readerlist = new StringBuffer();
            for (String append : result) {
                readerlist.append(append);
                readerlist.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            }
            return readerlist.toString();
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public boolean isCardPresent(String reader) throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            SmartcardError error = new SmartcardError();
            boolean result = this.smartcardService.isCardPresent(reader, error);
            Log.i(SMARTCARD_SERVICE_TAG, "isCardPresent(" + reader + ") returned: " + result);
            this.lastException = error.createException();
            return result;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public long openBasicChannel(String reader) throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "called: openBasicChannel(" + reader + ")");
            SmartcardError error = new SmartcardError();
            long channelValue = this.smartcardService.openBasicChannel(reader, this.callback, error);
            this.lastException = error.createException();
            return channelValue;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public long openBasicChannelAid(String reader, String aid) throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "called: openBasicChannelAid (" + reader + ")");
            SmartcardError error = new SmartcardError();
            long channelValue = this.smartcardService.openBasicChannelAid(reader, stringToByteArray(aid), this.callback, error);
            this.lastException = error.createException();
            return channelValue;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public long openLogicalChannel(String reader, String aid) throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "called: openLogicalChannel(" + reader + ", " + aid + ")");
            SmartcardError error = new SmartcardError();
            long channelValue = this.smartcardService.openLogicalChannel(reader, stringToByteArray(aid), this.callback, error);
            this.lastException = error.createException();
            return channelValue;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public String transmit(long hChannel, String command) throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "called: transmit(" + hChannel + ", " + command + ")");
            SmartcardError error = new SmartcardError();
            byte[] cmd = hexStringToByteArray(command);
            Log.i(SMARTCARD_SERVICE_TAG, "transmitting: " + bytesToString(cmd));
            String strResponse = "";
            try {
                byte[] rsp = this.smartcardService.transmit(hChannel, cmd, error);
                if (rsp != null) {
                    Log.i(SMARTCARD_SERVICE_TAG, "transmit returned: " + bytesToString(rsp));
                    strResponse = bytesToString(rsp);
                }
            } catch (Exception e) {
                Log.w(SMARTCARD_SERVICE_TAG, "transmit exception: " + e.toString());
                Log.w(SMARTCARD_SERVICE_TAG, "transmit Error object: " + error.toString());
            }
            Log.i(SMARTCARD_SERVICE_TAG, "transmit returned: " + strResponse);
            this.lastException = error.createException();
            return strResponse;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    public String getLastError() {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "called: getLastError");
            String strErrorMessage = "";
            if (this.lastException != null) {
                strErrorMessage = this.lastException.getMessage();
                if (strErrorMessage == null) {
                    strErrorMessage = this.lastException.toString();
                }
                Log.w(SMARTCARD_SERVICE_TAG, "getLastError - message " + strErrorMessage);
            }
            return strErrorMessage;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    private void bindSmartCardService() {
        Log.i(SMARTCARD_SERVICE_TAG, "called bindSmartCardService");
        this.mSmartcardSystemHandler.post(new Runnable() {
            public void run() {
                if (SmartcardSystemService.this.mBindStatus == 0) {
                    Log.i(SmartcardSystemService.SMARTCARD_SERVICE_TAG, "begin bind SmartCardService");
                    Intent startIntent = new Intent(ISmartcardService.class.getName());
                    ComponentName comp = startIntent.resolveSystemService(SmartcardSystemService.this.mContext.getPackageManager(), 0);
                    startIntent.setComponent(comp);
                    if (comp != null) {
                        boolean result = SmartcardSystemService.this.mContext.bindService(startIntent, SmartcardSystemService.this.mConnection, 1);
                        Log.i(SmartcardSystemService.SMARTCARD_SERVICE_TAG, "bindService result: " + result);
                        if (result) {
                            SmartcardSystemService.this.mBindStatus = 1;
                        }
                    } else {
                        Log.i(SmartcardSystemService.SMARTCARD_SERVICE_TAG, "SmartcardService not exist");
                        SmartcardSystemService.this.mBindStatus = 0;
                    }
                }
            }
        });
    }

    public boolean connectSmartCardService() throws RemoteException {
        if (checkAccessSmartcardAPI()) {
            Log.i(SMARTCARD_SERVICE_TAG, "connectSmartCardService");
            if (this.mIccCardReady) {
                Log.i(SMARTCARD_SERVICE_TAG, "Icc Card Ready");
                if (this.mBindStatus == 0) {
                    bindSmartCardService();
                }
            }
            Log.i(SMARTCARD_SERVICE_TAG, "bind result:" + this.mBindStatus);
            if (this.mBindStatus == 2) {
                return true;
            }
            return false;
        }
        throw new SecurityException("Permission denied for accessing Smartcard API");
    }

    private boolean checkAccessSmartcardAPI() {
        if (Binder.getCallingUid() < 10000) {
            return true;
        }
        return false;
    }
}
