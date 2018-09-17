package sun.security.action;

import java.security.PrivilegedAction;

public class GetIntegerAction implements PrivilegedAction<Integer> {
    private boolean defaultSet;
    private int defaultVal;
    private String theProp;

    public GetIntegerAction(String theProp) {
        this.defaultSet = false;
        this.theProp = theProp;
    }

    public GetIntegerAction(String theProp, int defaultVal) {
        this.defaultSet = false;
        this.theProp = theProp;
        this.defaultVal = defaultVal;
        this.defaultSet = true;
    }

    public Integer run() {
        Integer value = Integer.getInteger(this.theProp);
        if (value == null && this.defaultSet) {
            return new Integer(this.defaultVal);
        }
        return value;
    }
}
