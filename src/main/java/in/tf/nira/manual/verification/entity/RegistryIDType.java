package in.tf.nira.manual.verification.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryIDType", propOrder = { "organization", "type" })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryIDType {
	@XmlElement(name = "Organization", required = true)
	protected String organization;
	@XmlElement(name = "Type", required = true)
	protected String type;

	/**
	 * Gets the value of the organization property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Sets the value of the organization property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setOrganization(String value) {
		this.organization = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setType(String value) {
		this.type = value;
	}

}
