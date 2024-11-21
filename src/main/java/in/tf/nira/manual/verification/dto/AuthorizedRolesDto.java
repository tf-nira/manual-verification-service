package in.tf.nira.manual.verification.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "manual.verification.roles")
@Getter
@Setter
public class AuthorizedRolesDto {
	private List<String> getApplicationsForUser;
	private List<String> getApplicationDetails;
	private List<String> updateApplicationStatus;
}