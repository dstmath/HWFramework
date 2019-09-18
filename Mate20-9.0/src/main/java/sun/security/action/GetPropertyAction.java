package sun.security.action;

import java.security.PrivilegedAction;

public class GetPropertyAction implements PrivilegedAction<String> {
    private String defaultVal;
    private String theProp;

    public GetPropertyAction(String theProp2) {
        this.theProp = theProp2;
    }

    public GetPropertyAction(String theProp2, String defaultVal2) {
        this.theProp = theProp2;
        this.defaultVal = defaultVal2;
    }

    public String run() {
        String value = System.getProperty(this.theProp);
        return value == null ? this.defaultVal : value;
    }
}
