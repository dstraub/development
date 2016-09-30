package de.ctrlaltdel.development;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

/**
 * JmsToolServlet
 */
@SuppressWarnings("unchecked")
@WebServlet(urlPatterns = "/jms")
public class JmsTool extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ActivemqStartup.class);

    private enum OP {
        SEND,
        RECEIVE,
        BROWSE
    }

    private static final String HELP = "JMS-TestServlet parameter:\n"
            + " op [send,receive,browse]\n"
            + " queue QUEUE_NAME\n"
            + " data TEXT_TO_SEND\n"
            ;


    @Resource(lookup = "java:/JmsXA")
    private QueueConnectionFactory connectionFactory;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws  IOException {
        try {

            String opName = request.getParameter("op");
            if (opName == null) {
                response.getWriter().write(HELP);
                return;
            }

            OP operation = OP.valueOf(opName.toUpperCase());
            String queueName = request.getParameter("queue");
            if (queueName == null) {
                throw new IllegalArgumentException("queue is missing");
            }

            String result = "";
            switch (operation) {
                case SEND:
                    result = sendMessage(queueName, request.getParameter("data"));
                    break;
                case RECEIVE:
                    result = receiveMessage(queueName);
                    break;
                case BROWSE:
                    result = browseMessage(queueName);
                    break;

            }

            response.getWriter().write(result + '\n');

        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        } finally {
            response.flushBuffer();
        }
    }

    private String sendMessage(String queueName, String data) throws Exception {
        QueueConnection connection = connectionFactory.createQueueConnection();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(session.createQueue(queueName));
        Message message = session.createTextMessage(data);
        producer.send(message);
        try {
            return "Message sent " + message.getJMSMessageID();
        } finally {
            producer.close();
            session.close();
            connection.close();
        }
    }

    private String receiveMessage(String queueName) throws Exception {
        QueueConnection connection = connectionFactory.createQueueConnection();
        connection.start();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueReceiver receiver = session.createReceiver(session.createQueue(queueName));
        TextMessage message = (TextMessage) receiver.receive(1000);
        try {
            return message == null
                  ? "No message received"
                  : "Message received " + message.getJMSMessageID() + " " + message.getText();
        } finally {
            receiver.close();
            session.close();
            connection.close();
        }
    }

    private String browseMessage(String queueName) throws Exception {
        QueueConnection connection = connectionFactory.createQueueConnection();
        connection.start();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueBrowser browser = session.createBrowser(session.createQueue(queueName));
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<TextMessage> enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                TextMessage message = enumeration.nextElement();
                sb.append("Message ").append(message.getJMSMessageID()).append(' ').append(message.getText()).append('\n');
            }
            return sb.toString().trim();
        } finally {
            browser.close();
            session.close();
            connection.close();
        }

    }

}
