package com.hakira.common.exception;

public class ServiceFailExceptionTest {
    ServiceFailException serviceFailException = new ServiceFailException();

    public void testServiceFailException() {
        try {
            try {
                serviceFailException.getErrorCode();
            } catch (Exception e) {
                throw new ServiceUnknownException();
            }
        } catch (ServiceUnknownException e) {
            e.getErrorCode();
        }


    }

}