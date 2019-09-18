package huawei.cust;

import java.util.Map;

public interface IHwCarrierConfigPolicy {
    Map getFileConfig(String str);

    Map getFileConfig(String str, int i);

    String getOpKey();

    String getOpKey(int i);

    <T> T getValue(String str, int i, Class<T> cls);

    <T> T getValue(String str, Class<T> cls);
}
