package in.tf.nira.manual.verification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicDetailsDTO {

	private String status;
    private Identity identity;
    private List<Document> documents;
    private List<Object> verifiedAttributes;
    private String dateOfIssuance;
    private String dateOfExpiry;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identity {
        @JsonProperty("UIN")
        private String uin;

        @JsonProperty("CountryCode")
        private List<LanguageValue> countryCode;

        @JsonProperty("IDSchemaVersion")
        private double idSchemaVersion;

        @JsonProperty("NIN")
        private String nin;

        @JsonProperty("appBirCountryUGA")
        private List<LanguageValue> appBirCountryUGA;

        @JsonProperty("appOriCountryUGA")
        private List<LanguageValue> appOriCountryUGA;

        @JsonProperty("appResCountryUGA")
        private List<LanguageValue> appResCountryUGA;

        private String applicantPassportFileNumber;
        private String applicantPassportNumber;

        @JsonProperty("applicantPlaceOfBirthCity")
        private List<LanguageValue> applicantPlaceOfBirthCity;

        @JsonProperty("applicantPlaceOfBirthCounty")
        private List<LanguageValue> applicantPlaceOfBirthCounty;

        @JsonProperty("applicantPlaceOfBirthDistrict")
        private List<LanguageValue> applicantPlaceOfBirthDistrict;

        @JsonProperty("applicantPlaceOfBirthHealthFacility")
        private List<LanguageValue> applicantPlaceOfBirthHealthFacility;

        @JsonProperty("applicantPlaceOfBirthParish")
        private List<LanguageValue> applicantPlaceOfBirthParish;

        @JsonProperty("applicantPlaceOfBirthSubCounty")
        private List<LanguageValue> applicantPlaceOfBirthSubCounty;

        @JsonProperty("applicantPlaceOfBirthVillage")
        private List<LanguageValue> applicantPlaceOfBirthVillage;

        @JsonProperty("applicantPlaceOfOriginClan")
        private List<LanguageValue> applicantPlaceOfOriginClan;

        @JsonProperty("applicantPlaceOfOriginCounty")
        private List<LanguageValue> applicantPlaceOfOriginCounty;

        @JsonProperty("applicantPlaceOfOriginDistrict")
        private List<LanguageValue> applicantPlaceOfOriginDistrict;

        @JsonProperty("applicantPlaceOfOriginIndigenousCommunityTribe")
        private List<LanguageValue> applicantPlaceOfOriginIndigenousCommunityTribe;

        @JsonProperty("applicantPlaceOfOriginParish")
        private List<LanguageValue> applicantPlaceOfOriginParish;

        @JsonProperty("applicantPlaceOfOriginSubCounty")
        private List<LanguageValue> applicantPlaceOfOriginSubCounty;

        @JsonProperty("applicantPlaceOfOriginVillage")
        private List<LanguageValue> applicantPlaceOfOriginVillage;

        @JsonProperty("applicantPlaceOfResidenceCounty")
        private List<LanguageValue> applicantPlaceOfResidenceCounty;

        @JsonProperty("applicantPlaceOfResidenceDistrict")
        private List<LanguageValue> applicantPlaceOfResidenceDistrict;

        @JsonProperty("applicantPlaceOfResidenceParish")
        private List<LanguageValue> applicantPlaceOfResidenceParish;

        @JsonProperty("applicantPlaceOfResidenceSubCounty")
        private List<LanguageValue> applicantPlaceOfResidenceSubCounty;

        @JsonProperty("applicantPlaceOfResidenceVillage")
        private List<LanguageValue> applicantPlaceOfResidenceVillage;

        private String dateOfBirth;

        @JsonProperty("disabilities")
        private List<LanguageValue> disabilities;

        @JsonProperty("fatherPlaceOfOriginVillage")
        private List<LanguageValue> fatherPlaceOfOriginVillage;

        @JsonProperty("fatherPlaceOfResidenceCounty")
        private List<LanguageValue> fatherPlaceOfResidenceCounty;

        @JsonProperty("fatherPlaceOfResidenceDistrict")
        private List<LanguageValue> fatherPlaceOfResidenceDistrict;

        @JsonProperty("fatherPlaceOfResidenceParish")
        private List<LanguageValue> fatherPlaceOfResidenceParish;

        @JsonProperty("fatherPlaceOfResidenceStreet")
        private List<LanguageValue> fatherPlaceOfResidenceStreet;

        @JsonProperty("fatherPlaceOfResidenceSubCounty")
        private List<LanguageValue> fatherPlaceOfResidenceSubCounty;

        @JsonProperty("fatherPlaceOfResidenceVillage")
        private List<LanguageValue> fatherPlaceOfResidenceVillage;

        @JsonProperty("fatherPreviousName")
        private List<LanguageValue> fatherPreviousName;

        @JsonProperty("fatherSurname")
        private List<LanguageValue> fatherSurname;
        
        private List<LanguageValue> fatherLivingStatus;
        
        private List<LanguageValue> fatherGivenName;
        
        private List<LanguageValue> fatherOtherNames;
        
        private String fatherIDDocumentNo;
        
        private List<LanguageValue> fatherOccupation;
        
        private List<LanguageValue> fatherOtherOccupation;
        
        private List<LanguageValue> fatherForeignResidenceCountry;
        
        private List<LanguageValue> fatherForeignResidenceAddress;
        
        private List<LanguageValue> fatherPostalAddress;
        
        private String fatherPlaceOfResidenceHouseNo;
        
        private List<LanguageValue> fatherForeignOriginCountry;
        
        private List<LanguageValue> fatherForeignOriginAddress;
        
        private List<LanguageValue> fatherPlaceOfOriginDistrict;
        
        private List<LanguageValue> fatherPlaceOfOriginCounty;
        
        private List<LanguageValue> fatherPlaceOfOriginSubCounty;
        
        private List<LanguageValue> fatherPlaceOfOriginParish;
        
        private String fatherNIN;
        
        private List<LanguageValue> fatherCitizenshipType;
        
        private String fatherCitizenCertificateNumber;
        
        private List<LanguageValue> fatherIndigenousCommunityTribe;
        
        private List<LanguageValue> fatherIndigenousCommunityClan;
        
        private List<LanguageValue> motherLivingStatus;
        
        private List<LanguageValue> motherSurname;
        
        private List<LanguageValue> motherGivenName;
        
        private List<LanguageValue> motherOtherNames;
        
        private List<LanguageValue> motherMaidenName;
        
        private List<LanguageValue> motherPreviousName;
        
        private String motherIDDocumentNo;
        
        private List<LanguageValue> motherOccupation;
        
        private List<LanguageValue> motherOtherOccupation;
        
        private List<LanguageValue> motherForeignResidenceCountry;
        
        private List<LanguageValue> motherForeignResidenceAddress;
        
        private List<LanguageValue> motherPostalAddress;
        
        private List<LanguageValue> motherPlaceOfResidenceDistrict;
        
        private List<LanguageValue> motherPlaceOfResidenceCounty;
        
        private List<LanguageValue> motherPlaceOfResidenceSubCounty;
        
        private List<LanguageValue> motherPlaceOfResidenceParish;
        
        private List<LanguageValue> motherPlaceOfResidenceVillage;
        
        private List<LanguageValue> motherPlaceOfResidenceStreet;
        
        private List<LanguageValue> motherForeignOriginCountry;
        
        private List<LanguageValue> motherForeignOriginAddress;
        
        private List<LanguageValue> motherPlaceOfOriginDistrict;
        
        private List<LanguageValue> motherPlaceOfOriginCounty;
        
        private List<LanguageValue> motherPlaceOfOriginSubCounty;
        
        private List<LanguageValue> motherPlaceOfOriginParish;
        
        private List<LanguageValue> motherPlaceOfOriginVillage;
        
        private List<LanguageValue> motherCitizenshipType;
        
        private List<LanguageValue> motherIndigenousCommunityTribe;
        
        private List<LanguageValue> motherIndigenousCommunityClan;
        
        private String motherPlaceOfResidenceHouseNo;
        
        private String motherNIN;
        
        private String motherCitizenCertificateNumber;

        @JsonProperty("gender")
        private List<LanguageValue> gender;

        @JsonProperty("givenName")
        private List<LanguageValue> givenName;

        @JsonProperty("guardianClan")
        private List<LanguageValue> guardianClan;

        @JsonProperty("guardianGivenName")
        private List<LanguageValue> guardianGivenName;

        @JsonProperty("guardianOtherNames")
        private List<LanguageValue> guardianOtherNames;

        @JsonProperty("guardianRelationToApplicant")
        private List<LanguageValue> guardianRelationToApplicant;

        @JsonProperty("guardianResidenceCounty")
        private List<LanguageValue> guardianResidenceCounty;

        @JsonProperty("guardianResidenceDistrict")
        private List<LanguageValue> guardianResidenceDistrict;

        @JsonProperty("guardianResidenceParish")
        private List<LanguageValue> guardianResidenceParish;

        @JsonProperty("guardianResidenceSubCounty")
        private List<LanguageValue> guardianResidenceSubCounty;

        @JsonProperty("guardianResidenceVillage")
        private List<LanguageValue> guardianResidenceVillage;

        @JsonProperty("guardianSurname")
        private List<LanguageValue> guardianSurname;

        @JsonProperty("guardianTribe")
        private List<LanguageValue> guardianTribe;

        @JsonProperty("highestLevelOfEducation")
        private List<LanguageValue> highestLevelOfEducation;

        private String homePhoneNumber;
        private String inDepthCitizenshipVerification;

        private IndividualBiometrics individualBiometrics;

        @JsonProperty("maidenName")
        private List<LanguageValue> maidenName;

        @JsonProperty("maritalStatus")
        private List<LanguageValue> maritalStatus;
        
        @JsonProperty("spouseSurname")
        private List<LanguageValue> spouseSurname;
        
        @JsonProperty("spouseGivenName")
        private List<LanguageValue> spouseGivenName;
        
        @JsonProperty("spouseOtherNames")
        private List<LanguageValue> spouseOtherNames;
        
        @JsonProperty("spouseMaidenName")
        private List<LanguageValue> spouseMaidenName;
        
        @JsonProperty("spousePreviousName")
        private List<LanguageValue> spousePreviousName;
        
        @JsonProperty("spouseNIN")
        private String spouseNIN;
        
        @JsonProperty("spouseCitizenshipType")
        private List<LanguageValue> spouseCitizenshipType;
        
        @JsonProperty("spousePlaceOfMarriage")
        private List<LanguageValue> spousePlaceOfMarriage;
        
        @JsonProperty("spouseDateOfMarriage")
        private String spouseDateOfMarriage;
        
        @JsonProperty("spouseTypeOfMarriage")
        private List<LanguageValue> spouseTypeOfMarriage;
        
        @JsonProperty("spouseMarriageCertificateNumber")
        private String spouseMarriageCertificateNumber;
        
        @JsonProperty("spouseTwoSurname")
        private List<LanguageValue> spouseTwoSurname;
        
        @JsonProperty("spouseTwoGivenName")
        private List<LanguageValue> spouseTwoGivenName;
        
        @JsonProperty("spouseTwoOtherNames")
        private List<LanguageValue> spouseTwoOtherNames;
        
        @JsonProperty("spouseTwoMaidenName")
        private List<LanguageValue> spouseTwoMaidenName;
        
        @JsonProperty("spouseTwoPreviousName")
        private List<LanguageValue> spouseTwoPreviousName;
        
        @JsonProperty("spouseTwoNIN")
        private String spouseTwoNIN;
        
        @JsonProperty("spouseTwoCitizenshipType")
        private List<LanguageValue> spouseTwoCitizenshipType;
        
        @JsonProperty("spouseTwoPlaceOfMarriage")
        private List<LanguageValue> spouseTwoPlaceOfMarriage;
        
        @JsonProperty("spouseTwoDateOfMarriage")
        private String spouseTwoDateOfMarriage;
        
        @JsonProperty("spouseTwoTypeOfMarriage")
        private List<LanguageValue> spouseTwoTypeOfMarriage;
        
        @JsonProperty("spouseTwoMarriageCertificateNumber")
        private String spouseTwoMarriageCertificateNumber;
        
        @JsonProperty("spouseThreeSurname")
        private List<LanguageValue> spouseThreeSurname;
        
        @JsonProperty("spouseThreeGivenName")
        private List<LanguageValue> spouseThreeGivenName;
        
        @JsonProperty("spouseThreeOtherNames")
        private List<LanguageValue> spouseThreeOtherNames;
        
        @JsonProperty("spouseThreeMaidenName")
        private List<LanguageValue> spouseThreeMaidenName;
        
        @JsonProperty("spouseThreePreviousName")
        private List<LanguageValue> spouseThreePreviousName;
        
        @JsonProperty("spouseThreeNIN")
        private String spouseThreeNIN;
        
        @JsonProperty("spouseThreeCitizenshipType")
        private List<LanguageValue> spouseThreeCitizenshipType;
        
        @JsonProperty("spouseThreePlaceOfMarriage")
        private List<LanguageValue> spouseThreePlaceOfMarriage;
        
        @JsonProperty("spouseThreeDateOfMarriage")
        private String spouseThreeDateOfMarriage;
        
        @JsonProperty("spouseThreeTypeOfMarriage")
        private List<LanguageValue> spouseThreeTypeOfMarriage;
        
        @JsonProperty("spouseThreeMarriageCertificateNumber")
        private String spouseThreeMarriageCertificateNumber;
        
        @JsonProperty("spouseFourSurname")
        private List<LanguageValue> spouseFourSurname;
        
        @JsonProperty("spouseFourGivenName")
        private List<LanguageValue> spouseFourGivenName;
        
        @JsonProperty("spouseFourOtherNames")
        private List<LanguageValue> spouseFourOtherNames;
        
        @JsonProperty("spouseFourMaidenName")
        private List<LanguageValue> spouseFourMaidenName;
        
        @JsonProperty("spouseFourPreviousName")
        private List<LanguageValue> spouseFourPreviousName;
        
        @JsonProperty("spouseFourNIN")
        private String spouseFourNIN;
        
        @JsonProperty("spouseFourCitizenshipType")
        private List<LanguageValue> spouseFourCitizenshipType;
        
        @JsonProperty("spouseFourPlaceOfMarriage")
        private List<LanguageValue> spouseFourPlaceOfMarriage;
        
        @JsonProperty("spouseFourDateOfMarriage")
        private String spouseFourDateOfMarriage;
        
        @JsonProperty("spouseFourTypeOfMarriage")
        private List<LanguageValue> spouseFourTypeOfMarriage;
        
        @JsonProperty("spouseFourMarriageCertificateNumber")
        private String spouseFourMarriageCertificateNumber;
        
        private List<LanguageValue> childSurname;
        
        private List<LanguageValue> childGivenName;
        
        private List<LanguageValue> childOtherName;
        
        private List<LanguageValue> childSex;
        
        private String childDateOfBirth;
        
        private String childNIN;
        
        private List<LanguageValue> childTwoSurname;
        
        private List<LanguageValue> childTwoGivenName;
        
        private List<LanguageValue> childTwoOtherName;
        
        private List<LanguageValue> childTwoSex;
        
        private String childTwoDateOfBirth;
        
        private String childTwoNIN;
        
        private List<LanguageValue> childThreeSurname;
        
        private List<LanguageValue> childThreeGivenName;
        
        private List<LanguageValue> childThreeOtherName;
        
        private List<LanguageValue> childThreeSex;
        
        private String childThreeDateOfBirth;
        
        private String childThreeNIN;
        
        private List<LanguageValue> childFourSurname;
        
        private List<LanguageValue> childFourGivenName;
        
        private List<LanguageValue> childFourOtherName;
        
        private List<LanguageValue> childFourSex;
        
        private String childFourDateOfBirth;
        
        private String childFourNIN;
        
        private List<LanguageValue> childFiveSurname;
        
        private List<LanguageValue> childFiveGivenName;
        
        private List<LanguageValue> childFiveOtherName;
        
        private List<LanguageValue> childFiveSex;
        
        private String childFiveDateOfBirth;
        
        private String childFiveNIN;
        
        private List<LanguageValue> childSixSurname;
        
        private List<LanguageValue> childSixGivenName;
        
        private List<LanguageValue> childSixOtherName;
        
        private List<LanguageValue> childSixSex;
        
        private String childSixDateOfBirth;
        
        private String childSixNIN;
        
        private List<LanguageValue> enrolmentCountry;
        
        private List<LanguageValue> applicantPlaceOfEnrolmentDistrict;
        
        private List<LanguageValue> applicantPlaceOfEnrolmentCounty;
        
        private List<LanguageValue> applicantPlaceOfEnrolmentSubCounty;
        
        private List<LanguageValue> applicantPlaceOfEnrolmentParish;
        
        private List<LanguageValue> applicantPlaceOfEnrolmentVillage;
        
        @JsonProperty("occupation")
        private List<LanguageValue> occupation;

        @JsonProperty("otherNames")
        private List<LanguageValue> otherNames;

        private String part;
        private String phone;

        @JsonProperty("pollingStationNameOrigin")
        private List<LanguageValue> pollingStationNameOrigin;

        private String preferredLang;

        @JsonProperty("preferredPollingStation")
        private List<LanguageValue> preferredPollingStation;

        @JsonProperty("previousName")
        private List<LanguageValue> previousName;

        @JsonProperty("profession")
        private List<LanguageValue> profession;

        @JsonProperty("religion")
        private List<LanguageValue> religion;

        @JsonProperty("residenceStatus")
        private List<LanguageValue> residenceStatus;

        private String selectedHandles;

        @JsonProperty("surname")
        private List<LanguageValue> surname;

        @JsonProperty("userServiceType")
        private List<LanguageValue> userServiceType;

        @JsonProperty("applicantPlaceOfResidencePostalAddress")
        private List<LanguageValue> applicantPlaceOfResidencePostalAddress;

        @JsonProperty("applicantPlaceOfResidenceStreet")
        private List<LanguageValue> applicantPlaceOfResidenceStreet;
        
        private List<LanguageValue> dualCitizenshipCertificateNumber;
        
        private List<LanguageValue> registrationCertificateNumber;
        
        private List<LanguageValue> naturalizationCertificateNumber;

        private String applicantUnabletoSign;
        private String email;
	
        private String ninExpiryDate;

        private String signature;

        @JsonProperty("applicantForeignResidenceAddress")
        private List<LanguageValue> applicantForeignResidenceAddress;
        
        private List<LanguageValue> applicantForeignResidenceCountry;

        //all the document categories as mentioned in prereg ui spec
        
        @JsonProperty("proofOfCitizenship")
        private ProofDocument proofOfCitizenship;
        
        @JsonProperty("proofOfPhysicalApplicationForm")
        private ProofDocument proofOfPhysicalApplicationForm;
        
        @JsonProperty("proofOfAbandonment")
        private ProofDocument proofOfAbandonment;
        
        @JsonProperty("proofOfDeclarant")
        private ProofDocument proofOfDeclarant;
        
        @JsonProperty("proofOfAdoption")
        private ProofDocument proofOfAdoption;
        
        @JsonProperty("proofOfIdentity")
        private ProofDocument proofOfIdentity;
        
        @JsonProperty("proofOfAddress")
        private ProofDocument proofOfAddress;
        
        @JsonProperty("proofOfBirth")
        private ProofDocument proofOfBirth;
        
        @JsonProperty("proofOfRegistration")
        private ProofDocument proofOfRegistration;
        
        @JsonProperty("proofOfOtherSupportingdocumentIssuedbyGovt")
        private ProofDocument proofOfOtherSupportingdocumentIssuedbyGovt;
        
        @JsonProperty("proofOfOtherSupportingDocuments")
        private ProofDocument proofOfOtherSupportingDocuments;
        
        @JsonProperty("proofOfLegalStatutoryDeclaration")
        private ProofDocument proofOfLegalStatutoryDeclaration;
        
        @JsonProperty("proofOfModificationConsent")
        private ProofDocument proofOfModificationConsent;
        
        @JsonProperty("proofOfLoss")
        private ProofDocument proofOfLoss;
        
        @JsonProperty("proofOfLC1Letter")
        private ProofDocument proofOfLC1Letter;
        
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LanguageValue {
        private String language;
        private String value;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndividualBiometrics {
        private String format;
        private int version;
        private String value;
    }
    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
    	private String category;
    	private Object value;
        private String format;
    }
    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProofDocument {
        private String refNumber;
        private String format;
        private String type;
        private String value;
    }
}
