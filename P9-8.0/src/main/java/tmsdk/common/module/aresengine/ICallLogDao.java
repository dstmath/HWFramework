package tmsdk.common.module.aresengine;

public interface ICallLogDao<T extends CallLogEntity> {
    long insert(T t, FilterResult filterResult);
}
