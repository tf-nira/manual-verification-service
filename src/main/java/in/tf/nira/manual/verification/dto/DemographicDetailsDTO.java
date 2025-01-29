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

        private String applicantUnabletoSign;
        private String email;

        @JsonFormat(pattern = "yyyy/MM/dd")
        private LocalDate ninExpiryDate;

        private String signature;

        @JsonProperty("applicantForeignResidenceAddress")
        private List<LanguageValue> applicantForeignResidenceAddress;
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
    }
}
