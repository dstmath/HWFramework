package org.w3c.dom;

public interface DOMConfiguration {
    boolean canSetParameter(String str, Object obj);

    Object getParameter(String str) throws DOMException;

    DOMStringList getParameterNames();

    void setParameter(String str, Object obj) throws DOMException;
}
