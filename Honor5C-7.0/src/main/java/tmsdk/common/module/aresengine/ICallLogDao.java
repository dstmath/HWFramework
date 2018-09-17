package tmsdk.common.module.aresengine;

/* compiled from: Unknown */
public interface ICallLogDao<T extends CallLogEntity> {
    long insert(T t, FilterResult filterResult);
}
