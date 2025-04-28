package in.tf.nira.manual.verification.listener;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.service.ApplicationService;
import io.mosip.registration.processor.core.queue.factory.MosipQueue;
import io.mosip.registration.processor.core.spi.queue.MosipQueueConnectionFactory;
import io.mosip.registration.processor.core.spi.queue.MosipQueueManager;
import io.mosip.registration.processor.core.queue.factory.QueueListener;


@Component
public class Listener {

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	public static final String DECISION_SERVICE_ID = "mosip.registration.processor.manual.verification.decision.id";
	private static final String APPROVED = "APPROVED";
	private static final String REJECTED = "REJECTED";

	@Value("${registration.processor.queue.verification.response:mvs-to-mosip}")
	private String verificationResponseAddress;

	/** The address. */
	@Value("${registration.processor.queue.verification.request:mosip-to-mvs}")
	private String verificationRequestAddress;
	
	@Value("${registration.processor.verification.queue.username}")
	private String vusername;

	/** The password. */
	@Value("${registration.processor.verification.queue.password}")
	private String vpassword;

	@Value("${registration.processor.verification.queue.url}")
	private String vbrokerUrl;
	
	@Autowired
	private Environment env;

	private ActiveMQConnectionFactory activeMQConnectionFactory;

	/** The Constant FAIL_OVER. */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";

	private Connection connection;
	private Session session;
	private Destination destination;

	@Autowired
    ApplicationService verificationService;

	private Timer timer = new Timer();

	@Autowired
	private MosipQueueManager<MosipQueue, byte[]> mosipQueueManager;

	@Autowired
	private MosipQueueConnectionFactory<MosipQueue> mosipConnectionFactory;

	private MosipQueue queue;
	
	private Map<String, Integer> messageTypeMap = new ConcurrentHashMap<>();
	
	public Integer getMessageType(String applicationId) {
	    Integer type = messageTypeMap.get(applicationId);
	    return (type == null || type == 0) ? 1 : type; // Default to 1 if not found
	}

	public void setMessageType(String applicationId, Integer textType) {
	    messageTypeMap.put(applicationId, textType);
	}

	public boolean consumeLogic(javax.jms.Message message, String mvAddress) {
		boolean isrequestAddedtoQueue = false;
		Integer textType = 0;
		String messageData = null;
		try {
			if (message instanceof TextMessage || message instanceof ActiveMQTextMessage) {
				logger.info("Received message is text");
				textType = 1;
				TextMessage textMessage = (TextMessage) message;
				messageData = textMessage.getText();

			} else if (message instanceof ActiveMQBytesMessage) {
				logger.info("Received message is byte");
				textType = 2;
				messageData = new String(((ActiveMQBytesMessage) message).getContent().data);

			} else {
				logger.error("Received message is neither text nor byte");
				return false;
			}
			logger.info(String.format("Message Data %s" , messageData));

			final ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			CreateAppRequestDTO verifyRequestDTO = mapper.readValue(messageData, CreateAppRequestDTO.class);
			
			setMessageType(verifyRequestDTO.getRegId(), textType);
			
			verificationService.createApplication(verifyRequestDTO);
		} catch (Exception e) {
			logger.error("Could not process mv request", ExceptionUtils.getStackTrace(e));
		}
		logger.info(String.format("Is response sent = %b" , isrequestAddedtoQueue));
		return isrequestAddedtoQueue;
	}

	private MosipQueue getQueueConnection() {
	    String failOverBrokerUrl = FAIL_OVER + vbrokerUrl + "," + vbrokerUrl + RANDOMIZE_FALSE;
	    logger.info("Inside getQueueConnection()");
	    return mosipConnectionFactory.createConnection("ACTIVEMQ", vusername, vpassword, failOverBrokerUrl,
	            Arrays.asList("io.mosip", "in.tf.nira", "org.apache.activemq.command"));
	}
	
