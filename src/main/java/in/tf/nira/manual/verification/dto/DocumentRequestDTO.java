package in.tf.nira.manual.verification.dto;


import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequestDTO {
    @NotBlank
    private String id;
    
    @NotEmpty
    private List<String> documentNames;
    
    @NotBlank
    private String source;
    
    @NotBlank
    private String process;
}