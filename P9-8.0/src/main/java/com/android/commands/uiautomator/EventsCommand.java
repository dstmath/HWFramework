package com.android.commands.uiautomator;

import android.app.UiAutomation.OnAccessibilityEventListener;
import android.view.accessibility.AccessibilityEvent;
import com.android.commands.uiautomator.Launcher.Command;
import com.android.uiautomator.core.UiAutomationShellWrapper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventsCommand extends Command {
    private Object mQuitLock = new Object();

    public EventsCommand() {
        super("events");
    }

    public String shortHelp() {
        return "prints out accessibility events until terminated";
    }

    public String detailedOptions() {
        return null;
    }

    public void run(String[] args) {
        UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
        automationWrapper.connect();
        automationWrapper.getUiAutomation().setOnAccessibilityEventListener(new OnAccessibilityEventListener() {
            public void onAccessibilityEvent(AccessibilityEvent event) {
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
                System.out.println(String.format("%s %s", new Object[]{formatter.format(new Date()), event.toString()}));
            }
        });
        synchronized (this.mQuitLock) {
            try {
                this.mQuitLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        automationWrapper.disconnect();
        return;
    }
}
