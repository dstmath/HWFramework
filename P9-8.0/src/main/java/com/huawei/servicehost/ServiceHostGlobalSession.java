package com.huawei.servicehost;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.servicehost.IGlobalListener.Stub;

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
    private DeathListener mDeathListener;
    private final Object mDeathListenerLock;
    private GlobalListener mGlobalListener;
    private final Object mGlobalListenerLock;
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

    private class IDeathListener implements DeathRecipient {
        /* synthetic */ IDeathListener(ServiceHostGlobalSession this$0, IDeathListener -this1) {
            this();
        }

        private IDeathListener() {
        }

        public void binderDied() {
            Log.e(ServiceHostGlobalSession.TAG, "servicehost died!");
            synchronized (ServiceHostGlobalSession.this.mDeathListenerLock) {
                if (ServiceHostGlobalSession.this.mDeathListener == null) {
                    Log.e(ServiceHostGlobalSession.TAG, "listener is null, cann't report to app!");
                    return;
                }
                ServiceHostGlobalSession.this.mDeathListener.onServiceHostDied();
            }
        }
    }

    private static class SingleGlobalSessionHolder {
        private static final ServiceHostGlobalSession mInstance = new ServiceHostGlobalSession();

        private SingleGlobalSessionHolder() {
        }
    }

    /* synthetic */ ServiceHostGlobalSession(ServiceHostGlobalSession -this0) {
        this();
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
        this.mIGlobalListener = new Stub() {
            public void onGlobalEvent(IIPEvent iipEvent) throws RemoteException {
                if (iipEvent != null) {
                    GlobalListener globalListener;
                    synchronized (ServiceHostGlobalSession.this.mGlobalListenerLock) {
                        globalListener = ServiceHostGlobalSession.this.mGlobalListener;
                    }
                    if (globalListener != null) {
                        IBinder obj = iipEvent.getObject();
                        String type = iipEvent.getType();
                        if (type.equals(ServiceHostGlobalSession.GLOBAL_EVENT_CAPTURE_RESULT)) {
                            IIPEvent4GlobalResult result = IIPEvent4GlobalResult.Stub.asInterface(obj);
                            if (result == null) {
                                Log.e(ServiceHostGlobalSession.TAG, "get result from global event return null!");
                                return;
                            }
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
                            Log.i(ServiceHostGlobalSession.TAG, ", jpeg size: " + pictureSize);
                            Bundle picInfo = new Bundle();
                            picInfo.putString(ServiceHostGlobalSession.CAPTURE_RESULT_FILE_PATH, path);
                            picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_STATUS, imageSaveState);
                            picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_MODE, captureMode);
                            picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_WIDTH, captureWidth);
                            picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_HEIGHT, captureHeight);
                            picInfo.putInt(ServiceHostGlobalSession.CAPTURE_RESULT_SIZE, pictureSize);
                            picInfo.putString(ServiceHostGlobalSession.CAPTURE_RESULT_EXIF, "");
                            globalListener.onPictureSaved(picInfo);
                        } else if (type.equals(ServiceHostGlobalSession.GLOBAL_EVENT_AVALIABLE_CAPTURE_NUM)) {
                            int availableNum = IIPEvent4CapNumber.Stub.asInterface(obj).getLowCapNumber();
                            if (availableNum <= 0) {
                                Log.d(ServiceHostGlobalSession.TAG, "update available number: " + availableNum);
                            }
                            globalListener.onSnapshotNumUpdate(availableNum);
                        } else {
                            Log.e(ServiceHostGlobalSession.TAG, "unknown type: " + type);
                        }
                    }
                }
            }
        };
        this.mIDeathListener = new IDeathListener(this, null);
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
        return;
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
            return;
        }
    }

    public CameraCharacteristics getCharacteristics(String cameraId, CameraCharacteristics character) {
        Log.i(TAG, "get servicehost characteristics, camera id: " + cameraId);
        if (character == null) {
            Log.i(TAG, "input characteristics is null!");
            return null;
        } else if (ServiceFetcher.checkConnected()) {
            CameraMetadataNative nativeMetadata = character.getNativeCopy();
            ImageProcessManager.get().queryCapability(cameraId, nativeMetadata);
            return new CameraCharacteristics(nativeMetadata);
        } else {
            Log.i(TAG, "do not connect to service host.");
            return character;
        }
    }

    public int getSupportedMode() {
        Log.i(TAG, "get servicehost supported mode.");
        if (ServiceFetcher.checkConnected()) {
            int supportedMode = ImageProcessManager.get().getSupportedMode();
            Log.i(TAG, "supported mode: " + Integer.toHexString(supportedMode));
            return supportedMode;
        }
        Log.i(TAG, "can not connect to service host.");
        return 0;
    }

    public int dualCameraMode() {
        Log.i(TAG, "dual camera mode.");
        if (ServiceFetcher.checkConnected()) {
            int dualCameraMode = ImageProcessManager.get().dualCameraMode();
            Log.i(TAG, "dual camera mode: " + Integer.toHexString(dualCameraMode));
            return dualCameraMode;
        }
        Log.i(TAG, "can not connect to service host.");
        return 0;
    }
}
