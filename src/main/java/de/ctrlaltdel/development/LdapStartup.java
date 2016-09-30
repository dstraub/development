package de.ctrlaltdel.development;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.util.MinimalLogFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

/**
 * LdapStartup
 */
@WebListener
public class LdapStartup implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(LdapStartup.class);
    private InMemoryDirectoryServer directoryServer;

    public void contextInitialized(ServletContextEvent servletContextEvent) {

        int listenPort = resolveListePort();
        try {

            String baseDN = createLdapBaseDN();

            InMemoryDirectoryServerConfig serverConfig = new InMemoryDirectoryServerConfig(baseDN);

            serverConfig.setLDAPDebugLogHandler(new ConsoleHandler());

            serverConfig.setSchema(Schema.getDefaultStandardSchema());
            serverConfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", listenPort));
            serverConfig.addAdditionalBindCredentials(
                    System.getProperty("ldap.admin.dn"),
                    System.getProperty("ldap.admin.credential"));

            StreamHandler handler = new StreamHandler(System.out, new MinimalLogFormatter(null, false, false, true));
            handler.setLevel(Level.OFF);
            serverConfig.setAccessLogHandler(handler);
            serverConfig.setLDAPDebugLogHandler(handler);

            directoryServer = new InMemoryDirectoryServer(serverConfig);
            directoryServer.importFromLDIF(true, System.getProperty("jboss.server.config.dir") + "/sample.ldif");
            directoryServer.startListening();

            LOG.info("LDAP {} listening at {}", baseDN, listenPort);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private int resolveListePort() {
        try {
            String ldapUrl = System.getProperty("ldap.url");
            int idx = ldapUrl.lastIndexOf(":");
            return Integer.valueOf(ldapUrl.substring(idx +1));
        } catch (Exception e) {
            //
        }
        return 389;
    }


    private String createLdapBaseDN() {
        String[] ldapBaseDNs = System.getProperty("ldap.base.dn", "ou=people,dc=sample,dc=de").split(",");
        StringBuilder sb = new StringBuilder();
        for (String parts : ldapBaseDNs) {
            if (parts.startsWith("dc=")) {
                sb.append(parts).append(",");
            }
        }
        return sb.substring(0, sb.length() -1);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
