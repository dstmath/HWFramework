package android.media.tv;

import android.content.Context;
import android.graphics.Rect;
import android.media.PlaybackParams;
import android.media.tv.ITvInputSession.Stub;
import android.media.tv.TvInputService.RecordingSession;
import android.media.tv.TvInputService.Session;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.Surface;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.internal.os.SomeArgs;

public class ITvInputSessionWrapper extends Stub implements Callback {
    private static final int DO_APP_PRIVATE_COMMAND = 9;
    private static final int DO_CREATE_OVERLAY_VIEW = 10;
    private static final int DO_DISPATCH_SURFACE_CHANGED = 4;
    private static final int DO_RELAYOUT_OVERLAY_VIEW = 11;
    private static final int DO_RELEASE = 1;
    private static final int DO_REMOVE_OVERLAY_VIEW = 12;
    private static final int DO_SELECT_TRACK = 8;
    private static final int DO_SET_CAPTION_ENABLED = 7;
    private static final int DO_SET_MAIN = 2;
    private static final int DO_SET_STREAM_VOLUME = 5;
    private static final int DO_SET_SURFACE = 3;
    private static final int DO_START_RECORDING = 20;
    private static final int DO_STOP_RECORDING = 21;
    private static final int DO_TIME_SHIFT_ENABLE_POSITION_TRACKING = 19;
    private static final int DO_TIME_SHIFT_PAUSE = 15;
    private static final int DO_TIME_SHIFT_PLAY = 14;
    private static final int DO_TIME_SHIFT_RESUME = 16;
    private static final int DO_TIME_SHIFT_SEEK_TO = 17;
    private static final int DO_TIME_SHIFT_SET_PLAYBACK_PARAMS = 18;
    private static final int DO_TUNE = 6;
    private static final int DO_UNBLOCK_CONTENT = 13;
    private static final int EXECUTE_MESSAGE_TIMEOUT_LONG_MILLIS = 5000;
    private static final int EXECUTE_MESSAGE_TIMEOUT_SHORT_MILLIS = 50;
    private static final int EXECUTE_MESSAGE_TUNE_TIMEOUT_MILLIS = 2000;
    private static final String TAG = "TvInputSessionWrapper";
    private final HandlerCaller mCaller;
    private InputChannel mChannel;
    private final boolean mIsRecordingSession = true;
    private TvInputEventReceiver mReceiver;
    private RecordingSession mTvInputRecordingSessionImpl;
    private Session mTvInputSessionImpl;

