package in.tf.nira.manual.verification.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {
    private List<DocumentInfo> documents;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentInfo {
        private String documentName;
        private byte[] document;
        private String value;
        private String type;
        private String format;
        private String refNumber;
    }
}