package in.tf.nira.manual.verification.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import in.tf.nira.manual.verification.service.CbeffUtil;
import in.tf.nira.manual.verification.util.CbeffValidator;
import in.tf.nira.manual.verification.entity.BIR;

@Component
public class CbeffImpl implements CbeffUtil {
	/**
	 * Method used for getting list of BIR from XML bytes	 *
	 * @param xmlBytes byte array of XML data
	 * @return List of BIR data extracted from XML
	 * @throws Exception Exception
	 */
	@Override
	public List<BIR> getBIRDataFromXML(byte[] xmlBytes) throws Exception {
		BIR bir = CbeffValidator.getBIRFromXML(xmlBytes);
		return bir.getBirs();
	}
}
