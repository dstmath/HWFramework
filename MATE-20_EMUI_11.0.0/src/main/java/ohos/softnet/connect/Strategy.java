package ohos.softnet.connect;

public enum Strategy {
    BLE((byte) 1),
    P2P((byte) 2),
    USB((byte) 4),
    COAP((byte) 8),
    WIFI((byte) 16);
    
    private byte mStrategyValue;

    private Strategy(byte b) {
        this.mStrategyValue = b;
    }

    public byte getStrategyValue() {
        return this.mStrategyValue;
    }
}
