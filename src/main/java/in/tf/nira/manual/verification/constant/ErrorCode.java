package in.tf.nira.manual.verification.constant;

public enum ErrorCode {
	INVALID_COLUMN("MVS-001", "Invalid column received : %s"),
	INVALID_PAGINATION_VALUE("MVS-002", "Invalid pagination value received pagestart:%d and pagefetch:%d"),
	FILTER_TYPE_NOT_AVAILABLE("MVS-003", "Filter type is missing"),
	MISSING_FILTER_COLUMN("MVS-004", "Column is missing in request"),
	INVALID_SORT_INPUT("MVS-005", "Missing sort field or sort type values"),
	INVALID_BETWEEN_VALUES("MVS-006", "Invalid fromValue or toValue"),
	INVALID_PAGINATION("MVS-007", "Pagination cannot be null"),
	INVALID_SORT_TYPE("MVS-008", "Sort type %s is not supported"),
	ERROR_OCCURED_WHILE_SORTING("MVS-009", "Error occured while sorting"),
	INVALID_COLUMN_VALUE("MVS-010", "Invalid value present for the given column"),
	INVALID_SORT_FIELD("MVS-011", "Invalid sort field %s"), 
	INVALID_VALUE("MVS-012", "Invalid filter value"),
	INVALID_VALUES("MVS-013", "Invalid filter values"),
	FAILED_TO_FETCH_CLAIMS("MVS-014","Failed to fetch claims from mapping file"),
	FAILED_TO_FETCH_ACRVALUES("MVS-015","failed to fetch acr values from mapping file"),
	INVALID_VALUE_VALUES("MVS-016", "Both value and values cannot be present"),
	INVALID_STATUS_VALUE("MVS-017", "Invalid status value"),
	ESCALATION_NOT_ALLOWED("MVS-018", "Escalation not allowed"),
	SCHEDULE_INTERVIEW_NOT_ALLOWED("MVS-019", "Interview scheduling not allowed"),
	DOCUMENT_UPLOAD_NOT_ALLOWED("MVS-020", "Document upload not allowed"),
	INVALID_APP_ID("MVS-021", "Invalid Application Id"),
	OFFICER_FOR_ROLE_NOT_AVAILABLE("MVS-022", "Officer for %d role not available for assignment"),
	OFFICER_FOR_ID_NOT_AVAILABLE("MVS-023", "Officer not available for assignment");
     
	/**
	 * The error code.
	 */
	private final String errorCode;
	/**
	 * The error message.
	 */
	private final String errorMessage;

	/**
	 * Constructor for MasterdataSearchErrorCode.
	 * 
	 * @param errorCode    the error code.
	 * @param errorMessage the error message.
	 */
	private ErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for error code.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for error message.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
