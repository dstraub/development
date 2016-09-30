package de.ctrlaltdel.development;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;

/**
 * MailTest
 */
@WebServlet(urlPatterns = "/mail")
public class MailTest extends HttpServlet {

    @Resource(lookup = "java:jboss/mail/Default")
    private Session session;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Message message = new MimeMessage(session);
            message.setSentDate(new Date());
            message.setSubject("test");
            message.setText("test mail");
            message.setRecipients(MimeMessage.RecipientType.TO,
                    new Address[] {new InternetAddress("test@development.local", false)} );

            Transport.send(message);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());

        }
    }
}