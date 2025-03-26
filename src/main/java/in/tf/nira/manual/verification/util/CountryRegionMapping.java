package in.tf.nira.manual.verification.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.repository.CountryRegionMappingRepo;

@Component
public class CountryRegionMapping {
	private static final Logger logger = LoggerFactory.getLogger(CountryRegionMapping.class);
	
	@Autowired
	private CountryRegionMappingRepo countryRegionMappingRepo;
	
	/**
     * Determines the region for a given country based on database mapping.
     * Used for international officer assignment.
     * 
     * @param country The country name to lookup
     * @return The region name
     */
	public String getRegionForCountry(String country) {
		if (country == null || country.trim().isEmpty()) {
	        logger.warn("Empty country name provided for region determination");
	        throw new RequestException(ErrorCode.INVALID_COUNTRY_NAME.getErrorCode(), 
	                                 ErrorCode.INVALID_COUNTRY_NAME.getErrorMessage());
	    }
		try {
			return countryRegionMappingRepo.findRegionByCountryName(country)
					.orElseThrow(() -> {
				        logger.error("No region mapping found for country: {}", country);
				        // Remove the "return" keyword here
				        throw new RequestException(ErrorCode.REGION_NOT_FOUND.getErrorCode(),
				            String.format(ErrorCode.REGION_NOT_FOUND.getErrorMessage(), country));
				    });
		}catch(Exception e) {
			logger.error("Error retrieving region for country '{}': {}", country, e.getMessage());
			throw new RequestException(ErrorCode.REGION_LOOKUP_ERROR.getErrorCode(),
	                ErrorCode.REGION_LOOKUP_ERROR.getErrorMessage());
		}
	}
}
