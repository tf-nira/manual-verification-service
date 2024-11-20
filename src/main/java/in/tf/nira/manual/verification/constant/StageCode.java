package in.tf.nira.manual.verification.constant;

public enum StageCode {
	APPROVED("APPROVED", "APP"),
	REJECTED("REJECTED", "REJ"),
	ASSIGNED_TO_OFFICER("ASSIGNED TO OFFICER", "ATO"),
	ASSIGNED_TO_SUPERVISOR("ASSIGNED TO SUPERVISOR", "ATS"),
	ASSIGNED_TO_DISTRICT_OFFICER("ASSIGNED TO DISTRICT OFFICER", "ATDO"),
	INTERVIEW_SCHEDULED("INTERVIEW SCHEDULED", "INS");
	
	final String stage;
	final String stageCode;
	
	private StageCode(String stage, String stageCode) {
		this.stage = stage;
		this.stageCode = stageCode;
	}
	
	public String getStage() {
		return stage;
	}
	
	public String getStageCode() {
		return stageCode;
	}
}
