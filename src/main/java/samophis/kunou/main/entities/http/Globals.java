package samophis.kunou.main.entities.http;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Controls the "global lock" to prevent other {@link Route Routes} from executing while under a global rate limit.
 *
 * @author SamOphis
 * @since 0.1
 */

public class Globals {
    private static final ScheduledExecutorService POOL = Executors.newSingleThreadScheduledExecutor();
    private static boolean isLocked = false;

    /**
     * Stops {@link Requester Requesters} from processing {@link InnerRequest InnerRequests} whatsoever for a certain amount of milliseconds.
     * @param time The amount of milliseconds to keep the lock closed.
     */
    public static synchronized void lockGlobal(int time) {
        isLocked = true;
        try {
            Thread.sleep(time);
            isLocked = false;
        } catch (InterruptedException exc) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exc);
        }
    }

    /**
     * Fetches whether or not the lock is currently open or closed.
     * <br><p>Although being self-explanatory, "open" means there is no global rate limit, while "closed" mean there is one.</p>
     * @return Whether the lock is currently open or closed.
     */
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