    private final class TvInputEventReceiver extends InputEventReceiver {
        public TvInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event) {
            boolean z = true;
            if (ITvInputSessionWrapper.this.mTvInputSessionImpl == null) {
                finishInputEvent(event, false);
                return;
            }
            int handled = ITvInputSessionWrapper.this.mTvInputSessionImpl.dispatchInputEvent(event, this);
            if (handled != -1) {
                if (handled != 1) {
                    z = false;
                }
                finishInputEvent(event, z);
            }
        }
    }

    public ITvInputSessionWrapper(Context context, Session sessionImpl, InputChannel channel) {
        this.mCaller = new HandlerCaller(context, null, this, true);
        this.mTvInputSessionImpl = sessionImpl;
        this.mChannel = channel;
        if (channel != null) {
            this.mReceiver = new TvInputEventReceiver(channel, context.getMainLooper());
        }
    }

    public ITvInputSessionWrapper(Context context, RecordingSession recordingSessionImpl) {
        this.mCaller = new HandlerCaller(context, null, this, true);
        this.mTvInputRecordingSessionImpl = recordingSessionImpl;
    }

    public void executeMessage(Message msg) {
        if (!(this.mIsRecordingSession && this.mTvInputRecordingSessionImpl == null) && (this.mIsRecordingSession || this.mTvInputSessionImpl != null)) {
            long startTime = System.nanoTime();
            SomeArgs args;
            switch (msg.what) {
                case 1:
                    if (!this.mIsRecordingSession) {
                        this.mTvInputSessionImpl.release();
                        this.mTvInputSessionImpl = null;
                        if (this.mReceiver != null) {
                            this.mReceiver.dispose();
                            this.mReceiver = null;
                        }
                        if (this.mChannel != null) {
                            this.mChannel.dispose();
                            this.mChannel = null;
                            break;
                        }
                    }
                    this.mTvInputRecordingSessionImpl.release();
                    this.mTvInputRecordingSessionImpl = null;
                    break;
                    break;
                case 2:
                    this.mTvInputSessionImpl.setMain(((Boolean) msg.obj).booleanValue());
                    break;
                case 3:
                    this.mTvInputSessionImpl.setSurface((Surface) msg.obj);
                    break;
                case 4:
                    args = msg.obj;
                    this.mTvInputSessionImpl.dispatchSurfaceChanged(args.argi1, args.argi2, args.argi3);
                    args.recycle();
                    break;
                case 5:
                    this.mTvInputSessionImpl.setStreamVolume(((Float) msg.obj).floatValue());
                    break;
                case 6:
                    args = (SomeArgs) msg.obj;
                    if (this.mIsRecordingSession) {
                        this.mTvInputRecordingSessionImpl.tune((Uri) args.arg1, (Bundle) args.arg2);
                    } else {
                        this.mTvInputSessionImpl.tune((Uri) args.arg1, (Bundle) args.arg2);
                    }
                    args.recycle();
                    break;
                case 7:
                    this.mTvInputSessionImpl.setCaptionEnabled(((Boolean) msg.obj).booleanValue());
                    break;
                case 8:
                    args = (SomeArgs) msg.obj;
                    this.mTvInputSessionImpl.selectTrack(((Integer) args.arg1).intValue(), (String) args.arg2);
                    args.recycle();
                    break;
                case 9:
                    args = (SomeArgs) msg.obj;
                    if (this.mIsRecordingSession) {
                        this.mTvInputRecordingSessionImpl.appPrivateCommand((String) args.arg1, (Bundle) args.arg2);
                    } else {
                        this.mTvInputSessionImpl.appPrivateCommand((String) args.arg1, (Bundle) args.arg2);
                    }
                    args.recycle();
                    break;
                case 10:
                    args = (SomeArgs) msg.obj;
                    this.mTvInputSessionImpl.createOverlayView((IBinder) args.arg1, (Rect) args.arg2);
                    args.recycle();
                    break;
                case 11:
                    this.mTvInputSessionImpl.relayoutOverlayView((Rect) msg.obj);
                    break;
                case 12:
                    this.mTvInputSessionImpl.removeOverlayView(true);
                    break;
                case 13:
                    this.mTvInputSessionImpl.unblockContent((String) msg.obj);
                    break;
                case 14:
                    this.mTvInputSessionImpl.timeShiftPlay((Uri) msg.obj);
                    break;
                case 15:
                    this.mTvInputSessionImpl.timeShiftPause();
                    break;
                case 16:
                    this.mTvInputSessionImpl.timeShiftResume();
                    break;
                case 17:
                    this.mTvInputSessionImpl.timeShiftSeekTo(((Long) msg.obj).longValue());
                    break;
                case 18:
                    this.mTvInputSessionImpl.timeShiftSetPlaybackParams((PlaybackParams) msg.obj);
                    break;
                case 19:
                    this.mTvInputSessionImpl.timeShiftEnablePositionTracking(((Boolean) msg.obj).booleanValue());
                    break;
                case 20:
                    this.mTvInputRecordingSessionImpl.startRecording((Uri) msg.obj);
                    break;
                case 21:
                    this.mTvInputRecordingSessionImpl.stopRecording();
                    break;
                default:
                    Log.w(TAG, "Unhandled message code: " + msg.what);
                    break;
            }
            long durationMs = (System.nanoTime() - startTime) / 1000000;
            if (durationMs > 50) {
                Log.w(TAG, "Handling message (" + msg.what + ") took too long time (duration=" + durationMs + "ms)");
                if (msg.what == 6 && durationMs > 2000) {
                    throw new RuntimeException("Too much time to handle tune request. (" + durationMs + "ms > " + 2000 + "ms) " + "Consider handling the tune request in a separate thread.");
                } else if (durationMs > 5000) {
                    throw new RuntimeException("Too much time to handle a request. (type=" + msg.what + ", " + durationMs + "ms > " + 5000 + "ms).");
                }
            }
        }
    }

    public void release() {
        if (!this.mIsRecordingSession) {
            this.mTvInputSessionImpl.scheduleOverlayViewCleanup();
        }
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(1));
    }

    public void setMain(boolean isMain) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(2, Boolean.valueOf(isMain)));
    }

    public void setSurface(Surface surface) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(3, surface));
    }

    public void dispatchSurfaceChanged(int format, int width, int height) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageIIII(4, format, width, height, 0));
    }

    public final void setVolume(float volume) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(5, Float.valueOf(volume)));
    }

    public void tune(Uri channelUri, Bundle params) {
        this.mCaller.removeMessages(6);
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(6, channelUri, params));
    }

    public void setCaptionEnabled(boolean enabled) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(7, Boolean.valueOf(enabled)));
    }

    public void selectTrack(int type, String trackId) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(8, Integer.valueOf(type), trackId));
    }

    public void appPrivateCommand(String action, Bundle data) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(9, action, data));
    }

    public void createOverlayView(IBinder windowToken, Rect frame) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageOO(10, windowToken, frame));
    }

    public void relayoutOverlayView(Rect frame) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(11, frame));
    }

    public void removeOverlayView() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(12));
    }

    public void unblockContent(String unblockedRating) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(13, unblockedRating));
    }

    public void timeShiftPlay(Uri recordedProgramUri) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(14, recordedProgramUri));
    }

    public void timeShiftPause() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(15));
    }

    public void timeShiftResume() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(16));
    }

    public void timeShiftSeekTo(long timeMs) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(17, Long.valueOf(timeMs)));
    }

    public void timeShiftSetPlaybackParams(PlaybackParams params) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(18, params));
    }

    public void timeShiftEnablePositionTracking(boolean enable) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(19, Boolean.valueOf(enable)));
    }

    public void startRecording(Uri programUri) {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessageO(20, programUri));
    }

    public void stopRecording() {
        this.mCaller.executeOrSendMessage(this.mCaller.obtainMessage(21));
    }
}
