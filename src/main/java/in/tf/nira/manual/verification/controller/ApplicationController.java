package in.tf.nira.manual.verification.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import in.tf.nira.manual.verification.config.Config;
import in.tf.nira.manual.verification.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import in.tf.nira.manual.verification.constant.CommonConstants;
import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.service.ApplicationService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    ApplicationService applicationService;
    
    @PostMapping
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<StatusResponseDTO> createApplication(@Valid @RequestBody CreateAppRequestDTO verifyRequest) {
        ResponseWrapper<StatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.CREATE_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.createApplication(verifyRequest));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
    	
    	return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetApplicationsForUser())")
    @GetMapping("/user/{userId}/{rejectFlag}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<List<UserApplicationsResponse>> getApplicationsForUser(@PathVariable String userId, @PathVariable Boolean rejectFlag) {
    	ResponseWrapper<List<UserApplicationsResponse>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.GET_USER_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.getApplicationsForUser(userId, rejectFlag));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
		
        return responseWrapper;
    }

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetRejectedApplication())")
	@GetMapping("/reject/{rejectedAppId}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<RejectedApplicationResponse> getRejectedApplicationById(@PathVariable String rejectedAppId) {
		ResponseWrapper<RejectedApplicationResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.GET_USER_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.getRejectedApplication(rejectedAppId));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}

		return responseWrapper;
	}
    
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getSearchApplications())")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<PageResponseDto<UserApplicationsResponse>> searchApplications(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<UserApplicationsResponse>> responseWrapper = new ResponseWrapper<>();
		try {
			responseWrapper.setResponse(applicationService.searchApplications(request.getRequest()));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
		
		return responseWrapper;
	}
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetApplicationDetails())")
    @GetMapping("/{applicationId}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<ApplicationDetailsResponse> getApplicationDetails(@PathVariable String applicationId) {
    	ResponseWrapper<ApplicationDetailsResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.GET_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.getApplicationDetails(applicationId));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
    	
    	return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getUpdateApplicationStatus())")
    @PutMapping("/{applicationId}/status")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<StatusResponseDTO> updateApplicationStatus(@PathVariable String applicationId, @Valid @RequestBody RequestWrapper<UpdateStatusRequest> request) {
        ResponseWrapper<StatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.UPDATE_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.updateApplicationStatus(applicationId, request.getRequest()));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
    	
    	return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getScheduleInterview())")
    @PostMapping("/{applicationId}/schedule/interview")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<StatusResponseDTO> scheduleInterview(@PathVariable String applicationId, @Valid @RequestBody RequestWrapper<SchInterviewDTO> request) {
        ResponseWrapper<StatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.SCHEDULE_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.scheduleInterview(applicationId, request.getRequest()));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
    	
    	return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getUploadDocuments())")
    @PostMapping("/{applicationId}/upload/documents")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<StatusResponseDTO> uploadDocuments(@PathVariable String applicationId, @Valid @RequestBody RequestWrapper<DocumentDTO> request) {
        ResponseWrapper<StatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.UPLOAD_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.uploadDocuments(applicationId, request.getRequest()));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
    	
    	return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getFetchDemographicDetails())")
    @GetMapping("/demographics/{nin}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<DemographicDetailsDTO> fetchDemographicDetails(@PathVariable String nin) {
        ResponseWrapper<DemographicDetailsDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.GET_NIN_DEMOGRAPHIC);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.getDemographicDetails(nin));
		} catch (RequestException e) {
			throw e;
		} catch (Exception exc) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(exc.getMessage(), exc.getLocalizedMessage()));
		}
    	return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getFetchUploadedDocForSRO())")
    @PostMapping("/fetch/document")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<DocumentResponseDTO> fetchDocument(@Valid @RequestBody RequestWrapper<DocumentRequestDTO> request) {
        ResponseWrapper<DocumentResponseDTO> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(CommonConstants.FETCH_DOCUMENT_ID);
        responseWrapper.setVersion(CommonConstants.VERSION);
        try {
            responseWrapper.setResponse(applicationService.fetchDocument(request.getRequest()));
        } catch (RequestException e) {
            throw e;
        } catch (Exception exc) {
            throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
                    String.format(exc.getMessage(), exc.getLocalizedMessage()));
        }
        
        return responseWrapper;
    }
    
    @GetMapping("/district-office/{districtName}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<DistrictOfficeResponseDTO> getDistrictOfficeByName (@PathVariable("districtName") String districtName) {
    	ResponseWrapper<DistrictOfficeResponseDTO> responseWrapper = new ResponseWrapper<DistrictOfficeResponseDTO>();
    	responseWrapper.setId(CommonConstants.GET_DISTRICT_OFFICE_ID);
    	responseWrapper.setVersion(CommonConstants.VERSION);
    	try {
    		responseWrapper.setResponse(applicationService.getDistrictOfficeByName(districtName));
    	} catch (RequestException ex) {
    		throw ex;
    	} catch (Exception e) {
    		throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(), 
    				String.format(e.getMessage(), e.getLocalizedMessage()));
    	}
    	return responseWrapper;
    }

	@GetMapping("/get-config")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ConfigResponseDTO> getApplicationConfig () {
		ResponseWrapper<ConfigResponseDTO> responseWrapper = new ResponseWrapper<ConfigResponseDTO>();
		responseWrapper.setId(CommonConstants.GET_CONFIG_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.getApplicationConfig());
		} catch (RequestException ex) {
			throw ex;
		} catch (Exception e) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(e.getMessage(), e.getLocalizedMessage()));
		}
		return responseWrapper;
	}
    
	//@PreAuthorize("hasAnyRole(@authorizedRoles.getFetchDeomgraphicDetails())")
	@GetMapping("/matched-id/demographics/{registrationId}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<DemographicDetailsDTO.Identity> fetchMatchedIdDemographics(@PathVariable String registrationId) {
		ResponseWrapper<DemographicDetailsDTO.Identity> responseWrapper = new ResponseWrapper<DemographicDetailsDTO.Identity>();
		responseWrapper.setId(CommonConstants.GET_MATCHED_ID_DEMOGRAPHICS);
		responseWrapper.setVersion(CommonConstants.VERSION);
		try {
			responseWrapper.setResponse(applicationService.fetchMatchedRegIdDemographics(registrationId));
		} catch (RequestException ex) {
			throw ex;
		} catch (Exception e) {
			throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
					String.format(e.getMessage(), e.getLocalizedMessage()));
		}
		return responseWrapper;
	}
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetApplicationsForUser())")
	@GetMapping("/assignedofficer/{applicationId}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<String> getAssignedOfficer(@PathVariable String applicationId) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		String response=null;
		try {
			response=applicationService.getAssignedOfficerById(applicationId);
			responseWrapper.setResponse(response);
		} catch (Exception exc) {
			throw new RuntimeException("Unexpected error occurred: " + exc.getMessage(), exc);
		}

		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getModifyDemographics())")
    @PutMapping("/{applicationId}/modify_demographics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<StatusResponseDTO> updateDemographics(
            @PathVariable String applicationId,  @RequestBody Map<String, Object> request){
        ResponseWrapper<StatusResponseDTO> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(CommonConstants.UPDATE_APP_ID);
        responseWrapper.setVersion(CommonConstants.VERSION);

        try {
            responseWrapper.setResponse(applicationService.updateDemographics(applicationId, request));
        } catch (RequestException e) {
            throw e;
        } catch (Exception exc) {
            throw new RequestException(ErrorCode.UNKNOWN_ERROR.getErrorCode(),
                    String.format(exc.getMessage(), exc.getLocalizedMessage()));
        }

        return responseWrapper;
    }
}
