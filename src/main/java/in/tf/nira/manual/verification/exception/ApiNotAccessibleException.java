package in.tf.nira.manual.verification.exception;

import in.tf.nira.manual.verification.constant.ErrorCode;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ApiNotAccessibleException extends BaseUncheckedException {

    public ApiNotAccessibleException() {
        super(ErrorCode.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
        		ErrorCode.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage());
    }

    public ApiNotAccessibleException(String message) {
        super(ErrorCode.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
                message);
    }

    public ApiNotAccessibleException(Throwable e) {
        super(ErrorCode.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
        		ErrorCode.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage(), e);
    }

    public ApiNotAccessibleException(String errorMessage, Throwable t) {
        super(ErrorCode.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(), errorMessage, t);
    }


}
