package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class TemplateDto {
	private String id;
	private String name;
	private String description;
	private String fileFormatCode;
	private String model;
	private String fileText;
	private String moduleId;
	private String moduleName;
	private String templateTypeCode;
	private String langCode;
	private Boolean isActive;
}
