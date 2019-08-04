package IAM;

import core.service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class sessionManager {

    private static Logger logger = LogManager.getLogger(service.class);

    public static boolean validateSession(String userPhone, String sessionID) throws Exception {
        AtomicBoolean authorzie = new AtomicBoolean(false);
        try {
            //fixme sha1 session ID required.
            // check sessionID
            core.cache.sessions.asMap().forEach((k, v) -> {
                if (k.equals(userPhone) && v.equals(sessionID)) {
                    authorzie.set(true);
                    logger.info("session is active. sessionID {}", sessionID);
                } else {
                    logger.error("session is not active. sessionID {}", sessionID);
                    authorzie.set(false);
                }
            });
            if (!authorzie.get() == true) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
