package in.tf.nira.manual.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Pagination {

	private int pageStart;

	private int pageFetch = 10;

}
