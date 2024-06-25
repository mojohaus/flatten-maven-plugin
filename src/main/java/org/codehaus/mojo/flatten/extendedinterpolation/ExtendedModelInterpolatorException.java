package org.codehaus.mojo.flatten.extendedinterpolation;

public class ExtendedModelInterpolatorException extends RuntimeException {
    public ExtendedModelInterpolatorException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
