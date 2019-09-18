package android.media.tv;

import android.annotation.SystemApi;
import android.content.Context;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import java.util.ArrayDeque;
import java.util.Queue;

public class TvRecordingClient {
    private static final boolean DEBUG = false;
    private static final String TAG = "TvRecordingClient";
    /* access modifiers changed from: private */
    public final RecordingCallback mCallback;
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsRecordingStarted;
    /* access modifiers changed from: private */
    public boolean mIsTuned;
    /* access modifiers changed from: private */
    public final Queue<Pair<String, Bundle>> mPendingAppPrivateCommands = new ArrayDeque();
    /* access modifiers changed from: private */
    public TvInputManager.Session mSession;
    /* access modifiers changed from: private */
    public MySessionCallback mSessionCallback;
    private final TvInputManager mTvInputManager;

    private class MySessionCallback extends TvInputManager.SessionCallback {
        Uri mChannelUri;
        Bundle mConnectionParams;
        final String mInputId;

        MySessionCallback(String inputId, Uri channelUri, Bundle connectionParams) {
            this.mInputId = inputId;
            this.mChannelUri = channelUri;
            this.mConnectionParams = connectionParams;
        }

        public void onSessionCreated(TvInputManager.Session session) {
            if (this != TvRecordingClient.this.mSessionCallback) {
                Log.w(TvRecordingClient.TAG, "onSessionCreated - session already created");
                if (session != null) {
                    session.release();
                }
                return;
            }
            TvInputManager.Session unused = TvRecordingClient.this.mSession = session;
            if (session != null) {
                for (Pair<String, Bundle> command : TvRecordingClient.this.mPendingAppPrivateCommands) {
                    TvRecordingClient.this.mSession.sendAppPrivateCommand((String) command.first, (Bundle) command.second);
                }
                TvRecordingClient.this.mPendingAppPrivateCommands.clear();
                TvRecordingClient.this.mSession.tune(this.mChannelUri, this.mConnectionParams);
            } else {
                MySessionCallback unused2 = TvRecordingClient.this.mSessionCallback = null;
                if (TvRecordingClient.this.mCallback != null) {
                    TvRecordingClient.this.mCallback.onConnectionFailed(this.mInputId);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onTuned(TvInputManager.Session session, Uri channelUri) {
            if (this != TvRecordingClient.this.mSessionCallback) {
                Log.w(TvRecordingClient.TAG, "onTuned - session not created");
                return;
            }
            boolean unused = TvRecordingClient.this.mIsTuned = true;
            TvRecordingClient.this.mCallback.onTuned(channelUri);
        }

        public void onSessionReleased(TvInputManager.Session session) {
            if (this != TvRecordingClient.this.mSessionCallback) {
                Log.w(TvRecordingClient.TAG, "onSessionReleased - session not created");
                return;
            }
            boolean unused = TvRecordingClient.this.mIsTuned = false;
            boolean unused2 = TvRecordingClient.this.mIsRecordingStarted = false;
            MySessionCallback unused3 = TvRecordingClient.this.mSessionCallback = null;
            TvInputManager.Session unused4 = TvRecordingClient.this.mSession = null;
            if (TvRecordingClient.this.mCallback != null) {
                TvRecordingClient.this.mCallback.onDisconnected(this.mInputId);
            }
        }

        public void onRecordingStopped(TvInputManager.Session session, Uri recordedProgramUri) {
            if (this != TvRecordingClient.this.mSessionCallback) {
                Log.w(TvRecordingClient.TAG, "onRecordingStopped - session not created");
                return;
            }
            boolean unused = TvRecordingClient.this.mIsRecordingStarted = false;
            TvRecordingClient.this.mCallback.onRecordingStopped(recordedProgramUri);
        }

        public void onError(TvInputManager.Session session, int error) {
            if (this != TvRecordingClient.this.mSessionCallback) {
                Log.w(TvRecordingClient.TAG, "onError - session not created");
            } else {
                TvRecordingClient.this.mCallback.onError(error);
            }
        }

        public void onSessionEvent(TvInputManager.Session session, String eventType, Bundle eventArgs) {
            if (this != TvRecordingClient.this.mSessionCallback) {
                Log.w(TvRecordingClient.TAG, "onSessionEvent - session not created");
                return;
            }
            if (TvRecordingClient.this.mCallback != null) {
                TvRecordingClient.this.mCallback.onEvent(this.mInputId, eventType, eventArgs);
            }
        }
    }

    public static abstract class RecordingCallback {
        public void onConnectionFailed(String inputId) {
        }

        public void onDisconnected(String inputId) {
        }

        public void onTuned(Uri channelUri) {
        }

        public void onRecordingStopped(Uri recordedProgramUri) {
        }

        public void onError(int error) {
        }

        @SystemApi
        public void onEvent(String inputId, String eventType, Bundle eventArgs) {
        }
    }

    public TvRecordingClient(Context context, String tag, RecordingCallback callback, Handler handler) {
        this.mCallback = callback;
        this.mHandler = handler == null ? new Handler(Looper.getMainLooper()) : handler;
        this.mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
    }

    public void tune(String inputId, Uri channelUri) {
        tune(inputId, channelUri, null);
    }

    public void tune(String inputId, Uri channelUri, Bundle params) {
        if (TextUtils.isEmpty(inputId)) {
            throw new IllegalArgumentException("inputId cannot be null or an empty string");
        } else if (this.mIsRecordingStarted) {
            throw new IllegalStateException("tune failed - recording already started");
        } else if (this.mSessionCallback == null || !TextUtils.equals(this.mSessionCallback.mInputId, inputId)) {
            resetInternal();
            this.mSessionCallback = new MySessionCallback(inputId, channelUri, params);
            if (this.mTvInputManager != null) {
                this.mTvInputManager.createRecordingSession(inputId, this.mSessionCallback, this.mHandler);
            }
        } else if (this.mSession != null) {
            this.mSession.tune(channelUri, params);
        } else {
            this.mSessionCallback.mChannelUri = channelUri;
            this.mSessionCallback.mConnectionParams = params;
        }
    }

    public void release() {
        resetInternal();
    }

    private void resetInternal() {
        this.mSessionCallback = null;
        this.mPendingAppPrivateCommands.clear();
        if (this.mSession != null) {
            this.mSession.release();
            this.mSession = null;
        }
    }

    public void startRecording(Uri programUri) {
        if (!this.mIsTuned) {
            throw new IllegalStateException("startRecording failed - not yet tuned");
        } else if (this.mSession != null) {
            this.mSession.startRecording(programUri);
            this.mIsRecordingStarted = true;
        }
    }

    public void stopRecording() {
        if (!this.mIsRecordingStarted) {
            Log.w(TAG, "stopRecording failed - recording not yet started");
        }
        if (this.mSession != null) {
            this.mSession.stopRecording();
        }
    }

    public void sendAppPrivateCommand(String action, Bundle data) {
        if (TextUtils.isEmpty(action)) {
            throw new IllegalArgumentException("action cannot be null or an empty string");
        } else if (this.mSession != null) {
            this.mSession.sendAppPrivateCommand(action, data);
        } else {
            Log.w(TAG, "sendAppPrivateCommand - session not yet created (action \"" + action + "\" pending)");
            this.mPendingAppPrivateCommands.add(Pair.create(action, data));
        }
    }
}
