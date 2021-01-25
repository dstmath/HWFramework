package com.huawei.servicehost;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.servicehost.IGlobalListener;
import com.huawei.servicehost.IIPEvent4CapNumber;
import com.huawei.servicehost.IIPEvent4GlobalResult;
import java.util.NoSuchElementException;

public class ServiceHostGlobalSession {
    public static final String CAPTURE_RESULT_EXIF = "captureResultExif";
    public static final String CAPTURE_RESULT_FILE_PATH = "captureResultFilePath";
    public static final String CAPTURE_RESULT_HEIGHT = "captureResultHeight";
    public static final String CAPTURE_RESULT_MODE = "captureResultMode";
    public static final String CAPTURE_RESULT_SIZE = "captureResultSize";
    public static final String CAPTURE_RESULT_STATUS = "captureResultStatus";
    public static final String CAPTURE_RESULT_WIDTH = "captureResultWidth";
    private static final String GLOBAL_EVENT_AVALIABLE_CAPTURE_NUM = "AvaliableCapNum";
    private static final String GLOBAL_EVENT_CAPTURE_RESULT = "result";
    private static final String TAG = "ServiceHostGlobalSession";
    private DeathListener mDeathListener;
    private final Object mDeathListenerLock;
    private DeathRecipientListener mDeathRecipientListener;
    private GlobalListener mGlobalListener;
    private final Object mGlobalListenerLock;
    private final Object mGlobalSessionLock;
    private IGlobalListener mInterfaceGlobalListener;
    private IGlobalSession mInterfaceGlobalSession;

    public interface DeathListener {
        void onServiceHostDied();
    }

    public interface GlobalListener {
        void onPictureSaved(Bundle bundle);

        void onSnapshotNumUpdate(int i);
    }

