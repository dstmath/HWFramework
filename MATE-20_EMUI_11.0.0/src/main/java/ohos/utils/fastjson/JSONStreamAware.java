package ohos.utils.fastjson;

import java.io.IOException;

public interface JSONStreamAware {
    void writeJSONString(Appendable appendable) throws IOException;
}
