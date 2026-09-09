package in.tf.nira.manual.verification.util;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import in.tf.nira.manual.verification.dto.AuthUserDetails;

public class UserDetailUtil {
	private static final Logger logger = LoggerFactory.getLogger(UserDetailUtil.class);
	/**
	 * 
	 * @return
	 */
	public static AuthUserDetails getLoggedInUserDetails() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		} else {
			return null;
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static String getLoggedInUserId() {
		AuthUserDetails loggedInUser = getLoggedInUserDetails();
		logger.info("loggedInUser details: {}",loggedInUser);
		if (loggedInUser != null && loggedInUser.getUserId() != null) {
			return loggedInUser.getUserId();
		}

		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())) {
			logger.info("loggedInUser details2: {}",SecurityContextHolder.getContext().getAuthentication().getName());
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}

		return null;
	}
	
	public static String getLoggedInUser() {
		String loggedInUser = getLoggedInUserId();
		if (loggedInUser != null) {
			return loggedInUser;
		}
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())) {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}

		return null;
	}
}