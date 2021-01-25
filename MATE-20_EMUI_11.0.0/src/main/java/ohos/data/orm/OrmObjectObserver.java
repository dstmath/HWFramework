package ohos.data.orm;

public interface OrmObjectObserver {
    void onChange(OrmContext ormContext, AllChangeToTarget allChangeToTarget);
}
