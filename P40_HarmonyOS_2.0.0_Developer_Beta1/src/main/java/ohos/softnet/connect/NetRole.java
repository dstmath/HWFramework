package ohos.softnet.connect;

public enum NetRole {
    P2P_GO(1),
    P2P_GC(2),
    HOTSPOT_AP(3),
    HOTSPOT_STA(4);
    
    private int mNetRole;

    private NetRole(int i) {
        this.mNetRole = i;
    }

    public int getNetRole() {
        return this.mNetRole;
    }
}
