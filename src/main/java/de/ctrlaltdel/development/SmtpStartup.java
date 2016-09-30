package de.ctrlaltdel.development;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * SmtpStartup
 */
@WebListener
public class SmtpStartup implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(SmtpStartup.class);

    private SMTPServer smtpServer;

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        smtpServer = new SMTPServer(messageContext -> new LogMesssageHandler());
        smtpServer.setPort(Integer.parseInt(System.getProperty("mail.port")));
        smtpServer.start();
        LOG.info("SMTP Server started at {}", smtpServer.getPort());
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        smtpServer.stop();
    }

}
