package in.tf.nira.manual.verification.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.dto.TemplateResponseDto;
import in.tf.nira.manual.verification.exception.RequestException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.templatemanager.velocity.impl.TemplateManagerImpl;

@Component
public class TemplateGenerator {
	private static Logger logger = LoggerFactory.getLogger(TemplateGenerator.class);

	/** The resource loader. */
	private String resourceLoader = "classpath";

	/** The template path. */
	private String templatePath = ".";

	/** The cache. */
	private boolean cache = Boolean.TRUE;

	/** The default encoding. */
	private String defaultEncoding = StandardCharsets.UTF_8.name();

	@Autowired(required = true)
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@Value("${manual.verification.get.template.url}")
	private String templateUrl;

	/**
	 * Gets the template.
	 *
	 * @param templateTypeCode
	 *            the template type code
	 * @param attributes
	 *            the attributes
	 * @param langCode
	 *            the lang code
	 * @return the template
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 */
	public InputStream getTemplate(String templateTypeCode, Map<String, Object> attributes, String langCode)
			throws IOException {
		TemplateResponseDto template;

		try {
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(templateUrl);
			uriComponentsBuilder.pathSegment(langCode, templateTypeCode);
			String url = uriComponentsBuilder.build().toUriString();
			
			HttpHeaders headers = new HttpHeaders();
	        headers.set("Content-Type", "application/json");
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        
			ResponseEntity<ResponseWrapper<TemplateResponseDto>> responseEntity = restTemplate.exchange(
					url,
	                HttpMethod.GET,
	                entity,
	                new ParameterizedTypeReference<ResponseWrapper<TemplateResponseDto>>() {}
	        );
			
			if (responseEntity.getBody() == null) {
	            logger.error("Failed to get notification template. Status code: " + responseEntity.getStatusCodeValue());
	            throw new RequestException(ErrorCode.GET_TEMPLATE_FAILED.getErrorCode(),
						ErrorCode.GET_TEMPLATE_FAILED.getErrorMessage() + "with status code: " + responseEntity.getStatusCodeValue());
	        }

	        ResponseWrapper<TemplateResponseDto> responseWrapper = responseEntity.getBody();

	        if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
	        	logger.error("Get template error: {}", responseWrapper.getErrors().get(0));
	            throw new RequestException(ErrorCode.FAILED_GET_TEMPLATE_RESPONSE.getErrorCode(),
						ErrorCode.FAILED_GET_TEMPLATE_RESPONSE.getErrorMessage() + "with error: " + responseWrapper.getErrors().get(0));
	        }
			
			template = responseWrapper.getResponse();

			InputStream fileTextStream = null;
			if (template != null) {
				InputStream stream = new ByteArrayInputStream(
						template.getTemplates().iterator().next().getFileText().getBytes());
				fileTextStream = getTemplateManager().merge(stream, attributes);
			}
			
			return fileTextStream;

		} catch (Exception e) {
			throw new RequestException(ErrorCode.FAILED_GET_TEMPLATE_RESPONSE.getErrorCode(),
					ErrorCode.FAILED_GET_TEMPLATE_RESPONSE.getErrorMessage() + "with error: " + e.getMessage());
		}
	}

	/**
	 * Gets the template manager.
	 *
	 * @return the template manager
	 */
	public TemplateManager getTemplateManager() {
		final Properties properties = new Properties();
		properties.put(RuntimeConstants.INPUT_ENCODING, defaultEncoding);
		properties.put(RuntimeConstants.OUTPUT_ENCODING, defaultEncoding);
		properties.put(RuntimeConstants.ENCODING_DEFAULT, defaultEncoding);
		properties.put(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
		properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatePath);
		properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, cache);
		properties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
		properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		properties.put("file.resource.loader.class", FileResourceLoader.class.getName());
		VelocityEngine engine = new VelocityEngine(properties);
		engine.init();
		return new TemplateManagerImpl(engine);
	}
}
