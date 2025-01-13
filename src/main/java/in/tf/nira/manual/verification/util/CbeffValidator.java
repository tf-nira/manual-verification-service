package in.tf.nira.manual.verification.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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
		String decodedString = new String(fileBytes, StandardCharsets.UTF_8);
		System.out.println("Decoded XML: ");
		System.out.println(decodedString);

		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		 // Set up namespace-aware SAX parser
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    
	    // Add a namespace filter
	    NamespaceFilter namespaceFilter = new NamespaceFilter("http://standards.iso.org/iso-iec/19785/-3/ed-2/");
	    namespaceFilter.setParent(xmlReader);

	    InputSource inputSource = new InputSource(new ByteArrayInputStream(fileBytes));
	    SAXSource saxSource = new SAXSource(namespaceFilter, inputSource);
	    
	    JAXBElement<BIR> jaxBir = unmarshaller.unmarshal(saxSource, BIR.class);
	    BIR bir = jaxBir.getValue();
	    
//		JAXBElement<BIR> jaxBir = unmarshaller
//				.unmarshal(new StreamSource(new ByteArrayInputStream(fileBytes)), BIR.class);
//		BIR bir = jaxBir.getValue();
		return bir;
	}

}
