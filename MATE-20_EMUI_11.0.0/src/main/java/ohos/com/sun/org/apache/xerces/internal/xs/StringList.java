package ohos.com.sun.org.apache.xerces.internal.xs;

import java.util.List;

public interface StringList extends List {
    boolean contains(String str);

    int getLength();

    String item(int i);
}
