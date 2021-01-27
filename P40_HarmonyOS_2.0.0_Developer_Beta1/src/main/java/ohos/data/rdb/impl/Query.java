package ohos.data.rdb.impl;

public interface Query {
    PrecompiledStatement beginStepQuery();

    void endStepQuery(PrecompiledStatement precompiledStatement);

    void resetStatement(PrecompiledStatement precompiledStatement);

    int step();
}
