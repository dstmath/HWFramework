package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.server.SystemService;
import com.android.server.usb.descriptors.UsbACInterface;

public class AutoclickController extends BaseEventStreamTransformation {
    private static final String LOG_TAG = AutoclickController.class.getSimpleName();
    private ClickDelayObserver mClickDelayObserver;
    private ClickScheduler mClickScheduler;
    private final Context mContext;
    private final int mUserId;

    @Override // com.android.server.accessibility.BaseEventStreamTransformation, com.android.server.accessibility.EventStreamTransformation
    public /* bridge */ /* synthetic */ EventStreamTransformation getNext() {
        return super.getNext();
    }

    @Override // com.android.server.accessibility.BaseEventStreamTransformation, com.android.server.accessibility.EventStreamTransformation
    public /* bridge */ /* synthetic */ void setNext(EventStreamTransformation eventStreamTransformation) {
        super.setNext(eventStreamTransformation);
    }

    public AutoclickController(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (event.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            if (this.mClickScheduler == null) {
                Handler handler = new Handler(this.mContext.getMainLooper());
                this.mClickScheduler = new ClickScheduler(handler, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
                this.mClickDelayObserver = new ClickDelayObserver(this.mUserId, handler);
                this.mClickDelayObserver.start(this.mContext.getContentResolver(), this.mClickScheduler);
            }
            handleMouseMotion(event, policyFlags);
        } else {
            ClickScheduler clickScheduler = this.mClickScheduler;
            if (clickScheduler != null) {
                clickScheduler.cancel();
            }
        }
        super.onMotionEvent(event, rawEvent, policyFlags);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onKeyEvent(KeyEvent event, int policyFlags) {
        if (this.mClickScheduler != null) {
            if (KeyEvent.isModifierKey(event.getKeyCode())) {
                this.mClickScheduler.updateMetaState(event.getMetaState());
            } else {
                this.mClickScheduler.cancel();
            }
        }
        super.onKeyEvent(event, policyFlags);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void clearEvents(int inputSource) {
        ClickScheduler clickScheduler;
        if (inputSource == 8194 && (clickScheduler = this.mClickScheduler) != null) {
            clickScheduler.cancel();
        }
        super.clearEvents(inputSource);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onDestroy() {
        ClickDelayObserver clickDelayObserver = this.mClickDelayObserver;
        if (clickDelayObserver != null) {
            clickDelayObserver.stop();
            this.mClickDelayObserver = null;
        }
        ClickScheduler clickScheduler = this.mClickScheduler;
        if (clickScheduler != null) {
            clickScheduler.cancel();
            this.mClickScheduler = null;
        }
    }

    private void handleMouseMotion(MotionEvent event, int policyFlags) {
        int actionMasked = event.getActionMasked();
        if (actionMasked != 7) {
            if (actionMasked != 9 && actionMasked != 10) {
                this.mClickScheduler.cancel();
            }
        } else if (event.getPointerCount() == 1) {
            this.mClickScheduler.update(event, policyFlags);
        } else {
            this.mClickScheduler.cancel();
        }
    }

    private static final class ClickDelayObserver extends ContentObserver {
        private final Uri mAutoclickDelaySettingUri = Settings.Secure.getUriFor("accessibility_autoclick_delay");
        private ClickScheduler mClickScheduler;
        private ContentResolver mContentResolver;
        private final int mUserId;

        public ClickDelayObserver(int userId, Handler handler) {
            super(handler);
            this.mUserId = userId;
        }

        public void start(ContentResolver contentResolver, ClickScheduler clickScheduler) {
            if (this.mContentResolver != null || this.mClickScheduler != null) {
                throw new IllegalStateException("Observer already started.");
            } else if (contentResolver == null) {
                throw new NullPointerException("contentResolver not set.");
            } else if (clickScheduler != null) {
                this.mContentResolver = contentResolver;
                this.mClickScheduler = clickScheduler;
                this.mContentResolver.registerContentObserver(this.mAutoclickDelaySettingUri, false, this, this.mUserId);
                onChange(true, this.mAutoclickDelaySettingUri);
            } else {
                throw new NullPointerException("clickScheduler not set.");
            }
        }

        public void stop() {
            ContentResolver contentResolver = this.mContentResolver;
            if (contentResolver == null || this.mClickScheduler == null) {
                throw new IllegalStateException("ClickDelayObserver not started.");
            }
            contentResolver.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (this.mAutoclickDelaySettingUri.equals(uri)) {
                this.mClickScheduler.updateDelay(Settings.Secure.getIntForUser(this.mContentResolver, "accessibility_autoclick_delay", SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, this.mUserId));
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ClickScheduler implements Runnable {
        private static final double MOVEMENT_SLOPE = 20.0d;
        private boolean mActive;
        private MotionEvent.PointerCoords mAnchorCoords;
        private int mDelay;
        private int mEventPolicyFlags;
        private Handler mHandler;
        private MotionEvent mLastMotionEvent = null;
        private int mMetaState;
        private long mScheduledClickTime;
        private MotionEvent.PointerCoords[] mTempPointerCoords;
        private MotionEvent.PointerProperties[] mTempPointerProperties;

        public ClickScheduler(Handler handler, int delay) {
            this.mHandler = handler;
            resetInternalState();
            this.mDelay = delay;
            this.mAnchorCoords = new MotionEvent.PointerCoords();
        }

        @Override // java.lang.Runnable
        public void run() {
            long now = SystemClock.uptimeMillis();
            long j = this.mScheduledClickTime;
            if (now < j) {
                this.mHandler.postDelayed(this, j - now);
                return;
            }
            sendClick();
            resetInternalState();
        }

        public void update(MotionEvent event, int policyFlags) {
            this.mMetaState = event.getMetaState();
            boolean moved = detectMovement(event);
            cacheLastEvent(event, policyFlags, this.mLastMotionEvent == null || moved);
            if (moved) {
                rescheduleClick(this.mDelay);
            }
        }

        public void cancel() {
            if (this.mActive) {
                resetInternalState();
                this.mHandler.removeCallbacks(this);
            }
        }

        public void updateMetaState(int state) {
            this.mMetaState = state;
        }

        public void updateDelay(int delay) {
            this.mDelay = delay;
        }

        private void rescheduleClick(int delay) {
            long clickTime = SystemClock.uptimeMillis() + ((long) delay);
            if (!this.mActive || clickTime <= this.mScheduledClickTime) {
                if (this.mActive) {
                    this.mHandler.removeCallbacks(this);
                }
                this.mActive = true;
                this.mScheduledClickTime = clickTime;
                this.mHandler.postDelayed(this, (long) delay);
                return;
            }
            this.mScheduledClickTime = clickTime;
        }

        private void cacheLastEvent(MotionEvent event, int policyFlags, boolean useAsAnchor) {
            MotionEvent motionEvent = this.mLastMotionEvent;
            if (motionEvent != null) {
                motionEvent.recycle();
            }
            this.mLastMotionEvent = MotionEvent.obtain(event);
            this.mEventPolicyFlags = policyFlags;
            if (useAsAnchor) {
                this.mLastMotionEvent.getPointerCoords(this.mLastMotionEvent.getActionIndex(), this.mAnchorCoords);
            }
        }

        private void resetInternalState() {
            this.mActive = false;
            MotionEvent motionEvent = this.mLastMotionEvent;
            if (motionEvent != null) {
                motionEvent.recycle();
                this.mLastMotionEvent = null;
            }
            this.mScheduledClickTime = -1;
        }

        private boolean detectMovement(MotionEvent event) {
            if (this.mLastMotionEvent == null) {
                return false;
            }
            int pointerIndex = event.getActionIndex();
            if (Math.hypot((double) (this.mAnchorCoords.x - event.getX(pointerIndex)), (double) (this.mAnchorCoords.y - event.getY(pointerIndex))) > MOVEMENT_SLOPE) {
                return true;
            }
            return false;
        }

        private void sendClick() {
            if (this.mLastMotionEvent != null && AutoclickController.this.getNext() != null) {
                int pointerIndex = this.mLastMotionEvent.getActionIndex();
                if (this.mTempPointerProperties == null) {
                    this.mTempPointerProperties = new MotionEvent.PointerProperties[1];
                    this.mTempPointerProperties[0] = new MotionEvent.PointerProperties();
                }
                this.mLastMotionEvent.getPointerProperties(pointerIndex, this.mTempPointerProperties[0]);
                if (this.mTempPointerCoords == null) {
                    this.mTempPointerCoords = new MotionEvent.PointerCoords[1];
                    this.mTempPointerCoords[0] = new MotionEvent.PointerCoords();
                }
                this.mLastMotionEvent.getPointerCoords(pointerIndex, this.mTempPointerCoords[0]);
                long now = SystemClock.uptimeMillis();
                MotionEvent downEvent = MotionEvent.obtain(now, now, 0, 1, this.mTempPointerProperties, this.mTempPointerCoords, this.mMetaState, 1, 1.0f, 1.0f, this.mLastMotionEvent.getDeviceId(), 0, this.mLastMotionEvent.getSource(), this.mLastMotionEvent.getFlags());
                MotionEvent upEvent = MotionEvent.obtain(downEvent);
                upEvent.setAction(1);
                AutoclickController.super.onMotionEvent(downEvent, downEvent, this.mEventPolicyFlags);
                downEvent.recycle();
                AutoclickController.super.onMotionEvent(upEvent, upEvent, this.mEventPolicyFlags);
                upEvent.recycle();
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return "ClickScheduler: { active=" + this.mActive + ", delay=" + this.mDelay + ", scheduledClickTime=" + this.mScheduledClickTime + ", anchor={x:" + this.mAnchorCoords.x + ", y:" + this.mAnchorCoords.y + "}, metastate=" + this.mMetaState + ", policyFlags=" + this.mEventPolicyFlags + ", lastMotionEvent=" + this.mLastMotionEvent + " }";
        }
    }
}
