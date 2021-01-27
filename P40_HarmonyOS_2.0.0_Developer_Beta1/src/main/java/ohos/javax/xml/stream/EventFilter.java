package ohos.javax.xml.stream;

import ohos.javax.xml.stream.events.XMLEvent;

public interface EventFilter {
    boolean accept(XMLEvent xMLEvent);
}
