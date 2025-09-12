package in.tf.nira.manual.verification.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "service")
public class ServiceProperties {
	
	private Map<String, String> map = new HashMap<>();
	
	public Map<String, String> getMap() {
		return map;
	}
	
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
	public String toDisplay(String code) {
		return map.get(code);
	}
	
	public String toCode(String display) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue().equals(display))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}
}
