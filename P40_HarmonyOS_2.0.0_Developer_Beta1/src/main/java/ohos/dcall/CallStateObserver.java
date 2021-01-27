package ohos.dcall;

import java.lang.ref.WeakReference;
import ohos.dcall.CallStateObserver;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

public class CallStateObserver {
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_OFFHOOK = 2;
    public static final int CALL_STATE_RINGING = 1;
    public static final int CALL_STATE_UNKNOWN = -1;
    public static final int OBSERVE_MASK_CALL_FORWARDING_UNCONDITIONAL_INDICATOR = 2;
    public static final int OBSERVE_MASK_CALL_STATE = 4;
    public static final int OBSERVE_MASK_VOICE_MAIL_MESSAGE_INDICATOR = 1;
    protected final ICallStateObserver callback;
    protected int slotId;

    public void onCallStateUpdated(int i, String str) {
    }

    public void onCfuIndicatorUpdated(boolean z) {
    }

    public void onVoiceMailMsgIndicatorUpdated(boolean z) {
    }

    public CallStateObserver(int i) throws IllegalArgumentException {
        this(i, EventRunner.current());
    }

    public CallStateObserver(int i, EventRunner eventRunner) throws IllegalArgumentException {
        if (eventRunner != null) {
            this.slotId = i;
            this.callback = new CallStateObserverCallback(this, new EventHandler(eventRunner));
            return;
        }
        throw new IllegalArgumentException("runner must be non-null");
    }

    /* access modifiers changed from: protected */
    public void setReadCallLogPermission(boolean z) {
        ICallStateObserver iCallStateObserver = this.callback;
        if (iCallStateObserver != null) {
            iCallStateObserver.setReadCallLogPermission(z);
        }
    }

    /* access modifiers changed from: private */
    public static class CallStateObserverCallback extends CallStateObserverSkeleton {
        private WeakReference<CallStateObserver> callStateObserverWeakRef;
        private EventHandler handler;
        private boolean readCallLogPermission = false;

        public CallStateObserverCallback(CallStateObserver callStateObserver, EventHandler eventHandler) {
            this.callStateObserverWeakRef = new WeakReference<>(callStateObserver);
            this.handler = eventHandler;
        }

        @Override // ohos.dcall.ICallStateObserver
        public void onVoiceMailMsgIndicatorUpdated(boolean z) {
            CallStateObserver callStateObserver = this.callStateObserverWeakRef.get();
            if (callStateObserver != null) {
                this.handler.postTask(new Runnable(z) {
                    /* class ohos.dcall.$$Lambda$CallStateObserver$CallStateObserverCallback$GrgXT17xWk6bnIYUARKdrSgianQ */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallStateObserver.CallStateObserverCallback.lambda$onVoiceMailMsgIndicatorUpdated$0(CallStateObserver.this, this.f$1);
                    }
                });
            }
        }

        @Override // ohos.dcall.ICallStateObserver
        public void onCfuIndicatorUpdated(boolean z) {
            CallStateObserver callStateObserver = this.callStateObserverWeakRef.get();
            if (callStateObserver != null) {
                this.handler.postTask(new Runnable(z) {
                    /* class ohos.dcall.$$Lambda$CallStateObserver$CallStateObserverCallback$4A382L8PjxDkuXxPjAKZrQgpLZE */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallStateObserver.CallStateObserverCallback.lambda$onCfuIndicatorUpdated$1(CallStateObserver.this, this.f$1);
                    }
                });
            }
        }

        @Override // ohos.dcall.ICallStateObserver
        public void onCallStateUpdated(int i, String str) {
            CallStateObserver callStateObserver = this.callStateObserverWeakRef.get();
            if (callStateObserver != null) {
                if (this.readCallLogPermission) {
                    this.handler.postTask(new Runnable(i, str) {
                        /* class ohos.dcall.$$Lambda$CallStateObserver$CallStateObserverCallback$d1rMtMR3eJJ01ylvjQShjEW41j4 */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CallStateObserver.CallStateObserverCallback.lambda$onCallStateUpdated$2(CallStateObserver.this, this.f$1, this.f$2);
                        }
                    });
                } else {
                    this.handler.postTask(new Runnable(i) {
                        /* class ohos.dcall.$$Lambda$CallStateObserver$CallStateObserverCallback$tBoG7pD7MXY0t3pcUkEwnkV5tXk */
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CallStateObserver.CallStateObserverCallback.lambda$onCallStateUpdated$3(CallStateObserver.this, this.f$1);
                        }
                    });
                }
            }
        }

        @Override // ohos.dcall.ICallStateObserver
        public void setReadCallLogPermission(boolean z) {
            this.readCallLogPermission = z;
        }
    }
}
