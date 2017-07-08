package com.android.server.midi;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.media.midi.IBluetoothMidiService;
import android.media.midi.IMidiDeviceListener;
import android.media.midi.IMidiDeviceOpenCallback;
import android.media.midi.IMidiDeviceServer;
import android.media.midi.IMidiManager.Stub;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MidiService extends Stub {
    private static final MidiDeviceInfo[] EMPTY_DEVICE_INFO_ARRAY = null;
    private static final String[] EMPTY_STRING_ARRAY = null;
    private static final String TAG = "MidiService";
    private final HashMap<BluetoothDevice, Device> mBluetoothDevices;
    private int mBluetoothServiceUid;
    private final HashMap<IBinder, Client> mClients;
    private final Context mContext;
    private final HashMap<MidiDeviceInfo, Device> mDevicesByInfo;
    private final HashMap<IBinder, Device> mDevicesByServer;
    private int mNextDeviceId;
    private final PackageManager mPackageManager;
    private final PackageMonitor mPackageMonitor;

    private final class Client implements DeathRecipient {
        private final HashMap<IBinder, DeviceConnection> mDeviceConnections;
        private final HashMap<IBinder, IMidiDeviceListener> mListeners;
        private final int mPid;
        private final IBinder mToken;
        private final int mUid;

        public Client(IBinder token) {
            this.mListeners = new HashMap();
            this.mDeviceConnections = new HashMap();
            this.mToken = token;
            this.mUid = Binder.getCallingUid();
            this.mPid = Binder.getCallingPid();
        }

        public int getUid() {
            return this.mUid;
        }

        public void addListener(IMidiDeviceListener listener) {
            this.mListeners.put(listener.asBinder(), listener);
        }

        public void removeListener(IMidiDeviceListener listener) {
            this.mListeners.remove(listener.asBinder());
            if (this.mListeners.size() == 0 && this.mDeviceConnections.size() == 0) {
                close();
            }
        }

        public void addDeviceConnection(Device device, IMidiDeviceOpenCallback callback) {
            DeviceConnection connection = new DeviceConnection(device, this, callback);
            this.mDeviceConnections.put(connection.getToken(), connection);
            device.addDeviceConnection(connection);
        }

        public void removeDeviceConnection(IBinder token) {
            DeviceConnection connection = (DeviceConnection) this.mDeviceConnections.remove(token);
            if (connection != null) {
                connection.getDevice().removeDeviceConnection(connection);
            }
            if (this.mListeners.size() == 0 && this.mDeviceConnections.size() == 0) {
                close();
            }
        }

        public void removeDeviceConnection(DeviceConnection connection) {
            this.mDeviceConnections.remove(connection.getToken());
            if (this.mListeners.size() == 0 && this.mDeviceConnections.size() == 0) {
                close();
            }
        }

        public void deviceAdded(Device device) {
            if (device.isUidAllowed(this.mUid)) {
                MidiDeviceInfo deviceInfo = device.getDeviceInfo();
                try {
                    for (IMidiDeviceListener listener : this.mListeners.values()) {
                        listener.onDeviceAdded(deviceInfo);
                    }
                } catch (RemoteException e) {
                    Log.e(MidiService.TAG, "remote exception", e);
                }
            }
        }

        public void deviceRemoved(Device device) {
            if (device.isUidAllowed(this.mUid)) {
                MidiDeviceInfo deviceInfo = device.getDeviceInfo();
                try {
                    for (IMidiDeviceListener listener : this.mListeners.values()) {
                        listener.onDeviceRemoved(deviceInfo);
                    }
                } catch (RemoteException e) {
                    Log.e(MidiService.TAG, "remote exception", e);
                }
            }
        }

        public void deviceStatusChanged(Device device, MidiDeviceStatus status) {
            if (device.isUidAllowed(this.mUid)) {
                try {
                    for (IMidiDeviceListener listener : this.mListeners.values()) {
                        listener.onDeviceStatusChanged(status);
                    }
                } catch (RemoteException e) {
                    Log.e(MidiService.TAG, "remote exception", e);
                }
            }
        }

        private void close() {
            synchronized (MidiService.this.mClients) {
                MidiService.this.mClients.remove(this.mToken);
                this.mToken.unlinkToDeath(this, 0);
            }
            for (DeviceConnection connection : this.mDeviceConnections.values()) {
                connection.getDevice().removeDeviceConnection(connection);
            }
        }

        public void binderDied() {
            Log.d(MidiService.TAG, "Client died: " + this);
            close();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Client: UID: ");
            sb.append(this.mUid);
            sb.append(" PID: ");
            sb.append(this.mPid);
            sb.append(" listener count: ");
            sb.append(this.mListeners.size());
            sb.append(" Device Connections:");
            for (DeviceConnection connection : this.mDeviceConnections.values()) {
                sb.append(" <device ");
                sb.append(connection.getDevice().getDeviceInfo().getId());
                sb.append(">");
            }
            return sb.toString();
        }
    }

    private final class Device implements DeathRecipient {
        private final BluetoothDevice mBluetoothDevice;
        private final ArrayList<DeviceConnection> mDeviceConnections;
        private MidiDeviceInfo mDeviceInfo;
        private MidiDeviceStatus mDeviceStatus;
        private IMidiDeviceServer mServer;
        private ServiceConnection mServiceConnection;
        private final ServiceInfo mServiceInfo;
        private final int mUid;

        public Device(IMidiDeviceServer server, MidiDeviceInfo deviceInfo, ServiceInfo serviceInfo, int uid) {
            this.mDeviceConnections = new ArrayList();
            this.mDeviceInfo = deviceInfo;
            this.mServiceInfo = serviceInfo;
            this.mUid = uid;
            this.mBluetoothDevice = (BluetoothDevice) deviceInfo.getProperties().getParcelable("bluetooth_device");
            setDeviceServer(server);
        }

        public Device(BluetoothDevice bluetoothDevice) {
            this.mDeviceConnections = new ArrayList();
            this.mBluetoothDevice = bluetoothDevice;
            this.mServiceInfo = null;
            this.mUid = MidiService.this.mBluetoothServiceUid;
        }

        private void setDeviceServer(IMidiDeviceServer server) {
            IBinder binder;
            if (server != null) {
                if (this.mServer != null) {
                    Log.e(MidiService.TAG, "mServer already set in setDeviceServer");
                    return;
                }
                binder = server.asBinder();
                try {
                    if (this.mDeviceInfo == null) {
                        this.mDeviceInfo = server.getDeviceInfo();
                    }
                    binder.linkToDeath(this, 0);
                    this.mServer = server;
                    MidiService.this.mDevicesByServer.put(binder, this);
                } catch (RemoteException e) {
                    this.mServer = null;
                    return;
                }
            } else if (this.mServer != null) {
                server = this.mServer;
                this.mServer = null;
                binder = server.asBinder();
                MidiService.this.mDevicesByServer.remove(binder);
                try {
                    server.closeDevice();
                    binder.unlinkToDeath(this, 0);
                } catch (RemoteException e2) {
                }
            }
            if (this.mDeviceConnections != null) {
                for (DeviceConnection connection : this.mDeviceConnections) {
                    connection.notifyClient(server);
                }
            }
        }

        public MidiDeviceInfo getDeviceInfo() {
            return this.mDeviceInfo;
        }

        public void setDeviceInfo(MidiDeviceInfo deviceInfo) {
            this.mDeviceInfo = deviceInfo;
        }

        public MidiDeviceStatus getDeviceStatus() {
            return this.mDeviceStatus;
        }

        public void setDeviceStatus(MidiDeviceStatus status) {
            this.mDeviceStatus = status;
        }

        public IMidiDeviceServer getDeviceServer() {
            return this.mServer;
        }

        public ServiceInfo getServiceInfo() {
            return this.mServiceInfo;
        }

        public String getPackageName() {
            return this.mServiceInfo == null ? null : this.mServiceInfo.packageName;
        }

        public int getUid() {
            return this.mUid;
        }

        public boolean isUidAllowed(int uid) {
            return !this.mDeviceInfo.isPrivate() || this.mUid == uid;
        }

        public void addDeviceConnection(DeviceConnection connection) {
            synchronized (this.mDeviceConnections) {
                if (this.mServer != null) {
                    this.mDeviceConnections.add(connection);
                    connection.notifyClient(this.mServer);
                } else if (this.mServiceConnection != null || (this.mServiceInfo == null && this.mBluetoothDevice == null)) {
                    Log.e(MidiService.TAG, "No way to connect to device in addDeviceConnection");
                    connection.notifyClient(null);
                } else {
                    Intent intent;
                    this.mDeviceConnections.add(connection);
                    this.mServiceConnection = new ServiceConnection() {
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            IMidiDeviceServer server = null;
                            if (Device.this.mBluetoothDevice != null) {
                                try {
                                    server = IMidiDeviceServer.Stub.asInterface(IBluetoothMidiService.Stub.asInterface(service).addBluetoothDevice(Device.this.mBluetoothDevice));
                                } catch (RemoteException e) {
                                    Log.e(MidiService.TAG, "Could not call addBluetoothDevice()", e);
                                }
                            } else {
                                server = IMidiDeviceServer.Stub.asInterface(service);
                            }
                            Device.this.setDeviceServer(server);
                        }

                        public void onServiceDisconnected(ComponentName name) {
                            Device.this.setDeviceServer(null);
                            Device.this.mServiceConnection = null;
                        }
                    };
                    if (this.mBluetoothDevice != null) {
                        intent = new Intent("android.media.midi.BluetoothMidiService");
                        intent.setComponent(new ComponentName("com.android.bluetoothmidiservice", "com.android.bluetoothmidiservice.BluetoothMidiService"));
                    } else {
                        intent = new Intent("android.media.midi.MidiDeviceService");
                        intent.setComponent(new ComponentName(this.mServiceInfo.packageName, this.mServiceInfo.name));
                    }
                    if (!MidiService.this.mContext.bindService(intent, this.mServiceConnection, 1)) {
                        Log.e(MidiService.TAG, "Unable to bind service: " + intent);
                        setDeviceServer(null);
                        this.mServiceConnection = null;
                    }
                }
            }
        }

        public void removeDeviceConnection(DeviceConnection connection) {
            synchronized (this.mDeviceConnections) {
                this.mDeviceConnections.remove(connection);
                if (this.mDeviceConnections.size() == 0 && this.mServiceConnection != null) {
                    MidiService.this.mContext.unbindService(this.mServiceConnection);
                    this.mServiceConnection = null;
                    if (this.mBluetoothDevice != null) {
                        synchronized (MidiService.this.mDevicesByInfo) {
                            closeLocked();
                        }
                    } else {
                        setDeviceServer(null);
                    }
                }
            }
        }

        public void closeLocked() {
            synchronized (this.mDeviceConnections) {
                for (DeviceConnection connection : this.mDeviceConnections) {
                    connection.getClient().removeDeviceConnection(connection);
                }
                this.mDeviceConnections.clear();
            }
            setDeviceServer(null);
            if (this.mServiceInfo == null) {
                MidiService.this.removeDeviceLocked(this);
            } else {
                this.mDeviceStatus = new MidiDeviceStatus(this.mDeviceInfo);
            }
            if (this.mBluetoothDevice != null) {
                MidiService.this.mBluetoothDevices.remove(this.mBluetoothDevice);
            }
        }

        public void binderDied() {
            Log.d(MidiService.TAG, "Device died: " + this);
            synchronized (MidiService.this.mDevicesByInfo) {
                closeLocked();
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Device Info: ");
            sb.append(this.mDeviceInfo);
            sb.append(" Status: ");
            sb.append(this.mDeviceStatus);
            sb.append(" UID: ");
            sb.append(this.mUid);
            sb.append(" DeviceConnection count: ");
            sb.append(this.mDeviceConnections.size());
            sb.append(" mServiceConnection: ");
            sb.append(this.mServiceConnection);
            return sb.toString();
        }
    }

    private final class DeviceConnection {
        private IMidiDeviceOpenCallback mCallback;
        private final Client mClient;
        private final Device mDevice;
        private final IBinder mToken;

        public DeviceConnection(Device device, Client client, IMidiDeviceOpenCallback callback) {
            this.mToken = new Binder();
            this.mDevice = device;
            this.mClient = client;
            this.mCallback = callback;
        }

        public Device getDevice() {
            return this.mDevice;
        }

        public Client getClient() {
            return this.mClient;
        }

        public IBinder getToken() {
            return this.mToken;
        }

        public void notifyClient(IMidiDeviceServer deviceServer) {
            if (this.mCallback != null) {
                try {
                    this.mCallback.onDeviceOpened(deviceServer, deviceServer == null ? null : this.mToken);
                } catch (RemoteException e) {
                }
                this.mCallback = null;
            }
        }

        public String toString() {
            return "DeviceConnection Device ID: " + this.mDevice.getDeviceInfo().getId();
        }
    }

    public static class Lifecycle extends SystemService {
        private MidiService mMidiService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mMidiService = new MidiService(getContext());
            publishBinderService("midi", this.mMidiService);
        }

        public void onUnlockUser(int userHandle) {
            if (userHandle == 0) {
                this.mMidiService.onUnlockUser();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.midi.MidiService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.midi.MidiService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.midi.MidiService.<clinit>():void");
    }

    private Client getClient(IBinder token) {
        Client client;
        synchronized (this.mClients) {
            client = (Client) this.mClients.get(token);
            if (client == null) {
                client = new Client(token);
                try {
                    token.linkToDeath(client, 0);
                    this.mClients.put(token, client);
                } catch (RemoteException e) {
                    return null;
                }
            }
        }
        return client;
    }

    public MidiService(Context context) {
        this.mClients = new HashMap();
        this.mDevicesByInfo = new HashMap();
        this.mBluetoothDevices = new HashMap();
        this.mDevicesByServer = new HashMap();
        this.mNextDeviceId = 1;
        this.mPackageMonitor = new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                MidiService.this.addPackageDeviceServers(packageName);
            }

            public void onPackageModified(String packageName) {
                MidiService.this.removePackageDeviceServers(packageName);
                MidiService.this.addPackageDeviceServers(packageName);
            }

            public void onPackageRemoved(String packageName, int uid) {
                MidiService.this.removePackageDeviceServers(packageName);
            }
        };
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mBluetoothServiceUid = -1;
    }

    private void onUnlockUser() {
        PackageInfo packageInfo;
        this.mPackageMonitor.register(this.mContext, null, true);
        List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentServices(new Intent("android.media.midi.MidiDeviceService"), DumpState.DUMP_PACKAGES);
        if (resolveInfos != null) {
            int count = resolveInfos.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo serviceInfo = ((ResolveInfo) resolveInfos.get(i)).serviceInfo;
                if (serviceInfo != null) {
                    addPackageDeviceServer(serviceInfo);
                }
            }
        }
        try {
            packageInfo = this.mPackageManager.getPackageInfo("com.android.bluetoothmidiservice", 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
        }
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            this.mBluetoothServiceUid = -1;
        } else {
            this.mBluetoothServiceUid = packageInfo.applicationInfo.uid;
        }
    }

    public void registerListener(IBinder token, IMidiDeviceListener listener) {
        Client client = getClient(token);
        if (client != null) {
            client.addListener(listener);
            updateStickyDeviceStatus(client.mUid, listener);
        }
    }

    public void unregisterListener(IBinder token, IMidiDeviceListener listener) {
        Client client = getClient(token);
        if (client != null) {
            client.removeListener(listener);
        }
    }

    private void updateStickyDeviceStatus(int uid, IMidiDeviceListener listener) {
        synchronized (this.mDevicesByInfo) {
            for (Device device : this.mDevicesByInfo.values()) {
                if (device.isUidAllowed(uid)) {
                    try {
                        MidiDeviceStatus status = device.getDeviceStatus();
                        if (status != null) {
                            listener.onDeviceStatusChanged(status);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote exception", e);
                    }
                }
            }
        }
    }

    public MidiDeviceInfo[] getDevices() {
        ArrayList<MidiDeviceInfo> deviceInfos = new ArrayList();
        int uid = Binder.getCallingUid();
        synchronized (this.mDevicesByInfo) {
            for (Device device : this.mDevicesByInfo.values()) {
                if (device.isUidAllowed(uid)) {
                    deviceInfos.add(device.getDeviceInfo());
                }
            }
        }
        return (MidiDeviceInfo[]) deviceInfos.toArray(EMPTY_DEVICE_INFO_ARRAY);
    }

    public void openDevice(IBinder token, MidiDeviceInfo deviceInfo, IMidiDeviceOpenCallback callback) {
        Client client = getClient(token);
        if (client != null) {
            Device device;
            synchronized (this.mDevicesByInfo) {
                device = (Device) this.mDevicesByInfo.get(deviceInfo);
                if (device == null) {
                    throw new IllegalArgumentException("device does not exist: " + deviceInfo);
                } else if (device.isUidAllowed(Binder.getCallingUid())) {
                } else {
                    throw new SecurityException("Attempt to open private device with wrong UID");
                }
            }
            long identity = Binder.clearCallingIdentity();
            try {
                client.addDeviceConnection(device, callback);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void openBluetoothDevice(IBinder token, BluetoothDevice bluetoothDevice, IMidiDeviceOpenCallback callback) {
        Client client = getClient(token);
        if (client != null) {
            Device device;
            synchronized (this.mDevicesByInfo) {
                device = (Device) this.mBluetoothDevices.get(bluetoothDevice);
                if (device == null) {
                    device = new Device(bluetoothDevice);
                    this.mBluetoothDevices.put(bluetoothDevice, device);
                }
            }
            long identity = Binder.clearCallingIdentity();
            try {
                client.addDeviceConnection(device, callback);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void closeDevice(IBinder clientToken, IBinder deviceToken) {
        Client client = getClient(clientToken);
        if (client != null) {
            client.removeDeviceConnection(deviceToken);
        }
    }

    public MidiDeviceInfo registerDeviceServer(IMidiDeviceServer server, int numInputPorts, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type) {
        int uid = Binder.getCallingUid();
        if (type == 1 && uid != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            throw new SecurityException("only system can create USB devices");
        } else if (type != 3 || uid == this.mBluetoothServiceUid) {
            MidiDeviceInfo addDeviceLocked;
            synchronized (this.mDevicesByInfo) {
                addDeviceLocked = addDeviceLocked(type, numInputPorts, numOutputPorts, inputPortNames, outputPortNames, properties, server, null, false, uid);
            }
            return addDeviceLocked;
        } else {
            throw new SecurityException("only MidiBluetoothService can create Bluetooth devices");
        }
    }

    public void unregisterDeviceServer(IMidiDeviceServer server) {
        synchronized (this.mDevicesByInfo) {
            Device device = (Device) this.mDevicesByServer.get(server.asBinder());
            if (device != null) {
                device.closeLocked();
            }
        }
    }

    public MidiDeviceInfo getServiceDeviceInfo(String packageName, String className) {
        synchronized (this.mDevicesByInfo) {
            for (Device device : this.mDevicesByInfo.values()) {
                ServiceInfo serviceInfo = device.getServiceInfo();
                if (serviceInfo != null && packageName.equals(serviceInfo.packageName) && className.equals(serviceInfo.name)) {
                    MidiDeviceInfo deviceInfo = device.getDeviceInfo();
                    return deviceInfo;
                }
            }
            return null;
        }
    }

    public MidiDeviceStatus getDeviceStatus(MidiDeviceInfo deviceInfo) {
        Device device = (Device) this.mDevicesByInfo.get(deviceInfo);
        if (device != null) {
            return device.getDeviceStatus();
        }
        throw new IllegalArgumentException("no such device for " + deviceInfo);
    }

    public void setDeviceStatus(IMidiDeviceServer server, MidiDeviceStatus status) {
        Device device = (Device) this.mDevicesByServer.get(server.asBinder());
        if (device == null) {
            return;
        }
        if (Binder.getCallingUid() != device.getUid()) {
            throw new SecurityException("setDeviceStatus() caller UID " + Binder.getCallingUid() + " does not match device's UID " + device.getUid());
        }
        device.setDeviceStatus(status);
        notifyDeviceStatusChanged(device, status);
    }

    private void notifyDeviceStatusChanged(Device device, MidiDeviceStatus status) {
        synchronized (this.mClients) {
            for (Client c : this.mClients.values()) {
                c.deviceStatusChanged(device, status);
            }
        }
    }

    private MidiDeviceInfo addDeviceLocked(int type, int numInputPorts, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, IMidiDeviceServer server, ServiceInfo serviceInfo, boolean isPrivate, int uid) {
        int id = this.mNextDeviceId;
        this.mNextDeviceId = id + 1;
        MidiDeviceInfo deviceInfo = new MidiDeviceInfo(type, id, numInputPorts, numOutputPorts, inputPortNames, outputPortNames, properties, isPrivate);
        if (server != null) {
            try {
                server.setDeviceInfo(deviceInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in setDeviceInfo()");
                return null;
            }
        }
        Device device = null;
        Object obj = null;
        if (type == 3) {
            obj = (BluetoothDevice) properties.getParcelable("bluetooth_device");
            device = (Device) this.mBluetoothDevices.get(obj);
            if (device != null) {
                device.setDeviceInfo(deviceInfo);
            }
        }
        if (device == null) {
            device = new Device(server, deviceInfo, serviceInfo, uid);
        }
        this.mDevicesByInfo.put(deviceInfo, device);
        if (obj != null) {
            this.mBluetoothDevices.put(obj, device);
        }
        synchronized (this.mClients) {
            for (Client c : this.mClients.values()) {
                c.deviceAdded(device);
            }
        }
        return deviceInfo;
    }

    private void removeDeviceLocked(Device device) {
        IMidiDeviceServer server = device.getDeviceServer();
        if (server != null) {
            this.mDevicesByServer.remove(server.asBinder());
        }
        this.mDevicesByInfo.remove(device.getDeviceInfo());
        synchronized (this.mClients) {
            for (Client c : this.mClients.values()) {
                c.deviceRemoved(device);
            }
        }
    }

    private void addPackageDeviceServers(String packageName) {
        try {
            ServiceInfo[] services = this.mPackageManager.getPackageInfo(packageName, 132).services;
            if (services != null) {
                for (ServiceInfo addPackageDeviceServer : services) {
                    addPackageDeviceServer(addPackageDeviceServer);
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "handlePackageUpdate could not find package " + packageName, e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addPackageDeviceServer(ServiceInfo serviceInfo) {
        XmlResourceParser xmlResourceParser = null;
        xmlResourceParser = serviceInfo.loadXmlMetaData(this.mPackageManager, "android.media.midi.MidiDeviceService");
        if (xmlResourceParser == null) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } else if ("android.permission.BIND_MIDI_DEVICE_SERVICE".equals(serviceInfo.permission)) {
            Bundle properties = null;
            int numInputPorts = 0;
            int numOutputPorts = 0;
            boolean isPrivate = false;
            ArrayList<String> inputPortNames = new ArrayList();
            ArrayList<String> outputPortNames = new ArrayList();
            while (true) {
                int eventType = xmlResourceParser.next();
                if (eventType == 1) {
                    break;
                } else if (eventType == 2) {
                    String tagName = xmlResourceParser.getName();
                    int count;
                    int i;
                    String name;
                    String value;
                    if ("device".equals(tagName)) {
                        if (properties != null) {
                            Log.w(TAG, "nested <device> elements in metadata for " + serviceInfo.packageName);
                        } else {
                            properties = new Bundle();
                            properties.putParcelable("service_info", serviceInfo);
                            numInputPorts = 0;
                            numOutputPorts = 0;
                            isPrivate = false;
                            count = xmlResourceParser.getAttributeCount();
                            for (i = 0; i < count; i++) {
                                name = xmlResourceParser.getAttributeName(i);
                                value = xmlResourceParser.getAttributeValue(i);
                                if ("private".equals(name)) {
                                    isPrivate = "true".equals(value);
                                } else {
                                    properties.putString(name, value);
                                }
                            }
                        }
                    } else if ("input-port".equals(tagName)) {
                        if (properties == null) {
                            Log.w(TAG, "<input-port> outside of <device> in metadata for " + serviceInfo.packageName);
                        } else {
                            numInputPorts++;
                            portName = null;
                            count = xmlResourceParser.getAttributeCount();
                            for (i = 0; i < count; i++) {
                                name = xmlResourceParser.getAttributeName(i);
                                value = xmlResourceParser.getAttributeValue(i);
                                if ("name".equals(name)) {
                                    portName = value;
                                    break;
                                }
                            }
                            inputPortNames.add(portName);
                        }
                    } else if ("output-port".equals(tagName)) {
                        if (properties == null) {
                            Log.w(TAG, "<output-port> outside of <device> in metadata for " + serviceInfo.packageName);
                        } else {
                            numOutputPorts++;
                            portName = null;
                            count = xmlResourceParser.getAttributeCount();
                            for (i = 0; i < count; i++) {
                                name = xmlResourceParser.getAttributeName(i);
                                value = xmlResourceParser.getAttributeValue(i);
                                if ("name".equals(name)) {
                                    portName = value;
                                    break;
                                }
                            }
                            outputPortNames.add(portName);
                        }
                    }
                } else if (eventType == 3) {
                    if ("device".equals(xmlResourceParser.getName()) && properties != null) {
                        if (numInputPorts == 0 && numOutputPorts == 0) {
                            Log.w(TAG, "<device> with no ports in metadata for " + serviceInfo.packageName);
                        } else {
                            int uid;
                            try {
                                uid = this.mPackageManager.getApplicationInfo(serviceInfo.packageName, 0).uid;
                            } catch (NameNotFoundException e) {
                                Log.e(TAG, "could not fetch ApplicationInfo for " + serviceInfo.packageName);
                            }
                            try {
                                synchronized (this.mDevicesByInfo) {
                                    addDeviceLocked(2, numInputPorts, numOutputPorts, (String[]) inputPortNames.toArray(EMPTY_STRING_ARRAY), (String[]) outputPortNames.toArray(EMPTY_STRING_ARRAY), properties, null, serviceInfo, isPrivate, uid);
                                }
                                properties = null;
                                inputPortNames.clear();
                                outputPortNames.clear();
                            } catch (Throwable e2) {
                                Log.w(TAG, "Unable to load component info " + serviceInfo.toString(), e2);
                                if (xmlResourceParser != null) {
                                    xmlResourceParser.close();
                                }
                            } catch (Throwable th) {
                                if (xmlResourceParser != null) {
                                    xmlResourceParser.close();
                                }
                            }
                        }
                    }
                } else {
                    continue;
                }
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } else {
            Log.w(TAG, "Skipping MIDI device service " + serviceInfo.packageName + ": it does not require the permission " + "android.permission.BIND_MIDI_DEVICE_SERVICE");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private void removePackageDeviceServers(String packageName) {
        synchronized (this.mDevicesByInfo) {
            Iterator<Device> iterator = this.mDevicesByInfo.values().iterator();
            while (iterator.hasNext()) {
                Device device = (Device) iterator.next();
                if (packageName.equals(device.getPackageName())) {
                    iterator.remove();
                    removeDeviceLocked(device);
                }
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("MIDI Manager State:");
        pw.increaseIndent();
        pw.println("Devices:");
        pw.increaseIndent();
        synchronized (this.mDevicesByInfo) {
            for (Device device : this.mDevicesByInfo.values()) {
                pw.println(device.toString());
            }
        }
        pw.decreaseIndent();
        pw.println("Clients:");
        pw.increaseIndent();
        synchronized (this.mClients) {
            for (Client client : this.mClients.values()) {
                pw.println(client.toString());
            }
        }
        pw.decreaseIndent();
    }
}