	public void setup() {
		logger.info("Inside setup.");
		if(connection == null || ((ActiveMQConnection) connection).isClosed()) {
			logger.info("Connection is null.");
		}
		if(!(connection == null || ((ActiveMQConnection) connection).isClosed()) && session == null) {
			logger.info("Connection is not null.");
			logger.info("Session is null.");
			logger.info("Starting new Session.");
			try {
				connection.start();
				this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				logger.info("Started new Session.");
			} catch (Exception e) {
				logger.error("Error occured while creating a new session: "+ e.getMessage());
				e.printStackTrace();
			} 
		}
		try {
			if (connection == null || ((ActiveMQConnection) connection).isClosed()) {
				logger.info("Creating new connection.");
				connection = activeMQConnectionFactory.createConnection();

				if (session == null) {
					logger.info("Starting new Session.");
					connection.start();
					this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					logger.info("Started new Session.");
				}
			}
		} catch (Exception e) {
			logger.error("Error occured while creating a new session: "+ e.getMessage());
			logger.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		logger.info("Setup Completed.");
	}

	public void runVerificationQueue() {
	    try {
	    	logger.info("Inside runVerificationQueue()");
	        queue = getQueueConnection();
	        if (queue != null) {
	        	logger.info("Queue is not null");
	            QueueListener listener = new QueueListener() {
	                @Override
	                public void setListener(Message message) {
	                    consumeLogic(message, verificationResponseAddress);
	                }
	            };
	            
	            mosipQueueManager.consume(queue, verificationRequestAddress, listener);
	        } else {
	            logger.error("Queue connection not found");
	            throw new RuntimeException("Queue connection not found");
	        }
	    } catch (Exception e) {
	        logger.error("Error in queue setup: " + e.getMessage(), e);
	    }
	}

	public byte[] consume(String address, QueueListener object, String brokerUrl, String username, String password) throws Exception {

		if (activeMQConnectionFactory == null) {
			logger.info("Creating new connection.");
			String failOverBrokerUrl = FAIL_OVER + brokerUrl + "," + brokerUrl + RANDOMIZE_FALSE;
			logger.info(String.format("Broker url : %s" , failOverBrokerUrl));
			this.activeMQConnectionFactory = new ActiveMQConnectionFactory(username, password, failOverBrokerUrl);
		}

		ActiveMQConnectionFactory activeMQConnectionFactory = this.activeMQConnectionFactory;
		if (activeMQConnectionFactory == null) {
			logger.error("Could not create connection. Invalid connection configuration.");
			throw new Exception("Invalid Connection Exception");

		}
		if (destination == null) {
			setup();
		}
		MessageConsumer consumer;
		try {
			if(session == null) {
				setup();
			}
			destination = session.createQueue(address);
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(getListener(object));

		} catch (JMSException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MessageListener getListener(QueueListener object) {
			return new MessageListener() {
				@Override
				public void onMessage(Message message) {
					object.setListener(message);
				}
			};
	}
	
	public void sendToQueue(ResponseEntity<Object> obj, Integer textType)
	        throws JsonProcessingException, UnsupportedEncodingException {
	    final ObjectMapper mapper = new ObjectMapper();
	    mapper.findAndRegisterModules();
	    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	    logger.info("Response: {}", obj.getBody().toString());
	    if (textType == 2) {
	        send(mapper.writeValueAsString(obj.getBody()).getBytes("UTF-8"), verificationResponseAddress);
	    } else if (textType == 1) {
	        send(mapper.writeValueAsString(obj.getBody()), verificationResponseAddress);
	    }
	}

	public Boolean send(String message, String address) {
	    boolean flag = false;
	    
	    try {
	        if (queue == null) {
	            queue = getQueueConnection();
	        }
	        
	        flag = mosipQueueManager.send(queue, message, address);
	        logger.info("Message sent to queue: {}, success: {}", address, flag);
	    } catch (Exception e) {
	        logger.error("Error sending message to queue: " + e.getMessage(), e);
	    }
	    
	    return flag;
	}

	public Boolean send(byte[] message, String address) {
	    boolean flag = false;
	    
	    try {
	        if (queue == null) {
	            queue = getQueueConnection();
	        }
	        
	        flag = mosipQueueManager.send(queue, message, address);
	        logger.info("Byte message sent to queue: {}, success: {}", address, flag);
	    } catch (Exception e) {
	        logger.error("Error sending byte message to queue: " + e.getMessage(), e);
	    }
	    
	    return flag;
	}

	public static ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

	public static String javaObjectToJsonString(Object className) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		String outputJson = null;
		outputJson = objectMapper.writeValueAsString(className);
		return outputJson;
	}
	
	public boolean executeAsync(String response, int delayResponse, Integer textType,String mvAddress ){
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					if (textType == 2) {
						send(response.getBytes(), mvAddress);
					} else if (textType == 1) {

						send(response, mvAddress);
					}
					logger.info(String.format("Scheduled job completed: MsgType %d ",textType));
				} catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		};

		logger.info(String.format("Adding timed task with timer as %d seconds",delayResponse));
		timer.schedule(task, delayResponse*1000);
		return true;
	}

}
