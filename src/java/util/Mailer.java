// Utilidad simple
package util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class Mailer {
  public static void send(String host, int port, final String user, final String pass,
                          String to, String subject, String textBody, byte[] pdf, String fileName)
      throws Exception {
    Properties p = new Properties();
    p.put("mail.smtp.auth", "true");
    p.put("mail.smtp.starttls.enable", "true");
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.port", String.valueOf(port));

    Session s = Session.getInstance(p, new Authenticator(){
      @Override protected PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(user, pass);
      }
    });

    MimeBodyPart text = new MimeBodyPart();
    text.setText(textBody, "UTF-8");

    MimeBodyPart attach = new MimeBodyPart();
    attach.setFileName(fileName);
    attach.setContent(pdf, "application/pdf");

    Multipart mp = new MimeMultipart();
    mp.addBodyPart(text);
    mp.addBodyPart(attach);

    Message m = new MimeMessage(s);
    m.setFrom(new InternetAddress(user));
    m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    m.setSubject(subject);
    m.setContent(mp);

    Transport.send(m);
  }
}