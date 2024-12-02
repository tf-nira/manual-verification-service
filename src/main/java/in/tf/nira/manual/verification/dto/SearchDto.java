package in.tf.nira.manual.verification.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SearchDto {

	private List<SearchFilter> filters;

	private List<SearchSort> sort;

	private Pagination pagination;
}
