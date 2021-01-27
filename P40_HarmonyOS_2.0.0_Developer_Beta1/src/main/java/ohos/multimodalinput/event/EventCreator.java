package ohos.multimodalinput.event;

import java.util.Optional;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public final class EventCreator {
    public static Optional<KeyEvent> createKeyEvent(int i, int i2) {
        return MultimodalEventFactory.createKeyEvent(i, i2);
    }

    private EventCreator() {
    }
}
