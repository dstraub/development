package de.ctrlaltdel.development;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * LogMesssageHandler
 */
public class LogMesssageHandler implements MessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LogMesssageHandler.class);

    private String from;
    private String recipient;
    private String data;

    public void from(String from) throws RejectException {
        this.from = from;
    }

    public void recipient(String recipient) throws RejectException {
        this.recipient = recipient;
    }

    public void data(InputStream inputStream) throws RejectException, IOException {
        StringBuilder sb = new StringBuilder("\n");
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        data = sb.toString();
    }

    public void done() {
        LOG.info("Mail from {}, to {}, content: {}", from, recipient, data);
    }
}
