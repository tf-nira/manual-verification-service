package in.tf.nira.manual.verification.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonDeserialize(builder = VersionType.VersionTypeBuilder.class)
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersionType", propOrder = { "major", "minor" })
public class VersionType {
	@XmlElement(name = "Major")
	@XmlSchemaType(name = "unsignedInt")
	private int major;
	@XmlElement(name = "Minor")
	@XmlSchemaType(name = "unsignedInt")
	private int minor;
	
	public VersionType(VersionTypeBuilder versionTypeBuilder) {
		this.major = versionTypeBuilder.major;
		this.minor = versionTypeBuilder.minor;
	}
	public static class VersionTypeBuilder {
		private int major;
		private int minor;

		public VersionTypeBuilder withMinor(int minor) {
			this.minor = minor;
			return this;
		}

		public VersionTypeBuilder withMajor(int major) {
			this.major = major;
			return this;
		}

		public VersionType build() {
			return new VersionType(this);
		}
	}
}
