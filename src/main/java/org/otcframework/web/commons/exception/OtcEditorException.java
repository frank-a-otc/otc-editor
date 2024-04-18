/**
 * Copyright (c) otcframework.org
 *
 * @author  Franklin J Abel
 * @version 1.0
 * @since   2020-06-08
 *
 * This file is part of the OTC framework.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.otcframework.web.commons.exception;

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