    private ServiceHostGlobalSession() {
        this.mInterfaceGlobalSession = null;
        this.mGlobalListener = null;
        this.mDeathListener = null;
        this.mGlobalSessionLock = new Object();
        this.mGlobalListenerLock = new Object();
        this.mDeathListenerLock = new Object();
        this.mDeathRecipientListener = new DeathRecipientListener();
        this.mInterfaceGlobalListener = new IGlobalListener.Stub() {
            /* class com.huawei.servicehost.ServiceHostGlobalSession.AnonymousClass1 */

            @Override // com.huawei.servicehost.IGlobalListener
            public void onGlobalEvent(IIPEvent iipEvent) throws RemoteException {
                GlobalListener globalListener;
                synchronized (ServiceHostGlobalSession.this.mGlobalListenerLock) {
                    globalListener = ServiceHostGlobalSession.this.mGlobalListener;
                }
                if (globalListener != null && iipEvent != null) {
                    IBinder obj = iipEvent.getObject();
                    String type = iipEvent.getType();
                    char c = 65535;
                    int hashCode = type.hashCode();
                    if (hashCode != -934426595) {
                        if (hashCode == 420468279 && type.equals(ServiceHostGlobalSession.GLOBAL_EVENT_AVALIABLE_CAPTURE_NUM)) {
                            c = 1;
                        }
                    } else if (type.equals(ServiceHostGlobalSession.GLOBAL_EVENT_CAPTURE_RESULT)) {
                        c = 0;
                    }
                    if (c == 0) {
                        IIPEvent4GlobalResult result = IIPEvent4GlobalResult.Stub.asInterface(obj);
                        if (result == null) {
                            Log.e(ServiceHostGlobalSession.TAG, "get result from global event return null!");
                            return;
                        }
                        String path = result.getFilePath();
                        int imageSaveState = result.getResult();
                        int captureMode = result.getCaptureMode();
                        int captureWidth = result.getCaptureWidth();
                        int captureHeight = result.getCaptureHeight();
                        int pictureSize = result.getPictureSize();
                        Log.i(ServiceHostGlobalSession.TAG, "Result:" + imageSaveState + ", capture size: " + captureWidth + " x " + captureHeight + ", mode:" + captureMode + ",picsize: " + pictureSize);
                        Bundle picInfo = new Bundle();
                        picInfo.putString(ServiceHostGlobalSession.CAPTURE_RESULT_FILE_PATH, path);
                        picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_STATUS, imageSaveState);
                        picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_MODE, captureMode);
                        picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_WIDTH, captureWidth);
                        picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_HEIGHT, captureHeight);
                        picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_SIZE, pictureSize);
                        picInfo.putString(ServiceHostGlobalSession.CAPTURE_RESULT_EXIF, "");
                        globalListener.onPictureSaved(picInfo);
                    } else if (c != 1) {
                        Log.e(ServiceHostGlobalSession.TAG, "unknown type: " + type);
                    } else {
                        IIPEvent4CapNumber number = IIPEvent4CapNumber.Stub.asInterface(obj);
                        if (number != null) {
                            int availableNum = number.getLowCapNumber();
                            if (availableNum <= 0) {
                                Log.d(ServiceHostGlobalSession.TAG, "update available number: " + availableNum);
                            }
                            globalListener.onSnapshotNumUpdate(availableNum);
                        }
                    }
                }
            }
        };
        Log.i(TAG, "construct global session");
    }

    private static class SingleGlobalSessionHolder {
        private static final ServiceHostGlobalSession INSTANCE = new ServiceHostGlobalSession();

        private SingleGlobalSessionHolder() {
        }
    }

    public static final ServiceHostGlobalSession getInstance() {
        return SingleGlobalSessionHolder.INSTANCE;
    }

    public void initialize(GlobalListener globalListener, DeathListener deathListener) {
        Log.i(TAG, "initialize global session.");
        synchronized (this.mGlobalSessionLock) {
            this.mInterfaceGlobalSession = ImageProcessManager.get().getGlobalSession();
            if (this.mInterfaceGlobalSession == null) {
                Log.d(TAG, "get global session return null!");
                return;
            }
            try {
                this.mInterfaceGlobalSession.addListener(this.mInterfaceGlobalListener);
            } catch (RemoteException e) {
                Log.e(TAG, "add global session listener exception: " + e.getMessage());
            }
            try {
                this.mInterfaceGlobalSession.asBinder().linkToDeath(this.mDeathRecipientListener, 0);
            } catch (RemoteException e2) {
                Log.e(TAG, "register binder die notification exception:" + e2.getMessage());
            } catch (Exception e3) {
                Log.e(TAG, "register binder die notification exception:" + e3.getMessage());
            }
            synchronized (this.mGlobalListenerLock) {
                this.mGlobalListener = globalListener;
            }
            synchronized (this.mDeathListenerLock) {
                this.mDeathListener = deathListener;
            }
            Log.i(TAG, "link servicehost death.");
        }
    }

    public void release() {
        Log.i(TAG, "release global session.");
        synchronized (this.mGlobalSessionLock) {
            if (this.mInterfaceGlobalSession == null) {
                Log.d(TAG, "global session is null!");
                return;
            }
            try {
                this.mInterfaceGlobalSession.removeListener(this.mInterfaceGlobalListener);
            } catch (RemoteException e) {
                Log.e(TAG, "remove global session listener exception: " + e.getMessage());
            }
            if (this.mDeathRecipientListener == null) {
                Log.d(TAG, "servicehost death listener is null!");
                return;
            }
            try {
                this.mInterfaceGlobalSession.asBinder().unlinkToDeath(this.mDeathRecipientListener, 0);
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "unlinkToDeath exception: " + e2.getMessage());
            } catch (Exception e3) {
                Log.e(TAG, "unlinkToDeath exception: " + e3.getMessage());
            }
            this.mInterfaceGlobalSession = null;
            synchronized (this.mGlobalListenerLock) {
                this.mGlobalListener = null;
            }
            synchronized (this.mDeathListenerLock) {
                this.mDeathListener = null;
            }
            Log.i(TAG, "unlink servicehost death.");
        }
    }

    public CameraCharacteristics getCharacteristics(String cameraId, CameraCharacteristics character) {
        Log.i(TAG, "get servicehost characteristics, camera id: " + cameraId);
        if (character == null) {
            Log.i(TAG, "input characteristics is null!");
            return null;
        } else if (!ServiceFetcher.checkConnected()) {
            Log.i(TAG, "do not connect to service host.");
            return character;
        } else {
            CameraMetadataNative nativeMetadata = character.getNativeCopy();
            ImageProcessManager.get().queryCapability(cameraId, nativeMetadata);
            return new CameraCharacteristics(nativeMetadata);
        }
    }

    public int getSupportedMode() {
        Log.i(TAG, "get servicehost supported mode.");
        if (!ServiceFetcher.checkConnected()) {
            Log.i(TAG, "can not connect to service host.");
            return 0;
        }
        int supportedMode = ImageProcessManager.get().getSupportedMode();
        Log.i(TAG, "supported mode: " + Integer.toHexString(supportedMode));
        return supportedMode;
    }

    public int dualCameraMode() {
        Log.i(TAG, "dual camera mode.");
        if (!ServiceFetcher.checkConnected()) {
            Log.i(TAG, "can not connect to service host.");
            return 0;
        }
        int dualCameraMode = ImageProcessManager.get().dualCameraMode();
        Log.i(TAG, "dual camera mode: " + Integer.toHexString(dualCameraMode));
        return dualCameraMode;
    }

    private class DeathRecipientListener implements IBinder.DeathRecipient {
        private DeathRecipientListener() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.e(ServiceHostGlobalSession.TAG, "servicehost died!");
            synchronized (ServiceHostGlobalSession.this.mDeathListenerLock) {
                if (ServiceHostGlobalSession.this.mDeathListener == null) {
                    Log.e(ServiceHostGlobalSession.TAG, "listener is null, cann't report to app!");
                } else {
                    ServiceHostGlobalSession.this.mDeathListener.onServiceHostDied();
                }
            }
        }
    }
}
