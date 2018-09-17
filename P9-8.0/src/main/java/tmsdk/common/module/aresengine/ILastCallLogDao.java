package tmsdk.common.module.aresengine;

public interface ILastCallLogDao {
    boolean contains(String str);

    void update(CallLogEntity callLogEntity);
}
