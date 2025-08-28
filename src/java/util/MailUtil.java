package util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtil {

    /**
     * Envía un correo usando cualquier servidor SMTP.
     * 
     * @param host Servidor SMTP (ej: "smtp.gmail.com")
     * @param port Puerto SMTP (ej: 587 para TLS, 465 para SSL)
     * @param user Usuario/correo remitente
     * @param pass Contraseña o token de aplicación
     * @param to   Correo del destinatario
     * @param subject Asunto del correo
     * @param body Contenido del mensaje
     */
    public static void enviar(String host, int port, String user, String pass, 
                              String to, String subject, String body) throws MessagingException {
        
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS

        // Si usas puerto 465 (SSL) activa esto:
        if (port == 465) {
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        // Sesión autenticada
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        // Crear mensaje
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        // Enviar
        Transport.send(message);
        System.out.println("✅ Correo enviado a " + to);
    }
}