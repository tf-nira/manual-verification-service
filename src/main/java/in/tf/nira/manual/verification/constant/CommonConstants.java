package in.tf.nira.manual.verification.constant;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommonConstants {
	//roles
	public static final String MVS_OFFICER_ROLE = "MVS_OFFICER";
	public static final String MVS_SUPERVISOR_ROLE = "MVS_SUPERVISOR";
	public static final String MVS_DISTRICT_OFFICER_ROLE = "MVS_DISTRICT_OFFICER";
	public static final String MVS_LEGAL_OFFICER_ROLE = "MVS_LEGAL_OFFICER";
	public static final String MVS_EXECUTIVE_DIRECTOR = "MVS_EXECUTIVE_DIRECTOR";
	public static final String MVS_SENIOR_REGISTRATION_OFFICER = "MVS_SENIOR_REGISTRATION_OFFICER";
	public static final String MVS_INTERNATIONAL_OFFICER = "MVS_INTERNATIONAL_OFFICER";
	public static final String MVS_MANAGER= "MVS_MANAGER";
	
	//Location hierarchy
	public static final String COUNTY_ATTRIBUTE_KEY = "county";
	public static final String DISTRICT_ATTRIBUTE_KEY = "district";
	
	
	//status
	public static final String APPROVE_STATUS = "APPROVE";
	public static final String REJECT_STATUS = "REJECT";
	public static final String ESCALATE_STATUS = "ESCALATE";
	public static final String SCHEDULE_INTERVIEW_STATUS = "SCHEDULE_INTERVIEW";
	public static final String UPLOAD_DOCUMENTS_STATUS = "UPLOAD_DOCUMENTS";
	public static final String RECOMMEND_FOR_APPROVAL_STATUS = "RECOMMEND_FOR_APPROVAL";
	
	
	public static final String VERSION = "0.1";
	public static final String CREATE_APP_ID = "tf.nira.app.create";
	public static final String GET_USER_APP_ID = "tf.nira.user.app.get";
	public static final String GET_APP_ID = "tf.nira.app.get";
	public static final String GET_CONFIG_ID = "tf.nira.config.get";
	public static final String UPDATE_APP_ID = "tf.nira.app.update";
	public static final String SCHEDULE_APP_ID = "tf.nira.app.schedule";
	public static final String UPLOAD_APP_ID = "tf.nira.app.upload";
	public static final String GET_NIN_DEMOGRAPHIC = "tf.nira.app.get.demographic";
	public static final String GET_DISTRICT_OFFICE_ID = "manual.verification.get.district.office";
	public static final String GET_MATCHED_ID_DEMOGRAPHICS = "tf.nira.app.matchedId.demographic";
	public static final String PACKET_MANAGER_FETCH_DOCUMENT_ID = "mosip.manual.verification.fetch.document.packet.manager";
	public static final String GET_PACKET_INFO_ID = "mosip.manual.verification.get.packet.info";

	
	//service
	public static final String UPDATE = "UPDATE";
	public static final String ALIEN = "Alien";
	
	//Packet-Manager
	public static final String FETCH_DOCUMENT_ID = "mosip.manual.verification.fetch.document";
	public static final String PACKET_MANAGER_REQUEST_ID = "mosip.registration.packet.reader";
	public static final String PACKET_MANAGER_REQUEST_VERSION = "v1";
	
	public static final String OUTSIDE_UGANDA="Outside Uganda";
	public static final String INSIDE_UGANDA="Inside Uganda";
	//Only to determine the role for escalating the application based on the residence status
	public static final String MVS_DISTRICT_OR_INTERNATIONAL_OFFICER_ROLE="MVS_DISTRICT_OR_INTERNATIONAL_OFFICER_ROLE";
	public static final String INTERNATIONAL_ADDRESS ="international";
	
	//id-repo
	
	public static final String NIN = "NIN";
	public static final String GIVEN_NAME = "givenName";
	public static final String SURNAME = "surname";
	public static final String DATE_OF_BIRTH = "dateOfBirth";
	public static final String PHONE = "phone";
	public static final String USER_SERVICE = "userService";
	
	//age-group
	public static final String MINOR = "MINOR";
	public static final String INFANT = "INFANT";
	
	//cop
	public static final String COP_GIVEN_NAME_PREVIOUS = "copGivenNamePrevious";
	public static final String COP_SURNAME_PREVIOUS = "copSurnamePrevious";
	public static final String COP_EMAIL_PREVIOUS = "copEmailPrevious";
	public static final String COP_DATE_OF_BIRTH_PREVIOUS = "copDateOfBirthPrevious";
	public static final String COP_NIN_PREVIOUS = "copNinPrevious";
	public static final String COP_PHONE_PREVIOUS = "copPhonePrevious";
	public static final String COP_HOME_PHONE_NUMBER_PREVIOUS = "copHomePhoneNumberPrevious";
	public static final String COP_COUNTRY_CODE_PREVIOUS = "copCountryCodePrevious";

	public static final String APPLICANT_PLACE_OF_ENROLMENT_DISTRICT = "applicantPlaceOfEnrolmentDistrict";
	
	//Application status for mvs-admin
	public static final String APPROVED = "APPROVED";
	public static final String REJECTED = "REJECTED";

	public static final Map<String, String[]> ESCALATION_CATEGORIES = new LinkedHashMap<>() {{
		put("Suspected Non Citizen", new String[]{
				"New registrations",
				"New registrations officer",
				"Renewal of card",
				"Renewal of card officer",
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars"
		});
		put("Supporting documents look suspicious/forged/Altered", new String[]{
				"New registrations",
				"New registrations officer",
				"Renewal of card",
				"Renewal of card officer",
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars"
		});
		put("Inconsistent information on tribe of mother", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Inconsistent information on clan of mother", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Inconsistent information on tribe of father", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Inconsistent information on clan of father", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Inconsistent information on place of origin-Father", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Inconsistent information on place of origin-Mother", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Inconsistent information on place of origin-Blood Relative", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Wrong citizenship category/Type", new String[]{
				"New registrations",
				"New registrations officer",
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Applicant’s citizenship is stoplisted/blocked", new String[]{
				"New registrations",
				"New registrations officer",
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Father/Mother is too young", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Grandparents are too young", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Different DOB/Age of mother on mother’s individual application", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Different DOB/Age of father on father’s individual application", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Different DOB/Age of blood relative on blood relative individual application", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Attempting double registration", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("NIN has already been used to identify more than 20 people", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Indigenous community not listed under Schedule 3", new String[]{
				"New registrations",
				"New registrations officer"
		});
		put("Missing Documentation", new String[]{
				"New registrations",
				"New registrations officer",
				"Renewal of card"
		});
		put("Insufficient Documentation", new String[]{
				"New registrations",
				"New registrations officer",
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Father is too young", new String[]{
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Mother is too young", new String[]{
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Additional Renewal application", new String[]{
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Names are inconsistent with original application", new String[]{
				"Renewal of card",
				"Renewal of card officer"
		});
		put("Unsatisfactory Evidence of intended changes", new String[]{
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars"
		});
		put("Unauthorized Second Register Entry", new String[]{
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars"
		});
		put("Submission of falsified documents", new String[]{
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars"
		});
		put("Insufficient supporting Documents", new String[]{
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars"
		});
		put("Modification Required",new String[]{
				"GetFirst ID",
				"Replacement of card",
				"Change of Particulars",
				"Renewal of card",
				"Renewal of card officer",
				"New registrations",
				"New registrations officer",
		});
	}};

	public static final Map<String, String[]> REJECTION_CATEGORIES = new LinkedHashMap<>() {{
		put("Rejected due to evidence of non citizenship", new String[]{
				"New registrations"
		});
		put("Insufficient supporting documents to determine citizenship", new String[]{
				"New registrations"
		});
		put("Documents provided have inconsistent information", new String[]{
				"New registrations",
				"Change of Particulars",
				"Renewal of card",
				"GetFirst ID",
				"Replacement of card",
				"Deactivated"
		});
		put("Documents not in required format ", new String[]{
				"New registrations"
		});
		put("Documents not in required format (i.e SD exists but not registered)", new String[]{
				"Change of Particulars",
				"Renewal of card",
				"GetFirst ID",
				"Replacement of card"
		});
		put("Unsatisfactory CV Interview at Point of Registration ", new String[]{
				"New registrations"
		});
		put("Second register/application exists (May or may not have a NIN, stop listed)", new String[]{
				"New registrations"
		});
		put("Poorly scanned documents to enable decision", new String[]{
				"New registrations",
				"Change of Particulars",
				"Renewal of card",
				"GetFirst ID",
				"Replacement of card",
				"Deactivated"
		});
		put("Fraudulent/Altered /doctored documents ", new String[]{
				"New registrations",
				"Change of Particulars",
				"GetFirst ID",
				"Replacement of card"
		});
		put("Fraudulent/Altered /doctored documents", new String[]{
				"Renewal of card",
				"Deactivated"
		});
		put("No payment receipt attached", new String[]{
				"Change of Particulars",
				"Renewal of card",
				"Replacement of card"
		});
		put("Payments used on previous unrelated application", new String[]{
				"Change of Particulars",
				"Renewal of card",
				"Replacement of card"
		});
		put("Payment lower than statutory fees", new String[]{
				"Change of Particulars",
				"Renewal of card",
				"Replacement of card"
		});
		put("Evidence of multiple changes in short period of time(Time should be specified)", new String[]{
				"Change of Particulars",
				"Renewal of card",
				"GetFirst ID",
				"Replacement of card"
		});
		put("Insufficient supporting Documents", new String[]{
				"GetFirst ID",
				"Replacement of card",
				"Renewal of card",
				"Change of Particulars"
		});
		put("An existing record is stop listed", new String[]{
				"Change of Particulars",
				"Renewal of card",
				"GetFirst ID",
				"Replacement of card"
		});
		put("Other", new String[]{
				"New registrations",
				"Change of Particulars",
				"GetFirst ID",
				"Replacement of card"
		});
		put("Other ( Free Text)", new String[]{
				"Renewal of card",
				"Deactivated"
		});
	}};
	
}
