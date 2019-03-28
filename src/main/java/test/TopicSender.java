/*
 * Sender - Testing topics support with AMQP 1.0 brokers
 */
package test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
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
import org.apache.qpid.jms.JmsTopic;

public class TopicSender {
	private static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;

	private static final String usr = "test";
	private static final String pwd = "test";

	public static void main(String[] args) throws Exception {
		String port = "5675";
		String node = "croads";

		Options options = new Options();

		Option op2 = new Option("p", "port", true, "port to connect to");
		op2.setRequired(false);
		options.addOption(op2);

		Option op4 = new Option("h", "help", false, "help");
		op4.setRequired(false);
		options.addOption(op4);

		Option op3 = new Option("n", "node", true, "node (i.e. exchange, queue, ...)");
		op2.setRequired(false);
		options.addOption(op3);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
			port = cmd.getOptionValue("p", "5675");
			node = cmd.getOptionValue("n", node);

			if ( cmd.hasOption( "help" ) ) {
				formatter.printHelp("Sender", options);
				System.exit(1);
			}

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Sender", options);
			System.exit(1);
		}

		String conn_str = "amqp://localhost:" + port;

		try {
			// set up JNDI context
			Hashtable<String, String> hashtable = new Hashtable<>();
			hashtable.put("connectionfactory.myFactoryLookup", conn_str );
			hashtable.put("topic.myTopicLookup", node);
			hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			Context context = new InitialContext(hashtable);
			ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");

			Connection connection = factory.createConnection(usr, pwd);
			connection.setClientID("java-sender");
			connection.setExceptionListener(new MyExceptionListener());
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			System.out.println("connection_open: "+conn_str);
			System.out.println("node: "+node);

			String[] nat = {"it", "at", "at", "it", "it"};
			String[] prod = {"a22", "xyz", "xyz", "a22", "a22"};
			String[] type = {"asn1", "datex", "asn1", "asn1", "asn1"};
			String[] det = {"denm", "ivim", "denm", "ivim", "denm"};
			String[] geo = {"u0j2ws2", "", "", "u0j2x5z", "u0j8rkm"};
			
			System.out.println("sending messages..."+nat.length);

			long start = System.currentTimeMillis();
			for (int i = 0; i < nat.length; i++) {
				String body = "test"+(i+1);
				TextMessage message = session.createTextMessage(body);

				message.setStringProperty("nat", nat[i]);
				message.setStringProperty("prod", prod[i]);
				message.setStringProperty("type", type[i]);
				message.setStringProperty("det", det[i]);
				message.setStringProperty("geo", geo[i]);

				String topic = nat[i]+"."+prod[i]+"."+type[i]+"."+det[i];
				if (geo[i] != null) {
					String[] chars = geo[i].split("");
					String geoTopic = geo[i].substring(0, Math.min(4, geo[i].length()));
					for (int k = 4; k < chars.length; k++) {
						geoTopic += "."+chars[k];
					}
					topic += "."+geoTopic;
				}
				System.out.println(i + "-sent: " + body + ", topic: " + node+"/"+topic);

				topicSend(node+"/"+topic, message, session);
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

	public static void topicSend(String topic, Message message, Session session) throws Exception {
		MessageProducer messageProducer = session.createProducer(new JmsTopic(topic));
		messageProducer.send(message, DELIVERY_MODE, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		messageProducer.close();
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
