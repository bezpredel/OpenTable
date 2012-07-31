package com.bezpredel.opentable;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
* Date: 7/30/12
* Time: 2:16 PM
*/
public class Mailer {
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String from;
    private final String destinations;

    public Mailer(String host, String port, String username, String password, String from, String destinations) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.from = from;
        this.destinations = destinations;
    }

    public void sendMail(String subject, String body) throws Exception {
        Session session = createMailSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinations));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    public void test() throws Exception {
        Session session = createMailSession();

        Transport transport = session.getTransport();
        transport.connect();
        transport.close();
    }

    private Properties createProperties() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);


        return props;
    }

    private Session createMailSession() {
        Session session = Session.getInstance(
                createProperties(),
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(username, password);
                    }
                }
        );

        return session;
    }
}
