package in.tf.nira.manual.verification.util;

import java.util.List;
import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;

import org.apache.commons.codec.binary.Base64;

public class CbeffToBiometricUtil {

	/**
	 * Gets the photo.
	 *
	 * @param cbeffFileString the cbeff file string
	 * @param type            the type
	 * @param subType         the sub type
	 * @return the photo
	 * @throws Exception the exception
	 */
	public byte[] getImageBytes(String cbeffFileString, String type, List<String> subType) throws Exception {
//		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
//				"CbeffToBiometricUtil::getImageBytes()::entry");

		byte[] photoBytes = null;
		if (cbeffFileString != null) {
			BIR birType = CbeffValidator.getBIRFromXML(Base64.decodeBase64(cbeffFileString));
			List<BIR> bIRTypeList = birType.getBirs();
			photoBytes = getPhotoByTypeAndSubType(bIRTypeList, type, subType);
		}
//		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
//				"CbeffToBiometricUtil::getImageBytes()::exit");

		return photoBytes;
	}
	
	/**
	 * Gets the photo by type and sub type.
	 *
	 * @param bIRTypeList the b IR type list
	 * @param type        the type
	 * @param subType     the sub type
	 * @return the photo by type and sub type
	 */
	private byte[] getPhotoByTypeAndSubType(List<BIR> bIRList, String type, List<String> subType) {
		byte[] photoBytes = null;
		for (BIR bir : bIRList) {
			if (bir.getBdbInfo() != null) {
				List<BiometricType> singleTypeList = bir.getBdbInfo().getType();
				List<String> subTypeList = bir.getBdbInfo().getSubtype();

				boolean isType = isBiometricType(type, singleTypeList);
				boolean isSubType = isSubType(subType, subTypeList);

				if (isType && isSubType) {
					photoBytes = bir.getBdb();
					break;
				}
			}
		}
		return photoBytes;
	}
	private boolean isBiometricType(String type, List<BiometricType> biometricTypeList) {
		boolean isType = false;
		for (BiometricType biometricType : biometricTypeList) {
			if (biometricType.value().equalsIgnoreCase(type)) {
				isType = true;
			}
		}
		return isType;
	}
	/**
	 * Checks if is sub type.
	 *
	 * @param subType     the sub type
	 * @param subTypeList the sub type list
	 * @return true, if is sub type
	 */
	private boolean isSubType(List<String> subType, List<String> subTypeList) {
		return subTypeList.equals(subType) ? Boolean.TRUE : Boolean.FALSE;
	}

}
