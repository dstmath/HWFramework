package ohos.com.sun.org.apache.xerces.internal.xs;

import java.util.List;
import ohos.org.w3c.dom.ls.LSInput;

public interface LSInputList extends List {
    int getLength();

    LSInput item(int i);
}
