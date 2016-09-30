package de.ctrlaltdel.development;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.net.URI;

/**
 * ActivemqStartup
 */
@WebListener
public class ActivemqStartup implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActivemqStartup.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String activemqURL = System.getProperty("activemq.url");
        if (activemqURL.startsWith("vm://")) {
            // nothing to start with in memory-broker
            return;
        }

        try {
            BrokerService broker = BrokerFactory.createBroker(new URI("broker:" + activemqURL));
            broker.setUseJmx(false);
            broker.setUseShutdownHook(false);
            broker.setDataDirectory(System.getProperty("jboss.server.data.dir"));
            broker.start();

            LOG.info("ActiveMQ started {}", activemqURL);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }


    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
