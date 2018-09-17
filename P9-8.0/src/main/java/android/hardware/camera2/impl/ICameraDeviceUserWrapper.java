package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.view.Surface;

public class ICameraDeviceUserWrapper {
    private final ICameraDeviceUser mRemoteDevice;

    public ICameraDeviceUserWrapper(ICameraDeviceUser remoteDevice) {
        if (remoteDevice == null) {
            throw new NullPointerException("Remote device may not be null");
        }
        this.mRemoteDevice = remoteDevice;
    }

    public void unlinkToDeath(DeathRecipient recipient, int flags) {
        if (this.mRemoteDevice.asBinder() != null) {
            this.mRemoteDevice.asBinder().unlinkToDeath(recipient, flags);
        }
    }

    public void disconnect() {
        try {
            this.mRemoteDevice.disconnect();
        } catch (RemoteException e) {
        }
    }

    public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) throws CameraAccessException {
        try {
            return this.mRemoteDevice.submitRequest(request, streaming);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean streaming) throws CameraAccessException {
        try {
            return this.mRemoteDevice.submitRequestList(requestList, streaming);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public long cancelRequest(int requestId) throws CameraAccessException {
        try {
            return this.mRemoteDevice.cancelRequest(requestId);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void beginConfigure() throws CameraAccessException {
        try {
            this.mRemoteDevice.beginConfigure();
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void endConfigure(int operatingMode) throws CameraAccessException {
        try {
            this.mRemoteDevice.endConfigure(operatingMode);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void deleteStream(int streamId) throws CameraAccessException {
        try {
            this.mRemoteDevice.deleteStream(streamId);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public int createStream(OutputConfiguration outputConfiguration) throws CameraAccessException {
        try {
            return this.mRemoteDevice.createStream(outputConfiguration);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public int createInputStream(int width, int height, int format) throws CameraAccessException {
        try {
            return this.mRemoteDevice.createInputStream(width, height, format);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public Surface getInputSurface() throws CameraAccessException {
        try {
            return this.mRemoteDevice.getInputSurface();
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public CameraMetadataNative createDefaultRequest(int templateId) throws CameraAccessException {
        try {
            return this.mRemoteDevice.createDefaultRequest(templateId);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public CameraMetadataNative getCameraInfo() throws CameraAccessException {
        try {
            return this.mRemoteDevice.getCameraInfo();
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void waitUntilIdle() throws CameraAccessException {
        try {
            this.mRemoteDevice.waitUntilIdle();
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public long flush() throws CameraAccessException {
        try {
            return this.mRemoteDevice.flush();
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void prepare(int streamId) throws CameraAccessException {
        try {
            this.mRemoteDevice.prepare(streamId);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void tearDown(int streamId) throws CameraAccessException {
        try {
            this.mRemoteDevice.tearDown(streamId);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void prepare2(int maxCount, int streamId) throws CameraAccessException {
        try {
            this.mRemoteDevice.prepare2(maxCount, streamId);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }

    public void finalizeOutputConfigurations(int streamId, OutputConfiguration deferredConfig) throws CameraAccessException {
        try {
            this.mRemoteDevice.finalizeOutputConfigurations(streamId, deferredConfig);
        } catch (Throwable t) {
            CameraManager.throwAsPublicException(t);
            UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Unexpected exception", t);
        }
    }
}
