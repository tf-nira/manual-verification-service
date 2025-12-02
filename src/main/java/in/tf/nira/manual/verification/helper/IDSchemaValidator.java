package in.tf.nira.manual.verification.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class IDSchemaValidator {

    public static void validate(Map<String, String> stringFields, double schemaVersion, String schemaJson)
            throws ValidationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> identityMap = new HashMap<>();

            // Convert Map<String,String> to proper types
            for (Map.Entry<String, String> entry : stringFields.entrySet()) {
                String key = entry.getKey();
                String valueStr = entry.getValue();

                try {
                    JsonNode node = mapper.readTree(valueStr);
                    identityMap.put(key, mapper.convertValue(node, Object.class));
                } catch (Exception e) {
                    if ("true".equalsIgnoreCase(valueStr) || "false".equalsIgnoreCase(valueStr)) {
                        identityMap.put(key, Boolean.valueOf(valueStr));
                    } else {
                        try {
                            identityMap.put(key, Double.valueOf(valueStr));
                        } catch (NumberFormatException nfe) {
                            identityMap.put(key, valueStr);
                        }
                    }
                }
            }

            // Add required field
            identityMap.put("IDSchemaVersion", Double.valueOf(schemaVersion));

            // Wrap under "identity"
            Map<String, Object> rootMap = new HashMap<>();
            rootMap.put("identity", identityMap);

            // Convert to JSONObject for Everit
            JSONObject jsonObject = new JSONObject(mapper.writeValueAsString(rootMap));

            // Load schema
            JSONObject rawSchema = new JSONObject(schemaJson);
            Schema schema = SchemaLoader.load(rawSchema);

            // Validate structure
            schema.validate(jsonObject); // if invalid, throws ValidationException

            // --- Generic regex validation for all fields ---
            JSONObject identitySchema = rawSchema.getJSONObject("properties")
                    .getJSONObject("identity").getJSONObject("properties");

            for (String field : stringFields.keySet()) {
                JSONObject fieldSchema = identitySchema.optJSONObject(field);
                if (fieldSchema == null) continue;

                if (fieldSchema.has("validators")) {
                    JSONArray validators = fieldSchema.getJSONArray("validators");

                    for (int i = 0; i < validators.length(); i++) {
                        JSONObject validator = validators.getJSONObject(i);

                        if ("regex".equalsIgnoreCase(validator.getString("type"))) {
                            String regex = validator.getString("validator");
                            Pattern pattern = Pattern.compile(regex);

                            Object fieldValue = identityMap.get(field);
                            if (fieldValue == null) continue;

                            if (fieldValue instanceof Iterable) {
                                for (Object item : (Iterable<?>) fieldValue) {
                                    if (item instanceof Map) {
                                        Object valueField = ((Map<?, ?>) item).get("value");
                                        if (valueField instanceof String) {
                                            if (!pattern.matcher((String) valueField).matches()) {
                                                throw new ValidationException(schema,
                                                        "Field '" + field + "' regex failed: " + valueField);
                                            }
                                        }
                                    }
                                }
                            } else if (fieldValue instanceof String) {
                                if (!pattern.matcher((String) fieldValue).matches()) {
                                    throw new ValidationException(schema,
                                            "Field '" + field + "' regex failed: " + fieldValue);
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("All regex validations passed");

        } catch (ValidationException e) {
            // ✅ Instead of swallowing, rethrow it to the caller
            throw e;
        } catch (Exception ex) {
            // Wrap other exceptions as runtime exceptions or log and rethrow
            throw new RuntimeException("Unexpected error during schema validation", ex);
        }
    }
}
