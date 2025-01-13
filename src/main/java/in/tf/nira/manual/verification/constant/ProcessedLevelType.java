package in.tf.nira.manual.verification.constant;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ProcessedLevelType")
@XmlEnum
public enum ProcessedLevelType implements Serializable{

	@XmlEnumValue("Raw")
	RAW("Raw"),
	@XmlEnumValue("Intermediate")
	INTERMEDIATE("Intermediate"),
	@XmlEnumValue("Processed")
	PROCESSED("Processed");

	private final String value;

	ProcessedLevelType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ProcessedLevelType fromValue(String v) {
		for (ProcessedLevelType c : ProcessedLevelType.values()) {
			if (c.value.equalsIgnoreCase(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
