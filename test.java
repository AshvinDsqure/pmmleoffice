import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class test {

    public static void main(String[] args) {
        // Set up the mail server properties
        String host = "imap.gmail.com"; // For Gmail, use IMAP
        String user = "your-email@gmail.com"; // Your email
        String password = "your-email-password"; // Your email password or app-specific password

        // Create properties object to connect to Gmail IMAP server
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");

        // Create a session with the specified properties
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Connect to the mail server

            System.out.println("Emails from indeed.com deleted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
