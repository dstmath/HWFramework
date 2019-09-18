package android.arch.lifecycle;

public class MutableLiveData<T> extends LiveData<T> {
    public void postValue(T value) {
        super.postValue(value);
    }

    public void setValue(T value) {
        super.setValue(value);
    }
}
