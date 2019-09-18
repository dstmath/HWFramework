package com.android.org.conscrypt;

interface SessionDecorator extends ConscryptSession {
    ConscryptSession getDelegate();
}
