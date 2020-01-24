/*
 * Sender - Testing application properties/JMS filters support with AMQP 1.0 brokers
 */
package test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.commons.cli.*;
import java.util.Hashtable;
import java.util.Enumeration;

public class Sender {
	private static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;

	private static final String usr = "test";
	private static final String pwd = "test";

	public static void main(String[] args) throws Exception {
		
        String host = "localhost";
        String port = "5673";
		String node = "croads";
        String user = "";
        String passw = "";

		Options options = new Options();

		Option op2b = new Option("t", "host", true, "host to connect to");
		op2b.setRequired(false);
		options.addOption(op2b);
        
        Option op2 = new Option("p", "port", true, "port to connect to");
		op2.setRequired(false);
		options.addOption(op2);

		Option op4 = new Option("h", "help", false, "help");
		op4.setRequired(false);
		options.addOption(op4);

		Option op5 = new Option("n", "node", true, "name of destination node (e.g. queue or topic) to which messages are sent");
		op5.setRequired(false);
		options.addOption(op5);
        
		Option op6 = new Option("u", "user", true, "username");
		op6.setRequired(false);
		options.addOption(op6);
        
		Option op7 = new Option("w", "pwd", true, "password");
		op7.setRequired(false);
		options.addOption(op7);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
            host = cmd.getOptionValue("t", "localhost");
			port = cmd.getOptionValue("p", "5673");
			node = cmd.getOptionValue("n", node);
            user = cmd.getOptionValue("u", usr);
			passw = cmd.getOptionValue("w", pwd);

			if ( cmd.hasOption( "help" ) ) {
				formatter.printHelp("Sender", options);
				System.exit(1);
			}

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Sender", options);
			System.exit(1);
		}

		String conn_str = "amqp://" + host + ":" + port;

		try {
			// set up JNDI context
			Hashtable<String, String> hashtable = new Hashtable<>();
			hashtable.put("connectionfactory.myFactoryLookup", conn_str );
			hashtable.put("topic.myTopicLookup", node);
			hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			Context context = new InitialContext(hashtable);
			ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
			Destination address = (Destination) context.lookup("myTopicLookup");

			Connection connection = factory.createConnection(user, passw);
			connection.setClientID("java-sender");
			connection.setExceptionListener(new MyExceptionListener());
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			System.out.println("connection_open: "+conn_str);
			System.out.println("address: "+node);
			System.out.println("sending messages...");

			MessageProducer messageProducer = session.createProducer(address);

			String[] nat = {"it", "at", "at", "it", "it"};
			String[] prod = {"a22", "xyz", "xyz", "a22", "a22"};
			String[] type = {"asn1", "datex", "asn1", "asn1", "asn1"};
			String[] det = {"denm", "ivim", "denm", "ivim", "denm"};
			String[] geo = {"u0j2ws2", "", "", "u0j2x5z", "u0j8rkm"};


			long start = System.currentTimeMillis();
			for (int i = 0; i < nat.length; i++) {
				String body = "test"+(i+1);
				TextMessage message = session.createTextMessage(body);

				message.setStringProperty("nat", nat[i]);
				message.setStringProperty("prod", prod[i]);
				message.setStringProperty("type", type[i]);
				message.setStringProperty("det", det[i]);
				message.setStringProperty("geo", geo[i]);
				System.out.println(i + "-sent: " + body);
				// List all properties
				Enumeration<String> e = (Enumeration<String>) message.getPropertyNames();
				String out = "  ";
				while (e.hasMoreElements()) {
					String name = e.nextElement();
					out += name + "=" + message.getStringProperty(name) + ", ";
				}
				System.out.println(out.substring(0, out.length() - 2));

				messageProducer.send(message, DELIVERY_MODE, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
			}

			long finish = System.currentTimeMillis();
			long taken = finish - start;
			System.out.println("Sent " + nat.length + " messages in " + taken + "ms");

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
