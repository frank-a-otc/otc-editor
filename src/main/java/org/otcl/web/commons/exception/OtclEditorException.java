/**
* Copyright (c) otcl2.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.commons.exception;

import org.otcl.common.exception.OtclException;

public class OtclEditorException extends OtclException {

	private static final long serialVersionUID = 1773421021619538045L;

	public OtclEditorException(String errorCode) {
		super(errorCode);
	}

	public OtclEditorException(String errorCode, String msg) {
		super(errorCode, msg);
	}

	public OtclEditorException(Throwable cause) {
		super(cause);
	}
	
	public OtclEditorException(String errorCode, Throwable cause) {
		super(errorCode, cause);
	}
	
	public OtclEditorException(String errorCode, String msg, Throwable cause) {
		super(errorCode, msg, cause);
	}
}
