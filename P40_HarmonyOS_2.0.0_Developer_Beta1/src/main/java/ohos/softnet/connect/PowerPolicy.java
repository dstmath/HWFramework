package ohos.softnet.connect;

public enum PowerPolicy {
    High(1),
    Middle(2),
    Low(3),
    Very_Low(4);
    
    private int mPowerPolicyValue;

    private PowerPolicy(int i) {
        this.mPowerPolicyValue = i;
    }

    public int getPowerPolicyValue() {
        return this.mPowerPolicyValue;
    }
}
