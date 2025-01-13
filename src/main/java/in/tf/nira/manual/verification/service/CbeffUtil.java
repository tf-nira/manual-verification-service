package in.tf.nira.manual.verification.service;

import java.util.List;

import in.tf.nira.manual.verification.entity.BIR;

public interface CbeffUtil {

	/**
	 * Method used for getting list of BIR from XML bytes	 *
	 * @param xmlBytes byte array of XML data
	 * @return List of BIR data extracted from XML
	 * @throws Exception Exception
	 */
	List<BIR> getBIRDataFromXML(byte[] xmlBytes) throws Exception;

}
