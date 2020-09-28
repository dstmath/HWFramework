package android.view;

import android.annotation.UnsupportedAppUsage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.view.IInputFilter;

public abstract class InputFilter extends IInputFilter.Stub {
    private static final int MSG_INPUT_EVENT = 3;
    private static final int MSG_INSTALL = 1;
    private static final int MSG_UNINSTALL = 2;
    private final H mH;
    private IInputFilterHost mHost;
    private final InputEventConsistencyVerifier mInboundInputEventConsistencyVerifier;
    private final InputEventConsistencyVerifier mOutboundInputEventConsistencyVerifier;

    @UnsupportedAppUsage
    public InputFilter(Looper looper) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier;
        InputEventConsistencyVerifier inputEventConsistencyVerifier2 = null;
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            inputEventConsistencyVerifier = new InputEventConsistencyVerifier(this, 1, "InputFilter#InboundInputEventConsistencyVerifier");
        } else {
            inputEventConsistencyVerifier = null;
        }
        this.mInboundInputEventConsistencyVerifier = inputEventConsistencyVerifier;
        this.mOutboundInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 1, "InputFilter#OutboundInputEventConsistencyVerifier") : inputEventConsistencyVerifier2;
        this.mH = new H(looper);
    }

    @Override // android.view.IInputFilter
    public final void install(IInputFilterHost host) {
        this.mH.obtainMessage(1, host).sendToTarget();
    }

    @Override // android.view.IInputFilter
    public final void uninstall() {
        this.mH.obtainMessage(2).sendToTarget();
    }

    @Override // android.view.IInputFilter
    public final void filterInputEvent(InputEvent event, int policyFlags) {
        this.mH.obtainMessage(3, policyFlags, 0, event).sendToTarget();
    }

    public void sendInputEvent(InputEvent event, int policyFlags) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (this.mHost != null) {
            InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mOutboundInputEventConsistencyVerifier;
            if (inputEventConsistencyVerifier != null) {
                inputEventConsistencyVerifier.onInputEvent(event, 0);
            }
            try {
                this.mHost.sendInputEvent(event, policyFlags);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Cannot send input event because the input filter is not installed.");
        }
    }

    @UnsupportedAppUsage
    public void onInputEvent(InputEvent event, int policyFlags) {
        sendInputEvent(event, policyFlags);
    }

    public void onInstalled() {
    }

    public void onUninstalled() {
    }

    private final class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                InputFilter.this.mHost = (IInputFilterHost) msg.obj;
                if (InputFilter.this.mInboundInputEventConsistencyVerifier != null) {
                    InputFilter.this.mInboundInputEventConsistencyVerifier.reset();
                }
                if (InputFilter.this.mOutboundInputEventConsistencyVerifier != null) {
                    InputFilter.this.mOutboundInputEventConsistencyVerifier.reset();
                }
                InputFilter.this.onInstalled();
            } else if (i == 2) {
                try {
                    InputFilter.this.onUninstalled();
                } finally {
                    InputFilter.this.mHost = null;
                }
            } else if (i == 3) {
                InputEvent event = (InputEvent) msg.obj;
                try {
                    if (InputFilter.this.mInboundInputEventConsistencyVerifier != null) {
                        InputFilter.this.mInboundInputEventConsistencyVerifier.onInputEvent(event, 0);
                    }
                    InputFilter.this.onInputEvent(event, msg.arg1);
                } finally {
                    event.recycle();
                }
            }
        }
    }
}
