package android.service.euicc;

public interface IHwEuiccServiceEx {
    void cancelSession();

    void requestDefaultSmdpAddress(String str, IHwGetSmdsAddressCallback iHwGetSmdsAddressCallback);

    void resetMemory(String str, int i, IHwResetMemoryCallback iHwResetMemoryCallback);

    void setDefaultSmdpAddress(String str, String str2, IHwSetDefaultSmdpAddressCallback iHwSetDefaultSmdpAddressCallback);
}
