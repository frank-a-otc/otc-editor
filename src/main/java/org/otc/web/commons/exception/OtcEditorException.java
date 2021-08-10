/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otc.web.commons.exception;

import org.otcframework.common.exception.OtcException;

public class OtcEditorException extends OtcException {

	private static final long serialVersionUID = 1773421021619538045L;

	public OtcEditorException(String errorCode) {
		super(errorCode);
	}

	public OtcEditorException(String errorCode, String msg) {
		super(errorCode, msg);
	}

	public OtcEditorException(Throwable cause) {
		super(cause);
	}
	
	public OtcEditorException(String errorCode, Throwable cause) {
		super(errorCode, cause);
	}
	
	public OtcEditorException(String errorCode, String msg, Throwable cause) {
		super(errorCode, msg, cause);
	}
}
