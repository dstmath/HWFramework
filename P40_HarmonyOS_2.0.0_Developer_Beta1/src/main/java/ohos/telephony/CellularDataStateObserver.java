package ohos.telephony;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

public class CellularDataStateObserver {
    public static final int OBSERVE_MASK_DATA_CONNECTION_STATE = 1;
    public static final int OBSERVE_MASK_DATA_FLOW = 2;
    public static final int OBSERVE_MASK_NONE = 0;
    protected final ICellularDataStateObserver callback;
    protected int slotId;

    public void onCellularDataConnectStateUpdated(int i, int i2) {
    }

    public void onCellularDataFlow(int i) {
    }

    public CellularDataStateObserver(int i) {
        this(i, EventRunner.current());
    }

    public CellularDataStateObserver(int i, EventRunner eventRunner) {
        if (eventRunner != null) {
            this.slotId = i;
            this.callback = new CellularDataStateObserverCallback(this, new EventHandler(eventRunner));
            return;
        }
        throw new IllegalArgumentException("runner must be non-null");
    }
}
