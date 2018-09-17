package sun.security.action;

import java.security.PrivilegedAction;

public class GetPropertyAction implements PrivilegedAction<String> {
    private String defaultVal;
    private String theProp;

    public GetPropertyAction(String theProp) {
        this.theProp = theProp;
    }

    public GetPropertyAction(String theProp, String defaultVal) {
        this.theProp = theProp;
        this.defaultVal = defaultVal;
    }

    public String run() {
        String value = System.getProperty(this.theProp);
        return value == null ? this.defaultVal : value;
    }
}
