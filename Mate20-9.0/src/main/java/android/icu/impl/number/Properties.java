package android.icu.impl.number;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Properties implements Serializable {
    private static final long serialVersionUID = 4095518955889349243L;
    private transient DecimalFormatProperties instance;

    public DecimalFormatProperties getInstance() {
        return this.instance;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        if (this.instance == null) {
            this.instance = new DecimalFormatProperties();
        }
        this.instance.readObjectImpl(ois);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        if (this.instance == null) {
            this.instance = new DecimalFormatProperties();
        }
        this.instance.writeObjectImpl(oos);
    }
}
