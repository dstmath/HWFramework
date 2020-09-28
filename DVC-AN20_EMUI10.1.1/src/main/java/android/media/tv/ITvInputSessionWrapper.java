package android.media.tv;

import android.content.Context;
import android.graphics.Rect;
import android.media.PlaybackParams;
import android.media.tv.ITvInputSession;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TimeUtils;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.Surface;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;

public class ITvInputSessionWrapper extends ITvInputSession.Stub implements HandlerCaller.Callback {
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
    private TvInputService.RecordingSession mTvInputRecordingSessionImpl;
    private TvInputService.Session mTvInputSessionImpl;

    public ITvInputSessionWrapper(Context context, TvInputService.Session sessionImpl, InputChannel channel) {
        this.mCaller = new HandlerCaller(context, null, this, true);
        this.mTvInputSessionImpl = sessionImpl;
        this.mChannel = channel;
        if (channel != null) {
            this.mReceiver = new TvInputEventReceiver(channel, context.getMainLooper());
        }
    }

    public ITvInputSessionWrapper(Context context, TvInputService.RecordingSession recordingSessionImpl) {
        this.mCaller = new HandlerCaller(context, null, this, true);
        this.mTvInputRecordingSessionImpl = recordingSessionImpl;
    }

    @Override // com.android.internal.os.HandlerCaller.Callback
    public void executeMessage(Message msg) {
        if (this.mIsRecordingSession && this.mTvInputRecordingSessionImpl == null) {
            return;
        }
        if (this.mIsRecordingSession || this.mTvInputSessionImpl != null) {
            long startTime = System.nanoTime();
            switch (msg.what) {
                case 1:
                    if (!this.mIsRecordingSession) {
                        this.mTvInputSessionImpl.release();
                        this.mTvInputSessionImpl = null;
                        TvInputEventReceiver tvInputEventReceiver = this.mReceiver;
                        if (tvInputEventReceiver != null) {
                            tvInputEventReceiver.dispose();
                            this.mReceiver = null;
                        }
                        InputChannel inputChannel = this.mChannel;
                        if (inputChannel != null) {
                            inputChannel.dispose();
                            this.mChannel = null;
                            break;
                        }
                    } else {
                        this.mTvInputRecordingSessionImpl.release();
                        this.mTvInputRecordingSessionImpl = null;
                        break;
                    }
                    break;
                case 2:
                    this.mTvInputSessionImpl.setMain(((Boolean) msg.obj).booleanValue());
                    break;
                case 3:
                    this.mTvInputSessionImpl.setSurface((Surface) msg.obj);
                    break;
                case 4:
                    SomeArgs args = (SomeArgs) msg.obj;
                    this.mTvInputSessionImpl.dispatchSurfaceChanged(args.argi1, args.argi2, args.argi3);
                    args.recycle();
                    break;
                case 5:
                    this.mTvInputSessionImpl.setStreamVolume(((Float) msg.obj).floatValue());
                    break;
                case 6:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    if (this.mIsRecordingSession) {
                        this.mTvInputRecordingSessionImpl.tune((Uri) args2.arg1, (Bundle) args2.arg2);
                    } else {
                        this.mTvInputSessionImpl.tune((Uri) args2.arg1, (Bundle) args2.arg2);
                    }
                    args2.recycle();
                    break;
                case 7:
                    this.mTvInputSessionImpl.setCaptionEnabled(((Boolean) msg.obj).booleanValue());
                    break;
                case 8:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    this.mTvInputSessionImpl.selectTrack(((Integer) args3.arg1).intValue(), (String) args3.arg2);
                    args3.recycle();
                    break;
                case 9:
                    SomeArgs args4 = (SomeArgs) msg.obj;
                    if (this.mIsRecordingSession) {
                        this.mTvInputRecordingSessionImpl.appPrivateCommand((String) args4.arg1, (Bundle) args4.arg2);
                    } else {
                        this.mTvInputSessionImpl.appPrivateCommand((String) args4.arg1, (Bundle) args4.arg2);
                    }
                    args4.recycle();
                    break;
                case 10:
                    SomeArgs args5 = (SomeArgs) msg.obj;
                    this.mTvInputSessionImpl.createOverlayView((IBinder) args5.arg1, (Rect) args5.arg2);
                    args5.recycle();
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
            long durationMs = (System.nanoTime() - startTime) / TimeUtils.NANOS_PER_MS;
            if (durationMs > 50) {
                Log.w(TAG, "Handling message (" + msg.what + ") took too long time (duration=" + durationMs + "ms)");
                if (msg.what == 6 && durationMs > 2000) {
                    throw new RuntimeException("Too much time to handle tune request. (" + durationMs + "ms > " + 2000 + "ms) Consider handling the tune request in a separate thread.");
                } else if (durationMs > 5000) {
                    throw new RuntimeException("Too much time to handle a request. (type=" + msg.what + ", " + durationMs + "ms > " + 5000 + "ms).");
                }
            }
        }
    }

    @Override // android.media.tv.ITvInputSession
    public void release() {
        if (!this.mIsRecordingSession) {
            this.mTvInputSessionImpl.scheduleOverlayViewCleanup();
        }
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(1));
    }

