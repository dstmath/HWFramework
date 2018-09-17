package tmsdk.bg.module.network;

public interface NetDataEntityFactory {
    NetDataEntity getNetDataEntity();

    void networkConnectivityChangeNotify();
}
