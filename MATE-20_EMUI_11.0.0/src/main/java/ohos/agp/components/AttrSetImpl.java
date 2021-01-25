package ohos.agp.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class AttrSetImpl implements AttrSet {
    private final List<Attr> mAttrList;
    private final Map<String, Attr> mAttrMap;
    private String mStyle;

    public AttrSetImpl() {
        this(null);
    }

    public AttrSetImpl(AttrSet attrSet) {
        this.mAttrList = new ArrayList();
        this.mAttrMap = new HashMap();
        this.mStyle = null;
        if (attrSet != null) {
            int length = attrSet.getLength();
            for (int i = 0; i < length; i++) {
                attrSet.getAttr(i).ifPresent(new Consumer() {
                    /* class ohos.agp.components.$$Lambda$AttrSetImpl$hqaYChFcpm7YjiwYueFnC6Wws */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        AttrSetImpl.this.lambda$new$0$AttrSetImpl((Attr) obj);
                    }
                });
            }
            this.mStyle = attrSet.getStyle().orElse(null);
        }
    }

    public /* synthetic */ void lambda$new$0$AttrSetImpl(Attr attr) {
        this.mAttrList.add(attr);
        this.mAttrMap.put(attr.getName(), attr);
    }

    public boolean addAttr(Attr attr) {
        this.mAttrMap.put(attr.getName(), attr);
        return this.mAttrList.add(attr);
    }

    public void setStyle(String str) {
        this.mStyle = str;
    }

    @Override // ohos.agp.components.AttrSet
    public Optional<String> getStyle() {
        return Optional.ofNullable(this.mStyle);
    }

    @Override // ohos.agp.components.AttrSet
    public int getLength() {
        return this.mAttrList.size();
    }

    @Override // ohos.agp.components.AttrSet
    public Optional<Attr> getAttr(int i) {
        int size = this.mAttrList.size();
        if (i < 0 || i >= size) {
            return Optional.empty();
        }
        return Optional.of(this.mAttrList.get(i));
    }

    @Override // ohos.agp.components.AttrSet
    public Optional<Attr> getAttr(String str) {
        return Optional.ofNullable(this.mAttrMap.getOrDefault(str, null));
    }

    public String toString() {
        return "attrs size: " + getLength() + "\nstyle: " + getStyle().orElse("no style") + "\n" + this.mAttrList;
    }
}
