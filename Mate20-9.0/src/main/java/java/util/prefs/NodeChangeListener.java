package java.util.prefs;

import java.util.EventListener;

public interface NodeChangeListener extends EventListener {
    void childAdded(NodeChangeEvent nodeChangeEvent);

    void childRemoved(NodeChangeEvent nodeChangeEvent);
}
