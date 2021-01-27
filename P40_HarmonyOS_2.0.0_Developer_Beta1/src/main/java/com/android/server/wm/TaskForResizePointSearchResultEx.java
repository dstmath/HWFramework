package com.android.server.wm;

import com.android.server.wm.DisplayContent;

public class TaskForResizePointSearchResultEx {
    protected DisplayContent.TaskForResizePointSearchResult mResult;

    public TaskForResizePointSearchResultEx(DisplayContent.TaskForResizePointSearchResult result) {
        this.mResult = result;
    }

    public TaskForResizePointSearchResultEx() {
    }

    public void setTaskForResizePointSearchResult(DisplayContent.TaskForResizePointSearchResult result) {
        this.mResult = result;
    }

    public DisplayContent.TaskForResizePointSearchResult getResult() {
        return this.mResult;
    }

    public void setSearchDone(boolean isSearchDone) {
        this.mResult.searchDone = isSearchDone;
    }

    public void setTaskForResize(TaskEx task) {
        this.mResult.taskForResize = task.getTask();
    }
}
