package com.huawei.android.internal.os;

import android.os.Message;
import com.android.internal.os.SomeArgs;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SomeArgsEx {
    private SomeArgs args;

    public SomeArgsEx(Object obj) {
        if (obj instanceof SomeArgs) {
            this.args = (SomeArgs) obj;
        }
    }

    public static boolean isInstanceOfSomeArgs(Object object) {
        return object instanceof SomeArgs;
    }

    public void setArg1(Object obj) {
        this.args.arg1 = obj;
    }

    public void setArg2(Object obj) {
        this.args.arg2 = obj;
    }

    public void setArg3(Object obj) {
        this.args.arg3 = obj;
    }

    public Object arg1() {
        return this.args.arg1;
    }

    public Object arg2() {
        return this.args.arg2;
    }

    public Object arg3() {
        return this.args.arg3;
    }

    public static SomeArgsEx obtain() {
        return new SomeArgsEx(SomeArgs.obtain());
    }

    public void recycle() {
        this.args.recycle();
    }

    public int getArgi1() {
        return this.args.argi1;
    }

    public int getArgi2() {
        return this.args.argi2;
    }

    public int getArgi3() {
        return this.args.argi3;
    }

    public int getArgi4() {
        return this.args.argi4;
    }

    public void setArgi1(int argi1) {
        this.args.argi1 = argi1;
    }

    public void setArgi2(int argi2) {
        this.args.argi2 = argi2;
    }

    public void setArgi3(int argi3) {
        this.args.argi3 = argi3;
    }

    public void setArgi4(int argi4) {
        this.args.argi4 = argi4;
    }

    public void setMessageObj(SomeArgsEx argsEx, Message msg) {
        msg.obj = argsEx.args;
    }
}
