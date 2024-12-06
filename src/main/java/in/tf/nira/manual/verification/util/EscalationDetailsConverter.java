package in.tf.nira.manual.verification.util;

import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.tf.nira.manual.verification.dto.EscalationDetailsDTO;

@Converter
public class EscalationDetailsConverter implements AttributeConverter<List<EscalationDetailsDTO>, String> {

	private static ObjectMapper mapper;

	public static void setObjectMapper(ObjectMapper objectMapper) {
		mapper = objectMapper;
	}

	@Override
	public String convertToDatabaseColumn(List<EscalationDetailsDTO> escalationDetails) {
		try {
			return escalationDetails != null ? mapper.writeValueAsString(escalationDetails) : null;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting to JSON", e);
		}
	}

	@Override
	public List<EscalationDetailsDTO> convertToEntityAttribute(String dbData) {
		try {
			return dbData != null ? mapper.readValue(dbData, new TypeReference<List<EscalationDetailsDTO>>() {
			}) : null;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading JSON", e);
		}
	}
}