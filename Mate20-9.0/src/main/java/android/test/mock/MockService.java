package android.test.mock;

import android.app.Application;
import android.app.Service;
import android.content.Context;

@Deprecated
public class MockService {
    public static <T extends Service> void attachForTesting(Service service, Context context, String serviceClassName, Application application) {
        service.attach(context, null, serviceClassName, null, application, null);
    }

    private MockService() {
    }
}
