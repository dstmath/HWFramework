package com.huawei.ace.plugin;

import java.util.List;

public final class Function {
    public final List<Object> arguments;
    public final String name;

    public Function(String str, List<Object> list) {
        this.name = str;
        this.arguments = list;
    }
}
