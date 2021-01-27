package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DMSDPVirtualDevice;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class LocalVirtualDeviceAdapter {
    private static final Object LISTENER_LOCK = new Object();
    private static final int SWITCH_TO_LOCAL = 1;
    private static final int SWITCH_TO_REMOTE = 0;
    private static final String TAG = "LocalVirtualDeviceAdapter";
    private static DMSDPListener sDmsdpListener = null;
    private static VirtualService sDmsdpService;
    private static LocalVirtualDeviceAdapter sLocalVirtualDeviceAdapter;
    private static ILocalVirtualDeviceObserver sStateChangeCallback;
    private static EnumSet<VirtualDeviceType> sTypeFilters;

    private LocalVirtualDeviceAdapter() {
    }

    static LocalVirtualDeviceAdapter getInstance() {
        LocalVirtualDeviceAdapter localVirtualDeviceAdapter;
        synchronized (LocalVirtualDeviceAdapter.class) {
            if (sLocalVirtualDeviceAdapter == null) {
                sLocalVirtualDeviceAdapter = new LocalVirtualDeviceAdapter();
            }
            localVirtualDeviceAdapter = sLocalVirtualDeviceAdapter;
        }
        return localVirtualDeviceAdapter;
    }

    static void onConnect(VirtualService dmsdpService) {
        sDmsdpService = dmsdpService;
        VirtualService virtualService = sDmsdpService;
        if (virtualService != null) {
            DMSDPAdapter dmsdpAdapter = virtualService.getDMSDPAdapter();
            if (dmsdpAdapter != null) {
                dmsdpAdapter.registerDMSDPListener(5, getDmsdpListener());
            } else {
                HwLog.e(TAG, "dmsdpAdapter is null when register dmsdpListener");
            }
        }
    }

    static void onDisConnect() {
        VirtualService virtualService = sDmsdpService;
        if (virtualService != null) {
            DMSDPAdapter dmsdpAdapter = virtualService.getDMSDPAdapter();
            if (dmsdpAdapter != null) {
                dmsdpAdapter.unRegisterDMSDPListener(5, getDmsdpListener());
            } else {
                HwLog.e(TAG, "dmsdpAdapter is null when unRegister dmsdpListener");
            }
        } else {
            HwLog.e(TAG, "sDmsdpService is null when unRegister dmsdpListener");
        }
        sDmsdpService = null;
        sDmsdpListener = null;
        sStateChangeCallback = null;
    }

    public static int registerCallback(ILocalVirtualDeviceObserver callback, EnumSet<VirtualDeviceType> typeFilter) {
        if (callback == null) {
            HwLog.e(TAG, "registerCallback is null");
            return -2;
        } else if (typeFilter == null || typeFilter.size() == 0) {
            HwLog.e(TAG, "typeFilter is invalid");
            return -2;
        } else if (sDmsdpService == null) {
            HwLog.e(TAG, "sDmsdpService is null");
            return -2;
        } else {
            sStateChangeCallback = callback;
            sTypeFilters = typeFilter;
            List<VirtualDeviceInfo> listDeviceInfos = getVirtualDeviceStatus(sTypeFilters);
            if (listDeviceInfos == null) {
                return 0;
            }
            for (VirtualDeviceInfo virtualDeviceInfo : listDeviceInfos) {
                sStateChangeCallback.onDeviceStateChange(virtualDeviceInfo);
            }
            return 0;
        }
    }

    public static void unRegisterCallBack(ILocalVirtualDeviceObserver callback) {
        HwLog.i(TAG, "unRegisterCallBack");
        sStateChangeCallback = null;
    }

    public static int switchToRemoteAdapter(VirtualDeviceType type, String virtualDeviceId) {
        if (type == null || virtualDeviceId == null) {
            return -2;
        }
        VirtualService virtualService = sDmsdpService;
        if (virtualService == null) {
            HwLog.e(TAG, "switchToRemoteAdapter:sDmsdpService is null");
            return -11;
        }
        DMSDPAdapter dmsdpAdapter = virtualService.getDMSDPAdapter();
        if (dmsdpAdapter == null) {
            HwLog.e(TAG, "switchToRemoteAdapter:dmsdpAdapter is null");
            return -11;
        } else if (AnonymousClass2.$SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[type.ordinal()] != 1) {
            return -1;
        } else {
            return dmsdpAdapter.switchModem(virtualDeviceId, 0, null, 0);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.dmsdp.devicevirtualization.LocalVirtualDeviceAdapter$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType = new int[VirtualDeviceType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[VirtualDeviceType.MODEM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public static int switchToRemote(DMSDPDeviceService service, VirtualDeviceType type) {
        if (service == null || type == null) {
            HwLog.e(TAG, "switchToRemote invalid argument");
            return -2;
        }
        VirtualService virtualService = sDmsdpService;
        if (virtualService != null) {
            DMSDPAdapter dmsdpAdapter = virtualService.getDMSDPAdapter();
            if (dmsdpAdapter == null) {
                HwLog.e(TAG, "startDeviceService:dmsdpAdapter is null");
                return -11;
            } else if (AnonymousClass2.$SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[type.ordinal()] != 1) {
                return -1;
            } else {
                return dmsdpAdapter.switchModem(service.getServiceId(), 0, null, 0);
            }
        } else {
            HwLog.e(TAG, "startDeviceService:sDmsdpService is null");
            return -11;
        }
    }

    public static int switchToRemote(VirtualDeviceType type, String virtualDeviceId) {
        if (type == null || virtualDeviceId == null) {
            return -2;
        }
        if (AnonymousClass2.$SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[type.ordinal()] != 1) {
            return -1;
        }
        return VirtualModemDevice.switchToRemote(virtualDeviceId);
    }

    public static int switchToLocalAdapter(VirtualDeviceType type, String deviceId) {
        if (type == null || deviceId == null) {
            return -2;
        }
        VirtualService virtualService = sDmsdpService;
        if (virtualService != null) {
            DMSDPAdapter dmsdpAdapter = virtualService.getDMSDPAdapter();
            if (dmsdpAdapter == null) {
                HwLog.e(TAG, "startDeviceService:dmsdpAdapter is null");
                return -11;
            } else if (AnonymousClass2.$SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[type.ordinal()] != 1) {
                return -1;
            } else {
                return dmsdpAdapter.switchModem(deviceId, 1, null, 0);
            }
        } else {
            HwLog.e(TAG, "startDeviceService:sDmsdpService is null");
            return -11;
        }
    }

    public static int switchToLocal(VirtualDeviceType type, String virtualDeviceId) {
        if (type == null || virtualDeviceId == null) {
            return -2;
        }
        if (AnonymousClass2.$SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[type.ordinal()] != 1) {
            return -1;
        }
        return VirtualModemDevice.switchToLocal(virtualDeviceId);
    }

    public static List<VirtualDeviceInfo> getVirtualDeviceInfo(VirtualDeviceType type) {
        HwLog.i(TAG, "getVirtualDeviceInfo");
        if (type == null) {
            HwLog.e(TAG, "getVirtualDeviceInfo type is null");
            return null;
        }
        VirtualService virtualService = sDmsdpService;
        if (virtualService == null) {
            HwLog.e(TAG, "sDmsdpService is null when getVirtualDeviceInfo");
            return null;
        }
        DMSDPAdapter dmsdpAdapter = virtualService.getDMSDPAdapter();
        if (dmsdpAdapter == null) {
            HwLog.e(TAG, "dmsdpAdapter is null when getVirtualDeviceInfo");
            return null;
        } else if (!type.equals(VirtualDeviceType.MODEM)) {
            return null;
        } else {
            List<DMSDPVirtualDevice> virtualDeviceList = new ArrayList<>(0);
            dmsdpAdapter.getModemStatus(virtualDeviceList);
            if (virtualDeviceList.size() != 0) {
                return convertToVirtualDeviceInfo(VirtualDeviceType.MODEM, virtualDeviceList);
            }
            HwLog.i(TAG, "virtualDeviceList is invalid");
            return null;
        }
    }

    public static List<VirtualDeviceInfo> convertToVirtualDeviceInfo(VirtualDeviceType type, List<DMSDPVirtualDevice> virtualDeviceList) {
        List<VirtualDeviceInfo> listVirtualDevices = new ArrayList<>(0);
        if (type == null || virtualDeviceList == null) {
            return listVirtualDevices;
        }
        for (DMSDPVirtualDevice service : virtualDeviceList) {
            VirtualDeviceInfo curVirtualDevice = convertToVirtualDeviceInfo(type, service);
            if (curVirtualDevice != null) {
                listVirtualDevices.add(curVirtualDevice);
            } else {
                HwLog.e(TAG, "curVirtualDevice is null");
            }
        }
        return listVirtualDevices;
    }

    public static VirtualDeviceInfo convertToVirtualDeviceInfo(VirtualDeviceType type, DMSDPDeviceService virtualService, VirtualDeviceState state) {
        return new VirtualDeviceInfo(type, virtualService.getDeviceId(), virtualService.getDeviceName(), state);
    }

    public static VirtualDeviceInfo convertToVirtualDeviceInfo(VirtualDeviceType type, DMSDPVirtualDevice virtualDevice) {
        if (virtualDevice == null || type == null) {
            return null;
        }
        HwLog.i(TAG, "status:" + virtualDevice.getModemStatus());
        int status = virtualDevice.getModemStatus();
        if (status == 221) {
            return new VirtualDeviceInfo(type, virtualDevice.getDeviceId(), virtualDevice.getDeviceName(), VirtualDeviceState.READY);
        }
        if (status == 220) {
            return new VirtualDeviceInfo(type, virtualDevice.getDeviceId(), virtualDevice.getDeviceName(), VirtualDeviceState.INVALID);
        }
        if (status == 222) {
            return new VirtualDeviceInfo(type, virtualDevice.getDeviceId(), virtualDevice.getDeviceName(), VirtualDeviceState.RUNNING);
        }
        if (status == 223) {
            return new VirtualDeviceInfo(type, virtualDevice.getDeviceId(), virtualDevice.getDeviceName(), VirtualDeviceState.BUSY);
        }
        HwLog.e(TAG, "status is invalid :" + status);
        return null;
    }

    private static DMSDPListener getDmsdpListener() {
        synchronized (LISTENER_LOCK) {
            if (sDmsdpListener != null) {
                return sDmsdpListener;
            }
            sDmsdpListener = new DMSDPListener() {
                /* class com.huawei.dmsdp.devicevirtualization.LocalVirtualDeviceAdapter.AnonymousClass1 */

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceChange(DMSDPDevice dmsdpDevice, int event, Map<String, Object> map) {
                }

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> info) {
                    HwLog.i(LocalVirtualDeviceAdapter.TAG, "onDeviceServiceChange event:" + event);
                    LocalVirtualDeviceAdapter.onServiceChange(dmsdpDeviceService, event, info);
                }
            };
            return sDmsdpListener;
        }
    }

    /* access modifiers changed from: private */
    public static void onServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> info) {
        HwLog.i(TAG, "ServiceChange type:" + dmsdpDeviceService.getServiceType() + " event:" + Integer.toString(event));
        deviceServiceChangeHandler(dmsdpDeviceService, event, info);
    }

    private static void deviceServiceChangeHandler(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> map) {
        if (sStateChangeCallback == null) {
            HwLog.e(TAG, "sStateChangeCallback is null when deviceServiceChangeHandler");
        } else if (dmsdpDeviceService.getServiceType() == 1024) {
            VirtualModemDevice.eventHandle(sStateChangeCallback, dmsdpDeviceService, event);
        }
    }

    private static List<VirtualDeviceInfo> getVirtualDeviceStatus(EnumSet<VirtualDeviceType> typeFilter) {
        if (typeFilter == null) {
            HwLog.i(TAG, "getVirtualDeviceStatus typeFilter is null");
            return null;
        }
        List<VirtualDeviceInfo> listVirtualDeviceInfos = new ArrayList<>(0);
        Iterator it = typeFilter.iterator();
        while (it.hasNext()) {
            if (AnonymousClass2.$SwitchMap$com$huawei$dmsdp$devicevirtualization$VirtualDeviceType[((VirtualDeviceType) it.next()).ordinal()] == 1) {
                List<VirtualDeviceInfo> modemList = VirtualModemDevice.getModemStatus();
                if (modemList == null || modemList.size() == 0) {
                    HwLog.i(TAG, "modemList is invalid");
                } else {
                    listVirtualDeviceInfos.addAll(modemList);
                }
            }
        }
        return listVirtualDeviceInfos;
    }
}
