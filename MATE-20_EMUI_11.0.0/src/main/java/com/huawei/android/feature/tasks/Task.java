package com.huawei.android.feature.tasks;

import java.util.concurrent.Executor;

public abstract class Task<TResult> {
    public abstract Task<TResult> addOnCompleteListener(OnCompleteListener<TResult> onCompleteListener);

    public abstract Task<TResult> addOnCompleteListener(Executor executor, OnCompleteListener<TResult> onCompleteListener);

    public abstract Task<TResult> addOnFailureListener(OnFailureListener onFailureListener);

    public abstract Task<TResult> addOnFailureListener(Executor executor, OnFailureListener onFailureListener);

    public abstract Task<TResult> addOnSuccessListener(OnSuccessListener<? super TResult> onSuccessListener);

    public abstract Task<TResult> addOnSuccessListener(Executor executor, OnSuccessListener<? super TResult> onSuccessListener);

    public abstract Exception getException();

    public abstract TResult getResult();

    public abstract boolean isComplete();

    public abstract boolean isSuccessful();
}
