package android.hardware.camera2.impl;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.dispatch.Dispatchable;
import android.hardware.camera2.dispatch.MethodNameInvoker;
import android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback;
import android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK;
import android.view.Surface;
import com.android.internal.util.Preconditions;

public class CallbackProxies {

    public static class DeviceCaptureCallbackProxy extends CaptureCallback {
        private final MethodNameInvoker<CaptureCallback> mProxy;

        public DeviceCaptureCallbackProxy(Dispatchable<CaptureCallback> dispatchTarget) {
            this.mProxy = new MethodNameInvoker((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), CaptureCallback.class);
        }

        public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
            this.mProxy.invoke("onCaptureStarted", camera, request, Long.valueOf(timestamp), Long.valueOf(frameNumber));
        }

        public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
            this.mProxy.invoke("onCapturePartial", camera, request, result);
        }

        public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
            this.mProxy.invoke("onCaptureProgressed", camera, request, partialResult);
        }

        public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
            this.mProxy.invoke("onCaptureCompleted", camera, request, result);
        }

        public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
            this.mProxy.invoke("onCaptureFailed", camera, request, failure);
        }

        public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
            this.mProxy.invoke("onCaptureSequenceCompleted", camera, Integer.valueOf(sequenceId), Long.valueOf(frameNumber));
        }

        public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
            this.mProxy.invoke("onCaptureSequenceAborted", camera, Integer.valueOf(sequenceId));
        }
    }

    public static class DeviceStateCallbackProxy extends StateCallbackKK {
        private final MethodNameInvoker<StateCallbackKK> mProxy;

        public DeviceStateCallbackProxy(Dispatchable<StateCallbackKK> dispatchTarget) {
            this.mProxy = new MethodNameInvoker((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), StateCallbackKK.class);
        }

        public void onOpened(CameraDevice camera) {
            this.mProxy.invoke("onOpened", camera);
        }

        public void onDisconnected(CameraDevice camera) {
            this.mProxy.invoke("onDisconnected", camera);
        }

        public void onError(CameraDevice camera, int error) {
            this.mProxy.invoke("onError", camera, Integer.valueOf(error));
        }

        public void onUnconfigured(CameraDevice camera) {
            this.mProxy.invoke("onUnconfigured", camera);
        }

        public void onActive(CameraDevice camera) {
            this.mProxy.invoke("onActive", camera);
        }

        public void onBusy(CameraDevice camera) {
            this.mProxy.invoke("onBusy", camera);
        }

        public void onClosed(CameraDevice camera) {
            this.mProxy.invoke("onClosed", camera);
        }

        public void onIdle(CameraDevice camera) {
            this.mProxy.invoke("onIdle", camera);
        }
    }

    public static class SessionStateCallbackProxy extends StateCallback {
        private final MethodNameInvoker<StateCallback> mProxy;

        public SessionStateCallbackProxy(Dispatchable<StateCallback> dispatchTarget) {
            this.mProxy = new MethodNameInvoker((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), StateCallback.class);
        }

        public void onConfigured(CameraCaptureSession session) {
            this.mProxy.invoke("onConfigured", session);
        }

        public void onConfigureFailed(CameraCaptureSession session) {
            this.mProxy.invoke("onConfigureFailed", session);
        }

        public void onReady(CameraCaptureSession session) {
            this.mProxy.invoke("onReady", session);
        }

        public void onActive(CameraCaptureSession session) {
            this.mProxy.invoke("onActive", session);
        }

        public void onClosed(CameraCaptureSession session) {
            this.mProxy.invoke("onClosed", session);
        }

        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            this.mProxy.invoke("onSurfacePrepared", session, surface);
        }
    }

    private CallbackProxies() {
        throw new AssertionError();
    }
}
