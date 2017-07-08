package org.w3c.dom;

public interface Notation extends Node {
    String getPublicId();

    String getSystemId();
}
