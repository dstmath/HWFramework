package sun.security.action;

import java.security.PrivilegedAction;

public class GetIntegerAction implements PrivilegedAction<Integer> {
    private boolean defaultSet = false;
    private int defaultVal;
    private String theProp;

    public GetIntegerAction(String theProp2) {
        this.theProp = theProp2;
    }

    public GetIntegerAction(String theProp2, int defaultVal2) {
        this.theProp = theProp2;
        this.defaultVal = defaultVal2;
        this.defaultSet = true;
    }

    public Integer run() {
        Integer value = Integer.getInteger(this.theProp);
        if (value != null || !this.defaultSet) {
            return value;
        }
        return new Integer(this.defaultVal);
    }
}
