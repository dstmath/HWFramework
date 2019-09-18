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

public class ServiceHostGlobalSession {
    public static final String CAPTURE_RESULT_EXIF = "captureResultExif";
    public static final String CAPTURE_RESULT_FILE_PATH = "captureResultFilePath";
    public static final String CAPTURE_RESULT_HEIGHT = "captureResultHeight";
    public static final String CAPTURE_RESULT_MODE = "captureResultMode";
    public static final String CAPTURE_RESULT_SIZE = "captureResultSize";
    public static final String CAPTURE_RESULT_STATUS = "captureResultStatus";
    public static final String CAPTURE_RESULT_WIDTH = "captureResultWidth";
    private static final boolean DEBUG = false;
    private static final String GLOBAL_EVENT_AVALIABLE_CAPTURE_NUM = "AvaliableCapNum";
    private static final String GLOBAL_EVENT_CAPTURE_RESULT = "result";
    private static final String TAG = "ServiceHostGlobalSession";
    /* access modifiers changed from: private */
    public DeathListener mDeathListener;
    /* access modifiers changed from: private */
    public final Object mDeathListenerLock;
    /* access modifiers changed from: private */
    public GlobalListener mGlobalListener;
    /* access modifiers changed from: private */
    public final Object mGlobalListenerLock;
    private final Object mGlobalSessionLock;
    private IDeathListener mIDeathListener;
    private IGlobalListener mIGlobalListener;
    private IGlobalSession mIGlobalSession;

    public interface DeathListener {
        void onServiceHostDied();
    }

    public interface GlobalListener {
        void onPictureSaved(Bundle bundle);

        void onSnapshotNumUpdate(int i);
    }

    private class IDeathListener implements IBinder.DeathRecipient {
        private IDeathListener() {
        }

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

    private static class SingleGlobalSessionHolder {
        /* access modifiers changed from: private */
        public static final ServiceHostGlobalSession mInstance = new ServiceHostGlobalSession();

        private SingleGlobalSessionHolder() {
        }
    }

    public static final ServiceHostGlobalSession getInstance() {
        return SingleGlobalSessionHolder.mInstance;
    }

    private ServiceHostGlobalSession() {
        this.mIGlobalSession = null;
        this.mGlobalListener = null;
        this.mDeathListener = null;
        this.mGlobalSessionLock = new Object();
        this.mGlobalListenerLock = new Object();
        this.mDeathListenerLock = new Object();
        this.mIGlobalListener = new IGlobalListener.Stub() {
            public void onGlobalEvent(IIPEvent iipEvent) throws RemoteException {
                GlobalListener globalListener;
                if (iipEvent != null) {
                    synchronized (ServiceHostGlobalSession.this.mGlobalListenerLock) {
                        globalListener = ServiceHostGlobalSession.this.mGlobalListener;
                    }
                    if (globalListener != null) {
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
                        switch (c) {
                            case 0:
                                IIPEvent4GlobalResult result = IIPEvent4GlobalResult.Stub.asInterface(obj);
                                if (result != null) {
                                    String path = result.getFilePath();
                                    Log.i(ServiceHostGlobalSession.TAG, "jpeg saved, path: " + path);
                                    int imageSaveState = result.getResult();
                                    Log.i(ServiceHostGlobalSession.TAG, "servicehost save status: " + imageSaveState);
                                    int captureMode = result.getCaptureMode();
                                    Log.i(ServiceHostGlobalSession.TAG, "capture mode: " + captureMode);
                                    int captureWidth = result.getCaptureWidth();
                                    int captureHeight = result.getCaptureHeight();
                                    Log.i(ServiceHostGlobalSession.TAG, "capture size: " + captureWidth + " x " + captureHeight);
                                    int pictureSize = result.getPictureSize();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(", jpeg size: ");
                                    sb.append(pictureSize);
                                    Log.i(ServiceHostGlobalSession.TAG, sb.toString());
                                    Bundle picInfo = new Bundle();
                                    picInfo.putString(ServiceHostGlobalSession.CAPTURE_RESULT_FILE_PATH, path);
                                    picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_STATUS, imageSaveState);
                                    picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_MODE, captureMode);
                                    picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_WIDTH, captureWidth);
                                    picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_HEIGHT, captureHeight);
                                    picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_SIZE, pictureSize);
                                    picInfo.putString(ServiceHostGlobalSession.CAPTURE_RESULT_EXIF, "");
                                    globalListener.onPictureSaved(picInfo);
                                    break;
                                } else {
                                    Log.e(ServiceHostGlobalSession.TAG, "get result from global event return null!");
                                    return;
                                }
                            case 1:
                                int availableNum = IIPEvent4CapNumber.Stub.asInterface(obj).getLowCapNumber();
                                if (availableNum <= 0) {
                                    Log.d(ServiceHostGlobalSession.TAG, "update available number: " + availableNum);
                                }
                                globalListener.onSnapshotNumUpdate(availableNum);
                                break;
                            default:
                                Log.e(ServiceHostGlobalSession.TAG, "unknown type: " + type);
                                break;
                        }
                    }
                }
            }
        };
        this.mIDeathListener = new IDeathListener();
        Log.i(TAG, "construct global session");
    }

    public void initialize(GlobalListener globalListener, DeathListener deathListener) {
        Log.i(TAG, "initialize global session.");
        synchronized (this.mGlobalSessionLock) {
            this.mIGlobalSession = ImageProcessManager.get().getGlobalSession();
            if (this.mIGlobalSession == null) {
                Log.d(TAG, "get global session return null!");
                return;
            }
            try {
                this.mIGlobalSession.addListener(this.mIGlobalListener);
            } catch (RemoteException e) {
                Log.e(TAG, "add global session listener exception: " + e.getMessage());
            }
            try {
                this.mIGlobalSession.asBinder().linkToDeath(this.mIDeathListener, 0);
            } catch (Exception e2) {
                Log.e(TAG, "register binder die notification exception:" + e2.getMessage());
            }
        }
        synchronized (this.mGlobalListenerLock) {
            this.mGlobalListener = globalListener;
        }
        synchronized (this.mDeathListenerLock) {
            this.mDeathListener = deathListener;
        }
        Log.i(TAG, "link servicehost death.");
    }

    public void release() {
        Log.i(TAG, "release global session.");
        synchronized (this.mGlobalSessionLock) {
            if (this.mIGlobalSession == null) {
                Log.d(TAG, "global session is null!");
                return;
            }
            try {
                this.mIGlobalSession.removeListener(this.mIGlobalListener);
            } catch (RemoteException e) {
                Log.e(TAG, "remove global session listener exception: " + e.getMessage());
            }
            if (this.mIDeathListener == null) {
                Log.d(TAG, "servicehost death listener is null!");
                return;
            }
            try {
                this.mIGlobalSession.asBinder().unlinkToDeath(this.mIDeathListener, 0);
            } catch (Exception e2) {
                Log.e(TAG, "unlinkToDeath exception: " + e2.getMessage());
            }
            this.mIGlobalSession = null;
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
}