    @Override // android.media.tv.ITvInputSession
    public void setMain(boolean isMain) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(2, Boolean.valueOf(isMain)));
    }

    @Override // android.media.tv.ITvInputSession
    public void setSurface(Surface surface) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(3, surface));
    }

    @Override // android.media.tv.ITvInputSession
    public void dispatchSurfaceChanged(int format, int width, int height) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageIIII(4, format, width, height, 0));
    }

    @Override // android.media.tv.ITvInputSession
    public final void setVolume(float volume) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(5, Float.valueOf(volume)));
    }

    @Override // android.media.tv.ITvInputSession
    public void tune(Uri channelUri, Bundle params) {
        this.mCaller.removeMessages(6);
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageOO(6, channelUri, params));
    }

    @Override // android.media.tv.ITvInputSession
    public void setCaptionEnabled(boolean enabled) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(7, Boolean.valueOf(enabled)));
    }

    @Override // android.media.tv.ITvInputSession
    public void selectTrack(int type, String trackId) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageOO(8, Integer.valueOf(type), trackId));
    }

    @Override // android.media.tv.ITvInputSession
    public void appPrivateCommand(String action, Bundle data) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageOO(9, action, data));
    }

    @Override // android.media.tv.ITvInputSession
    public void createOverlayView(IBinder windowToken, Rect frame) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageOO(10, windowToken, frame));
    }

    @Override // android.media.tv.ITvInputSession
    public void relayoutOverlayView(Rect frame) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(11, frame));
    }

    @Override // android.media.tv.ITvInputSession
    public void removeOverlayView() {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(12));
    }

    @Override // android.media.tv.ITvInputSession
    public void unblockContent(String unblockedRating) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(13, unblockedRating));
    }

    @Override // android.media.tv.ITvInputSession
    public void timeShiftPlay(Uri recordedProgramUri) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(14, recordedProgramUri));
    }

    @Override // android.media.tv.ITvInputSession
    public void timeShiftPause() {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(15));
    }

    @Override // android.media.tv.ITvInputSession
    public void timeShiftResume() {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(16));
    }

    @Override // android.media.tv.ITvInputSession
    public void timeShiftSeekTo(long timeMs) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(17, Long.valueOf(timeMs)));
    }

    @Override // android.media.tv.ITvInputSession
    public void timeShiftSetPlaybackParams(PlaybackParams params) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(18, params));
    }

    @Override // android.media.tv.ITvInputSession
    public void timeShiftEnablePositionTracking(boolean enable) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(19, Boolean.valueOf(enable)));
    }

    @Override // android.media.tv.ITvInputSession
    public void startRecording(Uri programUri) {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessageO(20, programUri));
    }

    @Override // android.media.tv.ITvInputSession
    public void stopRecording() {
        HandlerCaller handlerCaller = this.mCaller;
        handlerCaller.executeOrSendMessage(handlerCaller.obtainMessage(21));
    }

    private final class TvInputEventReceiver extends InputEventReceiver {
        public TvInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override // android.view.InputEventReceiver
        public void onInputEvent(InputEvent event) {
            boolean z = false;
            if (ITvInputSessionWrapper.this.mTvInputSessionImpl == null) {
                finishInputEvent(event, false);
                return;
            }
            int handled = ITvInputSessionWrapper.this.mTvInputSessionImpl.dispatchInputEvent(event, this);
            if (handled != -1) {
                if (handled == 1) {
                    z = true;
                }
                finishInputEvent(event, z);
            }
        }
    }
}
