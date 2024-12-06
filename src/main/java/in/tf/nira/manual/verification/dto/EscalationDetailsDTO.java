package in.tf.nira.manual.verification.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EscalationDetailsDTO {
	private String level;
	private String category;
	private String comment;
	private LocalDateTime escDTimes;
	private String escBy;
	
	public EscalationDetailsDTO() {}
	
	public EscalationDetailsDTO(EscalationDetailsDTO original) {
		this.level = original.level;
		this.category = original.category;
		this.comment = original.comment;
		this.escDTimes = original.escDTimes;
		this.escBy = original.escBy;
	}
}
