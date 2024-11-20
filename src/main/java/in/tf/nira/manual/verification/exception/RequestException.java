package in.tf.nira.manual.verification.exception;

public class RequestException extends RuntimeException {
	public String reasonConstant = null;
	public int delayResponse = 0;

	public RequestException() {
		super();
	}

	public RequestException(String reasonConstant) {
		super();
		this.reasonConstant = reasonConstant;
	}

	public RequestException(String reasonConstant, int d) {
		super();
		this.reasonConstant = reasonConstant;
		this.delayResponse = d;
	}

	public String getReasonConstant() {
		return reasonConstant;
	}

	public void setReasonConstant(String reasonConstant) {
		this.reasonConstant = reasonConstant;
	}

	public int getDelayResponse() {
		return delayResponse;
	}

	public void setDelayResponse(int d) {
		this.delayResponse = d;
	}
}