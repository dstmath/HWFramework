package ohos.telephony;

import java.util.List;
import ohos.annotation.SystemApi;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

public class RadioStateObserver {
    public static final int OBSERVE_MASK_CELL_INFO = 4;
    public static final int OBSERVE_MASK_NETWORK_STATE = 1;
    public static final int OBSERVE_MASK_NONE = 0;
    public static final int OBSERVE_MASK_SIGNAL_INFO = 2;
    public final IRadioStateObserver callback;
    protected int slotId;

    @SystemApi
    public void onCellInfoUpdated(List<CellInformation> list) {
    }

    public void onNetworkStateUpdated(NetworkState networkState) {
    }

    public void onSignalInfoUpdated(List<SignalInformation> list) {
    }

    public RadioStateObserver(int i) {
        this(i, EventRunner.current());
    }

    public RadioStateObserver(int i, EventRunner eventRunner) {
        if (eventRunner != null) {
            this.slotId = i;
            this.callback = new RadioStateObserverCallback(this, new EventHandler(eventRunner));
            return;
        }
        throw new IllegalArgumentException("runner must be non-null");
    }
}
