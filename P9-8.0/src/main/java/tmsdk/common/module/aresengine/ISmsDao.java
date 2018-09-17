package tmsdk.common.module.aresengine;

public interface ISmsDao<T extends SmsEntity> {
    long insert(T t, FilterResult filterResult);
}
