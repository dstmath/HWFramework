package org.apache.xml.dtm;

import javax.xml.transform.SourceLocator;

public class DTMConfigurationException extends DTMException {
    static final long serialVersionUID = -4607874078818418046L;

    public DTMConfigurationException() {
        super("Configuration Error");
    }

    public DTMConfigurationException(String msg) {
        super(msg);
    }

    public DTMConfigurationException(Throwable e) {
        super(e);
    }

    public DTMConfigurationException(String msg, Throwable e) {
        super(msg, e);
    }

    public DTMConfigurationException(String message, SourceLocator locator) {
        super(message, locator);
    }

    public DTMConfigurationException(String message, SourceLocator locator, Throwable e) {
        super(message, locator, e);
    }
}
