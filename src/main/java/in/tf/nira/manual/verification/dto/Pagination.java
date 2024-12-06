package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class Pagination {

	private int pageStart;

	private int pageFetch = 10;

}
