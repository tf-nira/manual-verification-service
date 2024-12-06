package in.tf.nira.manual.verification.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.tf.nira.manual.verification.constant.CommonConstants;
import in.tf.nira.manual.verification.dto.ApplicationDetailsResponse;
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.dto.DocumentDTO;
import in.tf.nira.manual.verification.dto.PageResponseDto;
import in.tf.nira.manual.verification.dto.SchInterviewDTO;
import in.tf.nira.manual.verification.dto.SearchDto;
import in.tf.nira.manual.verification.dto.StatusResponseDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;
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
    public ResponseWrapper<StatusResponseDTO> createApplication(@Valid @RequestBody CreateAppRequestDTO verifyRequest) throws Exception {
        ResponseWrapper<StatusResponseDTO> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.CREATE_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		responseWrapper.setResponse(applicationService.createApplication(verifyRequest));
    	
    	return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetApplicationsForUser())")
    @GetMapping("/user/{userId}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
    public ResponseWrapper<List<UserApplicationsResponse>> getApplicationsForUser(@PathVariable String userId) throws Exception {
    	ResponseWrapper<List<UserApplicationsResponse>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(CommonConstants.GET_USER_APP_ID);
		responseWrapper.setVersion(CommonConstants.VERSION);
		responseWrapper.setResponse(applicationService.getApplicationsForUser(userId));
		
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
		responseWrapper.setResponse(applicationService.searchApplications(request.getRequest()));
		
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
		responseWrapper.setResponse(applicationService.getApplicationDetails(applicationId));
    	
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
		responseWrapper.setResponse(applicationService.updateApplicationStatus(applicationId, request.getRequest()));
    	
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
		responseWrapper.setResponse(applicationService.scheduleInterview(applicationId, request.getRequest()));
    	
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
		responseWrapper.setResponse(applicationService.uploadDocuments(applicationId, request.getRequest()));
    	
    	return responseWrapper;
    }
}
