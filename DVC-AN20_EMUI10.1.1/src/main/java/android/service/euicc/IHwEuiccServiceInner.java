package android.service.euicc;

import java.util.concurrent.ThreadPoolExecutor;

public interface IHwEuiccServiceInner {
    ThreadPoolExecutor getExecutor();
}
