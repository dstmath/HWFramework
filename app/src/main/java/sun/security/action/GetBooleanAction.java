package sun.security.action;

import java.security.PrivilegedAction;

public class GetBooleanAction implements PrivilegedAction<Boolean> {
    private String theProp;

    public GetBooleanAction(String theProp) {
        this.theProp = theProp;
    }

    public Boolean run() {
        return Boolean.valueOf(Boolean.getBoolean(this.theProp));
    }
}
