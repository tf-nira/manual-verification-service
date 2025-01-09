package in.tf.nira.manual.verification.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import in.tf.nira.manual.verification.constant.BiometricType;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
//@JsonDeserialize(builder = BDBInfo.BDBInfoBuilder.class)
public class BDBInfo {
	@XmlList
	@XmlElement(name = "Type")
	private List<BiometricType> type;
	@XmlList
	@XmlElement(name = "Subtype")
	private List<String> subtype;
}
