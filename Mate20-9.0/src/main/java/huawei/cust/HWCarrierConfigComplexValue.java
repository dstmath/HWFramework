package huawei.cust;

public abstract class HWCarrierConfigComplexValue<T> {
    private T mData;

    public abstract void addData(String str, Object obj);

    public HWCarrierConfigComplexValue(T data) {
        this.mData = data;
    }

    public T getData() {
        return this.mData;
    }
}
