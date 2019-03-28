/*
 * Receiver - Testing topics support with AMQP 1.0 brokers
 */
package test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.commons.cli.*;
import java.util.Hashtable;
import java.util.Enumeration;

public class TopicReceiver {
	private static final String DEFAULT_COUNT = "10";

	private static final String usr = "test";
	private static final String pwd = "test";

	public static void main(String[] args) throws Exception {

		String port = "5675";
		int count = Integer.parseInt(DEFAULT_COUNT);
		String topic = "croads/it.a22.*.*.u0j2.#";

		Options options = new Options();

		Option op1 = new Option("m", "messages", true, "number of messages to expect");
		op1.setRequired(false);
		options.addOption(op1);

		Option op2 = new Option("p", "port", true, "port to connect to");
		op2.setRequired(false);
		options.addOption(op2);

		Option op3 = new Option("t", "topic", true, "topic (node/topic)");
		op2.setRequired(false);
		options.addOption(op3);

		Option op4 = new Option("h", "help", false, "help");
		op4.setRequired(false);
		options.addOption(op4);

		Option op5 = new Option("n", "node", true, "name of node (e.g. queue or topic) from which messages are received");
		op5.setRequired(false);
		options.addOption(op5);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
			port = cmd.getOptionValue("p", "5675");
			count = Integer.parseInt(cmd.getOptionValue("m", DEFAULT_COUNT));
			topic = cmd.getOptionValue("t", topic);

			if ( cmd.hasOption( "help" ) ) {
				formatter.printHelp("Receiver", options);
				System.exit(1);
			}

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Receiver", options);
			System.exit(1);
		}

		String conn_str = "amqp://localhost:" + port;

		try {
			// set up JNDI context
			Hashtable<String, String> hashtable = new Hashtable<>();
			hashtable.put("connectionfactory.myFactoryLookup", conn_str );
			hashtable.put("topic.myTopicLookup", topic);
			hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			Context context = new InitialContext(hashtable);
			ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
			Destination address = (Destination) context.lookup("myTopicLookup");

			Connection connection = factory.createConnection(usr, pwd);
			connection.setClientID("java-receiver");
			connection.setExceptionListener(new MyExceptionListener());
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			System.out.println("connection_open: "+conn_str);
			System.out.println("topic: "+topic);
			System.out.println("consuming up to " + count + " messages...");

			MessageConsumer messageConsumer = session.createConsumer(address);

			long start = System.currentTimeMillis();

			int actualCount = 0;
			boolean deductTimeout = false;
			int timeout = 1000;
			for (int i = 1; i <= count; i++, actualCount++) {
				Message message = messageConsumer.receive();
				if (message == null) {
					System.out.println("Message " + i + " not received within timeout, stopping.");
					deductTimeout = true;
					break;
				} else {
					System.out.println(i+"-message: "+message.getBody(String.class));
					// List all properties
					Enumeration<String> e = (Enumeration<String>) message.getPropertyNames();
					String out = "  ";
					while (e.hasMoreElements()) {
						String name = e.nextElement();
						out += name + "=" + message.getStringProperty(name) + ", ";
					}
					System.out.println(out.substring(0, out.length() - 2));
					/* System.out.println(ANSI_GREEN+out.substring(0, out.length() - 2)+ANSI_RESET); */
				}
				if (i % 100 == 0) {
					System.out.println("Got message " + i);
				}
			}

			long finish = System.currentTimeMillis();
			long taken = finish - start;
			if (deductTimeout) {
				taken -= timeout;
			}
			System.out.println("Received " + actualCount + " messages in " + taken + "ms");

			connection.close();
		} catch (Exception exp) {
			System.out.println("Caught exception, exiting.");
			exp.printStackTrace(System.out);
			System.exit(1);
		}
	}

	private static class MyExceptionListener implements ExceptionListener {
		@Override
		public void onException(JMSException exception) {
			System.out.println("Connection ExceptionListener fired, exiting.");
			exception.printStackTrace(System.out);
			System.exit(1);
		}
	}
}
