package com.android.server.timezone;

interface RulesManagerIntentHelper {
    void sendTimeZoneOperationStaged();

    void sendTimeZoneOperationUnstaged();
}
