package android.inputmethodservice;

public interface IHwInputMethodService {
    boolean handleImeDockDestroy();

    boolean updateImeDockConfiguration(boolean z);

    boolean updateImeDockVisibility(boolean z);
}
