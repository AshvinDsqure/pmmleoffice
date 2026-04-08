package org.dspace.emas;

import org.dspace.core.Context;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SequenceServiceImpl {

    @Scheduled(cron = "0 0 0 1 4 *", zone = "Asia/Kolkata")
   //@Scheduled(cron = "0 25 21 31 3 *", zone = "Asia/Kolkata")
    public void resetSequence() {

        Context context = null;

        try {
            // ✅ Create context manually
            context = new Context();
            context.turnOffAuthorisationSystem();

            Session session =(Session) context.getDBConnection().getSession();

            // ✅ Reset sequences
            session.createNativeQuery("ALTER SEQUENCE file_seq RESTART WITH 1")
                    .executeUpdate();

            session.createNativeQuery("ALTER SEQUENCE inward_seq RESTART WITH 1")
                    .executeUpdate();

            // ✅ Commit
            context.complete();

            System.out.println("Sequences reset successfully");

        } catch (Exception e) {
            if (context != null) {
                context.abort();
            }
            e.printStackTrace();
        }
    }
}