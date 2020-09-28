package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.notification.NotificationData;
import java.util.Map;

public class DvNotificationService extends VirtualManager {
    public static final int ADD_NOTIFY = 1;
    public static final int CRITICAL_VIBRATION = 5;
    public static final int IMPOTRANT_VIBRATION = 4;
    public static final int NORMAL_VIBRATION = 3;
    private static final Object NOTIFICATION_LOCK = new Object();
    public static final int NOT_VIBRATION = 1;
    public static final int REMOVE_NOTIFY = 3;
    public static final int SLIGHT_VIBRATION = 2;
    private static final String TAG = "DvNotificationService";
    public static final int UPDATE_NOTIFY = 2;
    private static DMSDPListener sDmsdpListener = null;
    private static DvNotificationService sNotificationService = null;
    private DMSDPAdapter mDMSDPAdapter;

    private DvNotificationService() {
    }

    static DvNotificationService getInstance() {
        DvNotificationService dvNotificationService;
        synchronized (DvNotificationService.class) {
            if (sNotificationService == null) {
                sNotificationService = new DvNotificationService();
            }
            dvNotificationService = sNotificationService;
        }
        return dvNotificationService;
    }

    public int doNotification(String deviceId, int notificationId, DvNotification notification, int operationMode) {
        synchronized (NOTIFICATION_LOCK) {
            if (notification == null || deviceId == null || notificationId < 0) {
                HwLog.e(TAG, "Param invalid");
                return -2;
            } else if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "No service");
                return -11;
            } else {
                HwLog.d(TAG, "dvNotify");
                NotificationData notificationData = new NotificationData();
                notificationData.setPackageName(notification.getPackageName());
                notificationData.setTemplate(notification.getTemplate());
                notificationData.setDate(notification.getDate());
                notificationData.setVibrate(notification.getVibrate());
                if (notification.getTemplate() == 1) {
                    notificationData.setIconId(notification.getIconId());
                    notificationData.setTitle(notification.getTitle());
                    notificationData.setSubtitle(notification.getSubtitle());
                    notificationData.setContent(notification.getContent());
                } else if (notification.getTemplate() == 2) {
                    notificationData.setGuideDistance(notification.getGuideDistance());
                    notificationData.setGuideDistanceUnit(notification.getGuideDistanceUnit());
                    notificationData.setGuideDirectionId(notification.getGuideDirectionId());
                    notificationData.setGuideText(notification.getGuideText());
                } else {
                    HwLog.e(TAG, "Param template is invalid");
                    return -2;
                }
                return this.mDMSDPAdapter.sendNotification(deviceId, notificationId, notificationData, operationMode);
            }
        }
    }

    private static DMSDPListener getDmsdpListener() {
        synchronized (NOTIFICATION_LOCK) {
            if (sDmsdpListener != null) {
                return sDmsdpListener;
            }
            sDmsdpListener = new DMSDPListener() {
                /* class com.huawei.dmsdp.devicevirtualization.DvNotificationService.AnonymousClass1 */

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceChange(DMSDPDevice dmsdpDevice, int event, Map<String, Object> map) {
                }

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> map) {
                }
            };
            return sDmsdpListener;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService dmsdpService) {
        synchronized (NOTIFICATION_LOCK) {
            if (dmsdpService != null) {
                this.mDMSDPAdapter = dmsdpService.getDMSDPAdapter();
                if (this.mDMSDPAdapter != null) {
                    this.mDMSDPAdapter.registerDMSDPListener(5, getDmsdpListener());
                } else {
                    HwLog.e(TAG, "dmsdpAdapter is null when register dmsdpListener");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onDisConnect() {
        synchronized (NOTIFICATION_LOCK) {
            if (this.mDMSDPAdapter != null) {
                this.mDMSDPAdapter.unRegisterDMSDPListener(5, getDmsdpListener());
            }
            this.mDMSDPAdapter = null;
        }
    }
}
