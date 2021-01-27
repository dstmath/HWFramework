package com.android.server.wifi.util;

public class GeneralUtil {

    public static final class Mutable<E> {
        public E value;

        public Mutable() {
            this.value = null;
        }

        public Mutable(E value2) {
            this.value = value2;
        }
    }
}
