package in.tf.nira.manual.verification.dto;

import java.util.List;

import lombok.Data;

@Data
public class SearchDto {

	private List<SearchFilter> filters;

	private List<SearchSort> sort;

	private Pagination pagination;
}
