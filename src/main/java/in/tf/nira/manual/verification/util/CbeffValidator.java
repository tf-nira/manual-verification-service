package in.tf.nira.manual.verification.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import javax.xml.transform.stream.StreamSource;

import in.tf.nira.manual.verification.entity.BIR;

public class CbeffValidator {

	/**
	 * Method used for BIR Type
	 * 
	 * @param fileBytes byte array of XML data
	 * 
	 * @return BIR BIR data
	 * 
	 * @exception Exception exception
	 * 
	 */
	public static BIR getBIRFromXML(byte[] fileBytes) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement<BIR> jaxBir = unmarshaller
				.unmarshal(new StreamSource(new ByteArrayInputStream(fileBytes)), BIR.class);
		BIR bir = jaxBir.getValue();
		return bir;
	}

}
