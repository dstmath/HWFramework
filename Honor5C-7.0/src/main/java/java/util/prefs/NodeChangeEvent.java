package java.util.prefs;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventObject;

public class NodeChangeEvent extends EventObject {
    private static final long serialVersionUID = 8068949086596572957L;
    private Preferences child;

    public NodeChangeEvent(Preferences parent, Preferences child) {
        super(parent);
        this.child = child;
    }

    public Preferences getParent() {
        return (Preferences) getSource();
    }

    public Preferences getChild() {
        return this.child;
    }

    private void writeObject(ObjectOutputStream out) throws NotSerializableException {
        throw new NotSerializableException("Not serializable.");
    }

    private void readObject(ObjectInputStream in) throws NotSerializableException {
        throw new NotSerializableException("Not serializable.");
    }
}
