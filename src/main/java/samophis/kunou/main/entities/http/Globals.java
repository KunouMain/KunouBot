package samophis.kunou.main.entities.http;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Globals {
    private static final ScheduledExecutorService POOL = Executors.newSingleThreadScheduledExecutor();
    private static boolean isLocked = false;
    public static synchronized void setLocked(boolean isLocked, int time) {
        if (Globals.isLocked == isLocked)
            return;
        Globals.isLocked = isLocked;
        POOL.schedule(() -> Globals.isLocked = false, time, TimeUnit.MILLISECONDS);
    }
    public static boolean isLocked() {
        return isLocked;
    }
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!POOL.isShutdown())
                POOL.shutdownNow();
        }));
    }
}
