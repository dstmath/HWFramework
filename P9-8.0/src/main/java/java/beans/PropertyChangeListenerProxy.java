package java.beans;

import java.util.EventListenerProxy;

public class PropertyChangeListenerProxy extends EventListenerProxy<PropertyChangeListener> implements PropertyChangeListener {
    private final String propertyName;

    public PropertyChangeListenerProxy(String propertyName, PropertyChangeListener listener) {
        super(listener);
        this.propertyName = propertyName;
    }

    public void propertyChange(PropertyChangeEvent event) {
        ((PropertyChangeListener) getListener()).propertyChange(event);
    }

    public String getPropertyName() {
        return this.propertyName;
    }
}
