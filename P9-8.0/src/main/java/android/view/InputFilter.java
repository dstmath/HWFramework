package android.view;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.view.IInputFilter.Stub;

public abstract class InputFilter extends Stub {
    private static final int MSG_INPUT_EVENT = 3;
    private static final int MSG_INSTALL = 1;
    private static final int MSG_UNINSTALL = 2;
    private final H mH;
    private IInputFilterHost mHost;
    private final InputEventConsistencyVerifier mInboundInputEventConsistencyVerifier;
    private final InputEventConsistencyVerifier mOutboundInputEventConsistencyVerifier;

    private final class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    InputFilter.this.mHost = (IInputFilterHost) msg.obj;
                    if (InputFilter.this.mInboundInputEventConsistencyVerifier != null) {
                        InputFilter.this.mInboundInputEventConsistencyVerifier.reset();
                    }
                    if (InputFilter.this.mOutboundInputEventConsistencyVerifier != null) {
                        InputFilter.this.mOutboundInputEventConsistencyVerifier.reset();
                    }
                    InputFilter.this.onInstalled();
                    return;
                case 2:
                    try {
                        InputFilter.this.onUninstalled();
                        return;
                    } finally {
                        InputFilter.this.mHost = null;
                    }
                case 3:
                    InputEvent event = msg.obj;
                    try {
                        if (InputFilter.this.mInboundInputEventConsistencyVerifier != null) {
                            InputFilter.this.mInboundInputEventConsistencyVerifier.onInputEvent(event, 0);
                        }
                        InputFilter.this.onInputEvent(event, msg.arg1);
                        return;
                    } finally {
                        event.recycle();
                    }
                default:
                    return;
            }
        }
    }

    public InputFilter(Looper looper) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier;
        InputEventConsistencyVerifier inputEventConsistencyVerifier2 = null;
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            inputEventConsistencyVerifier = new InputEventConsistencyVerifier(this, 1, "InputFilter#InboundInputEventConsistencyVerifier");
        } else {
            inputEventConsistencyVerifier = null;
        }
        this.mInboundInputEventConsistencyVerifier = inputEventConsistencyVerifier;
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            inputEventConsistencyVerifier2 = new InputEventConsistencyVerifier(this, 1, "InputFilter#OutboundInputEventConsistencyVerifier");
        }
        this.mOutboundInputEventConsistencyVerifier = inputEventConsistencyVerifier2;
        this.mH = new H(looper);
    }

    public final void install(IInputFilterHost host) {
        this.mH.obtainMessage(1, host).sendToTarget();
    }

    public final void uninstall() {
        this.mH.obtainMessage(2).sendToTarget();
    }

    public final void filterInputEvent(InputEvent event, int policyFlags) {
        this.mH.obtainMessage(3, policyFlags, 0, event).sendToTarget();
    }

    public void sendInputEvent(InputEvent event, int policyFlags) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (this.mHost == null) {
            throw new IllegalStateException("Cannot send input event because the input filter is not installed.");
        } else {
            if (this.mOutboundInputEventConsistencyVerifier != null) {
                this.mOutboundInputEventConsistencyVerifier.onInputEvent(event, 0);
            }
            try {
                this.mHost.sendInputEvent(event, policyFlags);
            } catch (RemoteException e) {
            }
        }
    }

    public void onInputEvent(InputEvent event, int policyFlags) {
        sendInputEvent(event, policyFlags);
    }

    public void onInstalled() {
    }

    public void onUninstalled() {
    }
}
