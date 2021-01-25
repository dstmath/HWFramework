package ohos.com.sun.org.apache.xerces.internal.xs;

public interface PSVIProvider {
    AttributePSVI getAttributePSVI(int i);

    AttributePSVI getAttributePSVIByName(String str, String str2);

    ElementPSVI getElementPSVI();
}
