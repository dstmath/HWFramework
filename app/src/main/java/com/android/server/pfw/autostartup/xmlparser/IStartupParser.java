package com.android.server.pfw.autostartup.xmlparser;

import org.w3c.dom.Element;

interface IStartupParser<T> {
    T parseDOMElement(Element element);
}
