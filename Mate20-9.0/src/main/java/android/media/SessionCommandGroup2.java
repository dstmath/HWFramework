package android.media;

import android.media.update.ApiLoader;
import android.media.update.MediaSession2Provider;
import android.os.Bundle;
import java.util.Set;

public final class SessionCommandGroup2 {
    private final MediaSession2Provider.CommandGroupProvider mProvider;

    public SessionCommandGroup2() {
        this.mProvider = ApiLoader.getProvider().createMediaSession2CommandGroup(this, null);
    }

    public SessionCommandGroup2(SessionCommandGroup2 others) {
        this.mProvider = ApiLoader.getProvider().createMediaSession2CommandGroup(this, others);
    }

    public SessionCommandGroup2(MediaSession2Provider.CommandGroupProvider provider) {
        this.mProvider = provider;
    }

    public void addCommand(SessionCommand2 command) {
        this.mProvider.addCommand_impl(command);
    }

    public void addCommand(int commandCode) {
    }

    public void addAllPredefinedCommands() {
        this.mProvider.addAllPredefinedCommands_impl();
    }

    public void removeCommand(SessionCommand2 command) {
        this.mProvider.removeCommand_impl(command);
    }

    public void removeCommand(int commandCode) {
    }

    public boolean hasCommand(SessionCommand2 command) {
        return this.mProvider.hasCommand_impl(command);
    }

    public boolean hasCommand(int code) {
        return this.mProvider.hasCommand_impl(code);
    }

    public Set<SessionCommand2> getCommands() {
        return this.mProvider.getCommands_impl();
    }

    public MediaSession2Provider.CommandGroupProvider getProvider() {
        return this.mProvider;
    }

    public Bundle toBundle() {
        return this.mProvider.toBundle_impl();
    }

    public static SessionCommandGroup2 fromBundle(Bundle commands) {
        return ApiLoader.getProvider().fromBundle_MediaSession2CommandGroup(commands);
    }
}
