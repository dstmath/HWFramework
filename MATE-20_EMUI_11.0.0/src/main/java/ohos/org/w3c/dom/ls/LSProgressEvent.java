package ohos.org.w3c.dom.ls;

import ohos.org.w3c.dom.events.Event;

public interface LSProgressEvent extends Event {
    LSInput getInput();

    int getPosition();

    int getTotalSize();
}
