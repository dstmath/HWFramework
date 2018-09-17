package sun.security.action;

import java.security.PrivilegedAction;

public class GetIntegerAction implements PrivilegedAction<Integer> {
    private boolean defaultSet = false;
    private int defaultVal;
    private String theProp;

    public GetIntegerAction(String theProp) {
        this.theProp = theProp;
    }

    public GetIntegerAction(String theProp, int defaultVal) {
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
