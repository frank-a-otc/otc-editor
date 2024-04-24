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
package org.otcframework.web;

import org.otcframework.common.compiler.CompilationReport;
import org.otcframework.common.config.OtcConfig;
import org.otcframework.common.util.OtcUtils;
import org.otcframework.compiler.OtcsCompiler;
import org.otcframework.compiler.OtcsCompilerImpl;
import org.otcframework.web.commons.exception.OtcEditorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class CompilerUtil {

	private static final String UNIT_TEST_LOCATION = OtcConfig.getUnitTestDirectoryPath();
	private static final String GENERATED_SOURCE_CODE_LOCATION = OtcConfig.getSourceCodeDirectoryPath();

	/** The Constant otcsCompiler. */
	private static final OtcsCompiler otcsCompiler = OtcsCompilerImpl.getInstance();

	public String compile() {
		cleanupGeneratedFiles();
		// -- compile script and generate source code
		List<CompilationReport> compilationReports = otcsCompiler.compileOtcsFiles();
		if (compilationReports == null || compilationReports.isEmpty()) {
			return String.format("No OTC Scripts to compile in '%s'", UNIT_TEST_LOCATION);
		}
		return String.format("Successfully compiled all OTC Scripts in <br/><br/>'%s'<br/><br/> and generated " +
						"source-code files in <br/><br/>'%s'", UNIT_TEST_LOCATION, GENERATED_SOURCE_CODE_LOCATION);
	}

	private void cleanupGeneratedFiles() {
		if (!OtcConfig.isDefaultLocations() || !OtcConfig.getCleanupBeforeCompile()) {
			return;
		}
		OtcUtils.deleteFileOrFolder(OtcConfig.getOtcTmdDirectoryPath());
		OtcUtils.creteDirectory(OtcConfig.getOtcTmdDirectoryPath());

		OtcUtils.deleteFileOrFolder(GENERATED_SOURCE_CODE_LOCATION);
		OtcUtils.creteDirectory(GENERATED_SOURCE_CODE_LOCATION);
	}
}
