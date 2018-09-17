package tmsdk.common.module.aresengine;

import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public interface IKeyWordDao {
    boolean contains(String str);

    ArrayList<String> getAll();

    void setAll(List<String> list);
}
