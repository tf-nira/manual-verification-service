package in.tf.nira.manual.verification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.tf.nira.manual.verification.dto.ApplicationDetailsResponse;
import in.tf.nira.manual.verification.dto.AuthenticationRequest;
import in.tf.nira.manual.verification.dto.AuthenticationResponse;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;
import in.tf.nira.manual.verification.dto.UserResponse;
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;
import in.tf.nira.manual.verification.service.AuthService;
import io.mosip.kernel.core.http.ResponseWrapper;
import in.tf.nira.manual.verification.service.ApplicationService;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    ApplicationService applicationService;

    @PostMapping
    public ResponseWrapper<AuthenticationResponse> createApplication(@Valid @RequestBody CreateAppRequestDTO verifyRequest) throws Exception {
        ResponseWrapper<AuthenticationResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
    	try {
			responseWrapper.setResponse(applicationService.createApplication(verifyRequest));
		} catch (Exception e) {
			responseWrapper.setErrors(null);
		}
    	
    	return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetApplicationsForUser())")
    @GetMapping("/user/{userId}")
    public ResponseWrapper<List<UserApplicationsResponse>> getApplicationsForUser(@PathVariable String userId) throws Exception {
    	ResponseWrapper<List<UserApplicationsResponse>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
		responseWrapper.setResponse(applicationService.getApplicationsForUser(userId));
        return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetApplicationDetails())")
    @GetMapping("/{applicationId}")
    public ResponseWrapper<ApplicationDetailsResponse> getApplicationDetails(@PathVariable String applicationId) {
    	ResponseWrapper<ApplicationDetailsResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
    	try {
			responseWrapper.setResponse(applicationService.getApplicationDetails(applicationId));
		} catch (Exception e) {
			responseWrapper.setErrors(null);
		}
    	
    	return responseWrapper;
    }
    
    @PreAuthorize("hasAnyRole(@authorizedRoles.getUpdateApplicationStatus())")
    @PutMapping("/{applicationId}/status")
    public ResponseWrapper<AuthenticationResponse> updateApplicationStatus(@PathVariable String applicationId, @Valid @RequestBody UpdateStatusRequest request) {
        ResponseWrapper<AuthenticationResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
    	try {
			responseWrapper.setResponse(applicationService.updateApplicationStatus(applicationId, request));
		} catch (Exception e) {
			responseWrapper.setErrors(null);
		}
    	
    	return responseWrapper;
    }
}
