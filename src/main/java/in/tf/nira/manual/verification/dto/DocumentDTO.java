package in.tf.nira.manual.verification.dto;

import java.util.Map;

import lombok.Data;

@Data
public class DocumentDTO {
	private Map<String, Document> documents;
}
