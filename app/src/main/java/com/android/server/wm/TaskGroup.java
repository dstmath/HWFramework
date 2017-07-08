package com.android.server.wm;

import android.view.IApplicationToken;
import java.util.ArrayList;

public class TaskGroup {
    public int taskId;
    public ArrayList<IApplicationToken> tokens;

    public TaskGroup() {
        this.taskId = -1;
        this.tokens = new ArrayList();
    }

    public String toString() {
        return "id=" + this.taskId + " tokens=" + this.tokens;
    }
}
