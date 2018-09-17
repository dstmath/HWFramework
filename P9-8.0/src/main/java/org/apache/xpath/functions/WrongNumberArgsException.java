package org.apache.xpath.functions;

public class WrongNumberArgsException extends Exception {
    static final long serialVersionUID = -4551577097576242432L;

    public WrongNumberArgsException(String argsExpected) {
        super(argsExpected);
    }
}
