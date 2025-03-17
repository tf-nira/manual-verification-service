package in.tf.nira.manual.verification.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacketDocumentRequestDto {
    private String id;
    private String documentName;
    private String source;
    private String process;
}
