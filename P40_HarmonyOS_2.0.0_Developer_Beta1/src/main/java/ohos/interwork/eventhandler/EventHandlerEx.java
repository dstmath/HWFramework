package ohos.interwork.eventhandler;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.interwork.eventhandler.ICourierEx;
import ohos.rpc.IPCSkeleton;

public class EventHandlerEx extends EventHandler {
    private ICourierEx courierEx;

    public EventHandlerEx(EventRunner eventRunner) throws IllegalArgumentException {
        super(eventRunner);
    }

    /* access modifiers changed from: package-private */
    public final ICourierEx getICourierEx() {
        ICourierEx iCourierEx = this.courierEx;
        if (iCourierEx != null) {
            return iCourierEx;
        }
        this.courierEx = new CourierExImpl();
        return this.courierEx;
    }

    /* access modifiers changed from: private */
    public final class CourierExImpl extends ICourierEx.Stub {
        private CourierExImpl() {
        }

        public void send(InnerEvent innerEvent) {
            innerEvent.sendingUid = IPCSkeleton.getCallingUid();
            EventHandlerEx.this.sendEvent(innerEvent);
        }
    }
}
