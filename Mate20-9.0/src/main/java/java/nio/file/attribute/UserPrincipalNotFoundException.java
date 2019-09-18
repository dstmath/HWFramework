package java.nio.file.attribute;

import java.io.IOException;

public class UserPrincipalNotFoundException extends IOException {
    static final long serialVersionUID = -5369283889045833024L;
    private final String name;

    public UserPrincipalNotFoundException(String name2) {
        this.name = name2;
    }

    public String getName() {
        return this.name;
    }
}
