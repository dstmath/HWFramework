package com.huawei.distributedgw;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.huawei.distributedgw.IDistributedGatewayService;
import com.huawei.distributedgw.IDistributedGatewayStateCallback;
import com.huawei.distributedgw.InternetBorrowingRequest;
import com.huawei.distributedgw.InternetSharingRequest;
import java.util.regex.Pattern;

public class DistributedGatewayManager {
    private static final String PARAM_IS_NULL = "param error, request null.";
    private static final String PATTERN_IP = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String REMOTE_SERVICE_NAME = IDistributedGatewayService.class.getSimpleName();
    private static final String SERVICE_IS_NULL = "service is null.";
    private static final String TAG = DistributedGatewayManager.class.getSimpleName();
    private final ArrayMap<DistributedGatewayStateCallback, IDistributedGatewayStateCallback> mListenerMap = new ArrayMap<>();
    private final Object mLockService = new Object();
    private volatile IDistributedGatewayService mService = null;

    public boolean enableInternetSharing(int deviceType, String entryInterfaceName) {
        if (!checkParams(deviceType, entryInterfaceName)) {
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "enable sharing failed: service is null.");
                return false;
            }
            return this.mService.enableInternetSharing(constructSharingRequest(new InternetSharingRequestEx(deviceType, entryInterfaceName)));
        } catch (RemoteException e) {
            Slog.e(TAG, "enable sharing: RemoteException.");
            return false;
        }
    }

    public boolean enableInternetSharing(InternetSharingRequestEx requestEx) {
        if (!checkParams(requestEx)) {
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "enable sharing failed: service is null.");
                return false;
            }
            return this.mService.enableInternetSharing(constructSharingRequest(requestEx));
        } catch (RemoteException e) {
            Slog.e(TAG, "enable sharing: RemoteException.");
            return false;
        }
    }

    public boolean disableInternetSharing(int deviceType, String entryInterfaceName) {
        if (!checkParams(deviceType, entryInterfaceName)) {
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "disable sharing failed: service is null.");
                return false;
            }
            return this.mService.disableInternetSharing(constructSharingRequest(new InternetSharingRequestEx(deviceType, entryInterfaceName)));
        } catch (RemoteException e) {
            Slog.e(TAG, "disable sharing: RemoteException.");
            return false;
        }
    }

    public boolean disableInternetSharing(InternetSharingRequestEx requestEx) {
        if (!checkParams(requestEx)) {
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "disable sharing failed: service is null.");
                return false;
            }
            return this.mService.disableInternetSharing(constructSharingRequest(requestEx));
        } catch (RemoteException e) {
            Slog.e(TAG, "disable sharing: RemoteException.");
            return false;
        }
    }

    public boolean isInternetSharing(int deviceType, String entryInterfaceName) {
        if (!checkParams(deviceType, entryInterfaceName)) {
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "is sharing failed: service is null.");
                return false;
            }
            return this.mService.isInternetSharing(constructSharingRequest(new InternetSharingRequestEx(deviceType, entryInterfaceName)));
        } catch (RemoteException e) {
            Slog.e(TAG, "is sharing: RemoteException.");
            return false;
        }
    }

    public boolean isInternetSharing(InternetSharingRequestEx requestEx) {
        if (!checkParams(requestEx)) {
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "is sharing failed: service is null.");
                return false;
            }
            return this.mService.isInternetSharing(constructSharingRequest(requestEx));
        } catch (RemoteException e) {
            Slog.e(TAG, "is sharing: RemoteException.");
            return false;
        }
    }

    public boolean enableInternetBorrowing(InternetBorrowingRequestEx requestEx) {
        if (requestEx == null) {
            Slog.e(TAG, PARAM_IS_NULL);
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "enable borrowing failed: service is null.");
                return false;
            }
            return this.mService.enableInternetBorrowing(constructBorrowingRequest(requestEx));
        } catch (RemoteException e) {
            Slog.e(TAG, "enable borrowing failed: RemoteException.");
            return false;
        }
    }

    public boolean disableInternetBorrowing(InternetBorrowingRequestEx requestEx) {
        if (requestEx == null) {
            Slog.e(TAG, PARAM_IS_NULL);
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "disable borrowing failed: service is null.");
                return false;
            }
            return this.mService.disableInternetBorrowing(constructBorrowingRequest(requestEx));
        } catch (RemoteException e) {
            Slog.e(TAG, "disable borrowing failed: RemoteException.");
            return false;
        }
    }

    public boolean isInternetBorrowing(InternetBorrowingRequestEx requestEx) {
        if (requestEx == null) {
            Slog.e(TAG, PARAM_IS_NULL);
            return false;
        }
        try {
            initService();
            if (this.mService == null) {
                Slog.e(TAG, "is borrowing failed: service is null.");
                return false;
            }
            return this.mService.isInternetBorrowing(constructBorrowingRequest(requestEx));
        } catch (RemoteException e) {
            Slog.e(TAG, "is borrowing failed: RemoteException.");
            return false;
        }
    }

    public boolean regStateCallback(DistributedGatewayStateCallback callback) {
        try {
            initService();
            if (callback != null) {
                if (this.mService != null) {
                    if (this.mListenerMap.containsKey(callback)) {
                        Slog.i(TAG, "register callback failed: callback is already registered.");
                        return true;
                    }
                    IDistributedGatewayStateCallback distributedGatewayStateCallback = constructCallback(callback);
                    if (distributedGatewayStateCallback == null) {
                        Slog.e(TAG, "register callback failed: construct callback is null.");
                        return false;
                    }
                    this.mListenerMap.put(callback, distributedGatewayStateCallback);
                    return this.mService.regStateCallback(distributedGatewayStateCallback);
                }
            }
            Slog.e(TAG, "register callback failed: callback or service is null.");
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "register callback failed: RemoteException.");
            return false;
        }
    }

    public boolean unregStateCallback(DistributedGatewayStateCallback callback) {
        if (callback == null) {
            Slog.e(TAG, "unregister callback failed: callback is null.");
            return false;
        } else if (!this.mListenerMap.containsKey(callback)) {
            Slog.i(TAG, "unregister callback failed: callback is not registered.");
            return true;
        } else {
            IDistributedGatewayStateCallback distributedGatewayStateCallback = this.mListenerMap.get(callback);
            if (distributedGatewayStateCallback == null) {
                Slog.e(TAG, "unregister callback failed: can not find callback.");
                return false;
            }
            try {
                initService();
                if (this.mService == null) {
                    Slog.e(TAG, SERVICE_IS_NULL);
                    return false;
                }
                boolean isSuccess = this.mService.unregStateCallback(distributedGatewayStateCallback);
                if (isSuccess) {
                    this.mListenerMap.remove(callback);
                }
                return isSuccess;
            } catch (RemoteException e) {
                Slog.e(TAG, "unregister callback failed: RemoteException.");
                return false;
            }
        }
    }

    private void initService() {
        if (this.mService == null) {
            synchronized (this.mLockService) {
                if (this.mService == null) {
                    this.mService = IDistributedGatewayService.Stub.asInterface(ServiceManager.getService(REMOTE_SERVICE_NAME));
                }
            }
        }
    }

    private IDistributedGatewayStateCallback constructCallback(final DistributedGatewayStateCallback callback) {
        if (callback != null) {
            return new IDistributedGatewayStateCallback.Stub() {
                /* class com.huawei.distributedgw.DistributedGatewayManager.AnonymousClass1 */

                @Override // com.huawei.distributedgw.IDistributedGatewayStateCallback
                public void onSharingStateChanged(InternetSharingRequest request, int state) {
                    if (request == null) {
                        Slog.e(DistributedGatewayManager.TAG, "callback construct failed: sharing request is null.");
                    } else {
                        callback.onSharingStateChanged(DistributedGatewayManager.constructSharingRequestEx(request), state);
                    }
                }

                @Override // com.huawei.distributedgw.IDistributedGatewayStateCallback
                public void onBorrowingStateChanged(InternetBorrowingRequest request, int state) {
                    if (request == null) {
                        Slog.e(DistributedGatewayManager.TAG, "callback construct failed: borrowing request is null.");
                    } else {
                        callback.onBorrowingStateChanged(DistributedGatewayManager.constructBorrowingRequestEx(request), state);
                    }
                }
            };
        }
        return null;
    }

    private boolean checkParams(InternetSharingRequestEx requestEx) {
        if (requestEx == null) {
            Slog.e(TAG, "check params failed, request param is null.");
            return false;
        } else if (!checkParams(requestEx.getDeviceType(), requestEx.getEntryIfaceName()) || !isValidIp(requestEx.getRequestIp())) {
            return false;
        } else {
            if (!TextUtils.isEmpty(requestEx.getExitIfaceName())) {
                return true;
            }
            Slog.e(TAG, "check params failed, exit interface name is empty.");
            return false;
        }
    }

    private boolean checkParams(int deviceType, String entryIfaceName) {
        if (TextUtils.isEmpty(entryIfaceName)) {
            Slog.e(TAG, "check params failed, entry interface name is empty.");
            return false;
        } else if (deviceType == 1 || deviceType == 2 || deviceType == 3 || deviceType == 5 || deviceType == 6) {
            return true;
        } else {
            String str = TAG;
            Slog.e(str, "unsupported device type: " + deviceType + ".");
            return false;
        }
    }

    private boolean isValidIp(String ip) {
        if (TextUtils.isEmpty(ip)) {
            Slog.e(TAG, "check params failed: ip is empty.");
            return false;
        } else if (Pattern.compile(PATTERN_IP).matcher(ip).matches()) {
            return true;
        } else {
            Slog.e(TAG, "check params failed: ip is invalid.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static InternetSharingRequestEx constructSharingRequestEx(InternetSharingRequest request) {
        InternetSharingRequestEx requestEx = new InternetSharingRequestEx(request.getDeviceType(), request.getEntryIfaceName());
        requestEx.setExitIfaceName(request.getExitIfaceName());
        requestEx.setExitIfaceType(request.getExitIfaceType());
        requestEx.setRequestIp(request.getRequestIp());
        return requestEx;
    }

    /* access modifiers changed from: private */
    public static InternetBorrowingRequestEx constructBorrowingRequestEx(InternetBorrowingRequest request) {
        return new InternetBorrowingRequestEx(request.getIfaceName(), request.getRouteIp());
    }

    private static InternetSharingRequest constructSharingRequest(InternetSharingRequestEx requestEx) {
        return new InternetSharingRequest.Builder().setEntryIfaceName(requestEx.getEntryIfaceName()).setExitIfaceName(requestEx.getExitIfaceName()).setRequestIp(requestEx.getRequestIp()).setDeviceType(requestEx.getDeviceType()).setOption(requestEx.getDeviceType()).build();
    }

    private static InternetBorrowingRequest constructBorrowingRequest(InternetBorrowingRequestEx requestEx) {
        return new InternetBorrowingRequest.Builder().setEntryIfaceName(requestEx.getIfaceName()).setRouteIp(requestEx.getRouteIp()).setServiceName(requestEx.getServiceName()).build();
    }
}
