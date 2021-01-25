package com.huawei.internal.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class StateMachineEx {
    public static final boolean HANDLED = true;
    public static final boolean NOT_HANDLED = false;
    private StateMachineBridge mStateMachine;

    protected StateMachineEx(String name) {
        initStateMachine(name);
    }

    protected StateMachineEx(String name, Looper looper) {
        initStateMachine(name, looper);
    }

    private void initStateMachine(String name) {
        this.mStateMachine = new StateMachineBridge(name);
        this.mStateMachine.setStateMachineEx(this);
    }

    private void initStateMachine(String name, Looper looper) {
        this.mStateMachine = new StateMachineBridge(name, looper);
        this.mStateMachine.setStateMachineEx(this);
    }

    public final void addState(StateEx state) {
        this.mStateMachine.addState(state.getState());
    }

    public final void addState(StateEx state, StateEx parent) {
        this.mStateMachine.addState(state.getState(), parent.getState());
    }

    public void setInitialState(StateEx initialState) {
        this.mStateMachine.setInitialState(initialState.getState());
    }

    public void transitionTo(StateEx destState) {
        this.mStateMachine.transitionTo(destState.getState());
    }

    public String getWhatToString(int what) {
        return null;
    }

    public void unhandledMessage(Message msg) {
    }

    public void start() {
        this.mStateMachine.start();
    }

    public final void quit() {
        this.mStateMachine.quit();
    }

    public final Handler getHandler() {
        return this.mStateMachine.getHandler();
    }

    public final String getName() {
        return this.mStateMachine.getName();
    }

    public final Message obtainMessage() {
        return this.mStateMachine.obtainMessage();
    }

    public final Message obtainMessage(int what) {
        return this.mStateMachine.obtainMessage(what);
    }

    public final Message obtainMessage(int what, Object obj) {
        return this.mStateMachine.obtainMessage(what, obj);
    }

    public final Message obtainMessage(int what, int arg1) {
        return this.mStateMachine.obtainMessage(what, arg1);
    }

    public final Message obtainMessage(int what, int arg1, int arg2) {
        return this.mStateMachine.obtainMessage(what, arg1, arg2);
    }

    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        return this.mStateMachine.obtainMessage(what, arg1, arg2, obj);
    }

    public final void deferMessage(Message msg) {
        this.mStateMachine.deferMessage(msg);
    }

    public void sendMessage(int what) {
        this.mStateMachine.sendMessage(what);
    }

    public void sendMessage(int what, Object obj) {
        this.mStateMachine.sendMessage(what, obj);
    }

    public void sendMessage(int what, int arg1) {
        this.mStateMachine.sendMessage(what, arg1);
    }

    public void sendMessage(int what, int arg1, int arg2) {
        this.mStateMachine.sendMessage(what, arg1, arg2);
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        this.mStateMachine.sendMessage(what, arg1, arg2, obj);
    }

    public void sendMessage(Message msg) {
        this.mStateMachine.sendMessage(msg);
    }

    public void sendMessageDelayed(int what, long delayMillis) {
        this.mStateMachine.sendMessageDelayed(what, delayMillis);
    }

    public void sendMessageDelayed(int what, Object obj, long delayMillis) {
        this.mStateMachine.sendMessageDelayed(what, obj, delayMillis);
    }

    public void sendMessageDelayed(int what, int arg1, long delayMillis) {
        this.mStateMachine.sendMessageDelayed(what, arg1, delayMillis);
    }

    public void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
        this.mStateMachine.sendMessageDelayed(what, arg1, arg2, delayMillis);
    }

    public void sendMessageDelayed(int what, int arg1, int arg2, Object obj, long delayMillis) {
        this.mStateMachine.sendMessageDelayed(what, arg1, arg2, obj, delayMillis);
    }

    public void sendMessageDelayed(Message msg, long delayMillis) {
        this.mStateMachine.sendMessageDelayed(msg, delayMillis);
    }

    public final boolean isInSpecificState(StateEx stateEx) {
        return this.mStateMachine.getCurrentState() == stateEx.getState();
    }

    public final boolean hasMessages(int what) {
        return this.mStateMachine.hasMessagesHw(what);
    }

    public final void removeMessages(int what) {
        this.mStateMachine.removeMessagesHw(what);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mStateMachine.dump(fd, pw, args);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
    }
}
