package com.android.commands.uiautomator;

import android.app.UiAutomation;
import android.view.accessibility.AccessibilityEvent;
import com.android.commands.uiautomator.Launcher;
import com.android.uiautomator.core.UiAutomationShellWrapper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventsCommand extends Launcher.Command {
    private Object mQuitLock = new Object();

    public EventsCommand() {
        super("events");
    }

    @Override // com.android.commands.uiautomator.Launcher.Command
    public String shortHelp() {
        return "prints out accessibility events until terminated";
    }

    @Override // com.android.commands.uiautomator.Launcher.Command
    public String detailedOptions() {
        return null;
    }

    @Override // com.android.commands.uiautomator.Launcher.Command
    public void run(String[] args) {
        UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
        automationWrapper.connect();
        automationWrapper.getUiAutomation().setOnAccessibilityEventListener(new UiAutomation.OnAccessibilityEventListener() {
            /* class com.android.commands.uiautomator.EventsCommand.AnonymousClass1 */

            @Override // android.app.UiAutomation.OnAccessibilityEventListener
            public void onAccessibilityEvent(AccessibilityEvent event) {
                System.out.println(String.format("%s %s", new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date()), event.toString()));
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
    }
}
