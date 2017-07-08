package java.security;

import java.io.Serializable;

public interface Key extends Serializable {
    public static final long serialVersionUID = 6603384152749567654L;

    String getAlgorithm();

    byte[] getEncoded();

    String getFormat();
}
