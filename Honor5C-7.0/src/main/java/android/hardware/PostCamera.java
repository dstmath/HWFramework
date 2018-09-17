package android.hardware;

import android.app.ActivityThread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class PostCamera {
    public static final String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";
    private static final int CAMERA_MSG_COMPRESSED_IMAGE = 256;
    private static final int CAMERA_MSG_ERROR = 1;
    private static final String TAG = "PostCamera";
    private PostErrorCallback mErrorCallback;
    private EventHandler mEventHandler;
    private PostPictureCallback mJpegCallback;
    private Object mJpegCallbackLock;
    private long mNativeContext;
    private boolean mOneShot;
    private boolean mWithBuffer;

    private class EventHandler extends Handler {
        private PostCamera mCamera;

        public EventHandler(PostCamera c, Looper looper) {
            super(looper);
            this.mCamera = c;
        }

        public void handleMessage(Message msg) {
            Log.i(PostCamera.TAG, "post call back, msg=" + msg.what);
            switch (msg.what) {
                case PostCamera.CAMERA_MSG_ERROR /*1*/:
                    Log.e(PostCamera.TAG, "Error " + msg.arg1);
                    if (PostCamera.this.mErrorCallback != null) {
                        PostCamera.this.mErrorCallback.onError(msg.arg1, this.mCamera);
                    }
                case PostCamera.CAMERA_MSG_COMPRESSED_IMAGE /*256*/:
                    if (PostCamera.this.mJpegCallback != null) {
                        PostCamera.this.mJpegCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                default:
                    Log.e(PostCamera.TAG, "Unknown message type " + msg.what);
            }
        }
    }

    public interface PostErrorCallback {
        void onError(int i, PostCamera postCamera);
    }

    public interface PostPictureCallback {
        void onPictureTaken(byte[] bArr, PostCamera postCamera);
    }

    private final native void native_release();

    private final native void native_setup(Object obj, String str);

    public final native void lock();

    public final native void reconnect() throws IOException;

    public final native void unlock();

    public static PostCamera open(PostPictureCallback jpeg, PostErrorCallback error) {
        return new PostCamera(jpeg, error);
    }

    PostCamera(PostPictureCallback jpeg, PostErrorCallback error) {
        this.mJpegCallbackLock = new Object();
        this.mJpegCallback = jpeg;
        this.mErrorCallback = error;
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        native_setup(new WeakReference(this), ActivityThread.currentPackageName());
    }

    public final void release() {
        native_release();
    }

    private static void postEventFromNative(Object camera_ref, int what, int arg1, int arg2, Object obj) {
        PostCamera c = (PostCamera) ((WeakReference) camera_ref).get();
        if (!(c == null || c.mEventHandler == null)) {
            c.mEventHandler.sendMessage(c.mEventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }
}
