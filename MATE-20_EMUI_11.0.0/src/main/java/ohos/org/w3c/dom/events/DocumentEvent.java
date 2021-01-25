package ohos.org.w3c.dom.events;

import ohos.org.w3c.dom.DOMException;

public interface DocumentEvent {
    Event createEvent(String str) throws DOMException;
}
