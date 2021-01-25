package ohos.aafwk.ability;

import java.io.File;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.locale.LanguageTag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UIContent {
    private static final int INVALID_LAYOUT_RES_ID = -1;
    private static final LogLabel LABEL = LogLabel.create();
    private static final String PREFIX = "    ";
    volatile ComponentContainer curViewGroup;
    private boolean latestUIAttached = false;
    private int layoutResId = -1;
    ComponentContainer preViewGroup;
    private boolean uiAttachedAllowed = true;
    private boolean uiAttachedDisable = false;
    private UIContentType uiContentType = UIContentType.TYPE_UNKNOWN;
    private AbilityWindow windowProxy;

    /* access modifiers changed from: private */
    public enum UIContentType {
        TYPE_UNKNOWN,
        TYPE_LAYOUT_RES_ID,
        TYPE_VIEWGROUP_OBJECT
    }

    UIContent(AbilityWindow abilityWindow) {
        if (abilityWindow != null) {
            this.windowProxy = abilityWindow;
            return;
        }
        throw new IllegalArgumentException("input parameter window is null");
    }

    /* access modifiers changed from: package-private */
    public synchronized void updateUIContent(ComponentContainer componentContainer) {
        if (this.uiContentType != UIContentType.TYPE_VIEWGROUP_OBJECT || !this.curViewGroup.equals(componentContainer)) {
            this.uiContentType = UIContentType.TYPE_VIEWGROUP_OBJECT;
            this.preViewGroup = this.curViewGroup;
            this.curViewGroup = componentContainer;
            attachToWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void updateUIContent(int i) {
        if (this.uiContentType == UIContentType.TYPE_LAYOUT_RES_ID && this.layoutResId == i) {
            Log.error(LABEL, "same UI set, ignore", new Object[0]);
            return;
        }
        this.uiContentType = UIContentType.TYPE_LAYOUT_RES_ID;
        this.layoutResId = i;
        attachToWindow();
    }

    /* access modifiers changed from: package-private */
    public synchronized Component findComponentById(int i) {
        return this.windowProxy.findComponentById(i);
    }

    /* access modifiers changed from: package-private */
    public void ensureLatestUIAttached() {
        if (!isLatestUIAttached()) {
            applyLatestUI();
        } else if (Log.isDebuggable()) {
            Log.debug(LABEL, "latest UI has been applied already", new Object[0]);
        }
    }

    private void attachToWindow() {
        setLatestUIAttachedFlag(false);
        applyLatestUI();
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean isLatestUIAttached() {
        return this.latestUIAttached;
    }

    /* access modifiers changed from: package-private */
    public synchronized void setLatestUIAttachedFlag(boolean z) {
        this.latestUIAttached = z;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean isUiAttachedAllowed() {
        return this.uiAttachedAllowed;
    }

    /* access modifiers changed from: package-private */
    public synchronized void setUiAttachedAllowed(boolean z) {
        this.uiAttachedAllowed = z;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean isUiAttachedDisable() {
        return this.uiAttachedDisable;
    }

    /* access modifiers changed from: package-private */
    public synchronized void setUiAttachedDisable(boolean z) {
        this.uiAttachedDisable = z;
    }

    private void applyLatestUI() {
        if (!isUiAttachedAllowed() || isUiAttachedDisable()) {
            Log.warn(LABEL, "attach UI not allowed for now, will be applied when foreground", new Object[0]);
            dumpUIContent();
            return;
        }
        setLatestUIAttachedFlag(attachUI());
        dumpUIContent();
    }

    private synchronized boolean attachUI() {
        if (this.windowProxy == null) {
            Log.error(LABEL, "can not set UI to window, fatal error", new Object[0]);
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$ohos$aafwk$ability$UIContent$UIContentType[this.uiContentType.ordinal()];
        if (i == 1) {
            this.windowProxy.setUIContent(this.curViewGroup);
            return true;
        } else if (i == 2) {
            this.windowProxy.setUIContent(this.layoutResId);
            this.preViewGroup = this.curViewGroup;
            this.curViewGroup = this.windowProxy.getCurrentAttachedUI();
            if (this.curViewGroup == null) {
                Log.error(LABEL, "NOTE: null viewGroup got back from window when set layout res", new Object[0]);
            } else if (this.curViewGroup.equals(this.preViewGroup)) {
                Log.warn(LABEL, "NOTE: wrong viewGroup maybe got back from window when set layout res", new Object[0]);
            }
            return true;
        } else if (i != 3) {
            Log.error(LABEL, "missing handling for UI content type enum", new Object[0]);
            return false;
        } else {
            Log.error(LABEL, "unknown UI content type", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.aafwk.ability.UIContent$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$aafwk$ability$UIContent$UIContentType = new int[UIContentType.values().length];

        static {
            try {
                $SwitchMap$ohos$aafwk$ability$UIContent$UIContentType[UIContentType.TYPE_VIEWGROUP_OBJECT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$UIContent$UIContentType[UIContentType.TYPE_LAYOUT_RES_ID.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$UIContent$UIContentType[UIContentType.TYPE_UNKNOWN.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public synchronized void reset() {
        this.curViewGroup = null;
        this.preViewGroup = null;
        this.layoutResId = -1;
        this.uiContentType = UIContentType.TYPE_UNKNOWN;
        setLatestUIAttachedFlag(false);
        setUiAttachedAllowed(true);
        setUiAttachedDisable(false);
    }

    private void dumpUIContent() {
        if (Log.isDebuggable()) {
            Log.debug(LABEL, "latest UI result: uiContent@%{public}d [%{public}s]", Integer.valueOf(hashCode()), toString());
        }
    }

    public String toString() {
        return " (uiAttachedAllowed: " + this.uiAttachedAllowed + ", uiAttachedDisable: " + this.uiAttachedDisable + ", latestUIAttached: " + this.latestUIAttached + ", uiContentType: " + this.uiContentType + ", preViewGroup = " + this.preViewGroup + ", curViewGroup = " + this.curViewGroup + ", layoutResId = " + this.layoutResId + ")";
    }

    /* access modifiers changed from: package-private */
    public synchronized void dump(String str, PrintWriter printWriter, String str2) {
        printWriter.print(str);
        printWriter.println("uiContentType: " + this.uiContentType);
        if (this.uiContentType == UIContentType.TYPE_LAYOUT_RES_ID) {
            printWriter.print(str);
            printWriter.println("layoutResId: " + this.layoutResId);
        } else if (this.uiContentType != UIContentType.TYPE_VIEWGROUP_OBJECT) {
            printWriter.print(str);
            printWriter.println("unknown uiContentType");
        } else if (!str2.isEmpty()) {
            if (!dumpToXml(this.curViewGroup, str2)) {
                printWriter.println("dump layout info to \"" + str2 + "\" failed.");
            }
        } else {
            printWriter.print(str);
            printWriter.println("<ComponentContainer>");
            dumpViewGroup(str + "    ", printWriter, this.curViewGroup);
        }
    }

    private void dumpViewGroup(String str, PrintWriter printWriter, ComponentContainer componentContainer) {
        int childCount = componentContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Component componentAt = componentContainer.getComponentAt(i);
            if (componentAt instanceof ComponentContainer) {
                printWriter.print(str);
                String str2 = i == childCount - 1 ? str + "    " : str + "|    ";
                printWriter.println("|-- <Components>");
                dumpViewGroup(str2, printWriter, (ComponentContainer) componentAt);
            } else {
                printWriter.print(str);
                Class<?> cls = componentAt.getClass();
                printWriter.print("|-- [" + cls.getSimpleName());
                Class<? super Object> superclass = cls.getSuperclass();
                while (superclass != Component.class && superclass != Object.class) {
                    printWriter.print(LanguageTag.SEP + superclass.getSimpleName());
                    superclass = superclass.getSuperclass();
                }
                printWriter.print("] Position: [" + componentAt.getLeft() + ", " + componentAt.getTop());
                printWriter.println("] [Width, Height]: [" + componentAt.getWidth() + ", " + componentAt.getHeight() + "]");
            }
        }
    }

    private void buildElement(Document document, Element element, ComponentContainer componentContainer) {
        int childCount = componentContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Component componentAt = componentContainer.getComponentAt(i);
            if (componentAt instanceof ComponentContainer) {
                Element createElement = document.createElement("Views");
                buildElement(document, createElement, (ComponentContainer) componentAt);
                element.appendChild(createElement);
            } else {
                Class<?> cls = componentAt.getClass();
                StringBuilder sb = new StringBuilder(cls.getSimpleName());
                Class<? super Object> superclass = cls.getSuperclass();
                while (superclass != Component.class && superclass != Object.class) {
                    sb.append(LanguageTag.SEP);
                    sb.append(superclass.getSimpleName());
                    superclass = superclass.getSuperclass();
                }
                Element createElement2 = document.createElement("View");
                createElement2.setAttribute("Type", sb.toString());
                createElement2.setAttribute("PositionX", String.valueOf(componentAt.getLeft()));
                createElement2.setAttribute("PositionY", String.valueOf(componentAt.getTop()));
                createElement2.setAttribute("Width", String.valueOf(componentAt.getWidth()));
                createElement2.setAttribute("Height", String.valueOf(componentAt.getHeight()));
                element.appendChild(createElement2);
            }
        }
    }

    private boolean dumpToXml(ComponentContainer componentContainer, String str) {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        try {
            newInstance.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException unused) {
            Log.error(LABEL, "error set feature, ignore", new Object[0]);
        }
        try {
            Document newDocument = newInstance.newDocumentBuilder().newDocument();
            Element createElement = newDocument.createElement("ViewGroup");
            buildElement(newDocument, createElement, componentContainer);
            newDocument.appendChild(createElement);
            TransformerFactory newInstance2 = TransformerFactory.newInstance();
            try {
                newInstance2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                Transformer newTransformer = newInstance2.newTransformer();
                newTransformer.setOutputProperty(Constants.ATTRNAME_OUTPUT_INDENT, "yes");
                newTransformer.transform(new DOMSource(newDocument), new StreamResult(new File(str)));
                return true;
            } catch (TransformerException e) {
                Log.error(LABEL, "dump layout info to xml failed: %{public}s", e);
                return false;
            }
        } catch (ParserConfigurationException e2) {
            Log.error(LABEL, "dump layout info to xml failed: %{public}s", e2);
            return false;
        }
    }
}
