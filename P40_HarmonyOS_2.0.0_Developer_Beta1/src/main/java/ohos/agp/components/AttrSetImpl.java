package ohos.agp.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ohos.aafwk.utils.log.LogDomain;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AttrSetImpl implements AttrSet {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_AttrSet");
    private static final String THEME_TAG = "theme";
    private boolean hasStyleMerged;
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
        this.hasStyleMerged = false;
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
        mergeStyle();
        return this.mAttrList.size();
    }

    @Override // ohos.agp.components.AttrSet
    public Optional<Attr> getAttr(int i) {
        mergeStyle();
        int size = this.mAttrList.size();
        if (i < 0 || i >= size) {
            return Optional.empty();
        }
        return Optional.of(this.mAttrList.get(i));
    }

    @Override // ohos.agp.components.AttrSet
    public Optional<Attr> getAttr(String str) {
        mergeStyle();
        return Optional.ofNullable(this.mAttrMap.getOrDefault(str, null));
    }

    public String toString() {
        return "attrs size: " + getLength() + "\nstyle: " + getStyle().orElse("no style") + "\n" + this.mAttrList;
    }

    private void mergeStyle() {
        if (!this.hasStyleMerged) {
            this.hasStyleMerged = true;
            Attr attr = this.mAttrMap.get(THEME_TAG);
            if (attr instanceof AttrImpl) {
                AttrImpl attrImpl = (AttrImpl) attr;
                try {
                    HashMap patternHash = attrImpl.mTypeAttribute.getPatternValue().getPatternHash();
                    if (patternHash == null) {
                        return;
                    }
                    if (!patternHash.isEmpty()) {
                        patternHash.forEach(new BiConsumer(attrImpl) {
                            /* class ohos.agp.components.$$Lambda$AttrSetImpl$rxPWNVViFYfPoqG3cMVlcF46FME */
                            private final /* synthetic */ AttrImpl f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.util.function.BiConsumer
                            public final void accept(Object obj, Object obj2) {
                                AttrSetImpl.this.lambda$mergeStyle$1$AttrSetImpl(this.f$1, (String) obj, (TypedAttribute) obj2);
                            }
                        });
                    }
                } catch (IOException | NotExistException | WrongTypeException e) {
                    HiLog.error(TAG, "Merge style failed: %{public}s", new Object[]{e.getMessage()});
                }
            }
        }
    }

    public /* synthetic */ void lambda$mergeStyle$1$AttrSetImpl(AttrImpl attrImpl, String str, TypedAttribute typedAttribute) {
        if (str != null && typedAttribute != null && this.mAttrMap.get(str) == null) {
            addAttr(new AttrImpl(str, typedAttribute, attrImpl.mContext));
        }
    }
}
