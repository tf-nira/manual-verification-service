package in.tf.nira.manual.verification.listener;

import javax.jms.Message;

public abstract class QueueListener {
	
	public abstract void setListener(Message message);

}
