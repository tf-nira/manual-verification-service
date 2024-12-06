package in.tf.nira.manual.verification.dto;

import java.util.List;

import lombok.Data;

@Data
public class SearchFilter {
	private String value;
	
	private List<String> values;

	// @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$",
	// message = "Invalid date time pattern")
	private String fromValue;

	// @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$",
	// message = "Invalid date time pattern")
	private String toValue;

	// @NotBlank
	private String columnName;

	// @NotNull
	private String type;

}
