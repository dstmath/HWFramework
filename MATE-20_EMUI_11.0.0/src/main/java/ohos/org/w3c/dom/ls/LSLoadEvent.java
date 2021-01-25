package ohos.org.w3c.dom.ls;

import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.events.Event;

public interface LSLoadEvent extends Event {
    LSInput getInput();

    Document getNewDocument();
}
