package in.tf.nira.manual.verification.dto;

import java.util.List;

import javax.validation.Valid;

import lombok.Data;

@Data
public class PageResponseDto<T> {
	private long fromRecord;
	private long toRecord;
	private long totalRecord;
	@Valid
	private List<T> data;
}
