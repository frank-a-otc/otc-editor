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

import java.util.List;

public class CompilerUtil {

	private static final String UNIT_TEST_LOCATION = OtcConfig.getUnitTestLocation();
	private static final String GENERATED_SOURCE_CODE_LOCATION = OtcConfig.getSourceCodeLocation();

	/** The Constant otclCompiler. */
	private static final OtcsCompiler otcsCompiler = OtcsCompilerImpl.getInstance();

	public String compile() {
		cleanupGeneratedFiles();
		// -- compile script and generate source code
		List<CompilationReport> compilationReports = otcsCompiler.compileOtcsFiles();
		if (compilationReports == null || compilationReports.isEmpty()) {
			return String.format("No OTC Scripts to compile in '%s'", UNIT_TEST_LOCATION);
		}
		return String.format("Successfully compiled all OTC Scripts in '%s' and generated source-code files in '%s'",
				UNIT_TEST_LOCATION, GENERATED_SOURCE_CODE_LOCATION);
	}

	private void cleanupGeneratedFiles() {
		if (!OtcConfig.isDefaultLocations() || !OtcConfig.getCleanupBeforeCompile()) {
			return;
		}
		OtcUtils.deleteRecursive(OtcConfig.getOtcTmdLocation());
		OtcUtils.creteDirectory(OtcConfig.getOtcTmdLocation());

		OtcUtils.deleteRecursive(OtcConfig.getSourceCodeLocation());
		OtcUtils.creteDirectory(OtcConfig.getSourceCodeLocation());

		OtcUtils.deleteRecursive(OtcConfig.getTargetLocation());
		OtcUtils.creteDirectory(OtcConfig.getTargetLocation());
	}

//	private void deleteRecursive(File folder) {
//		if (!folder.isDirectory()) {
//			return;
//		}
//		File[] allContents = folder.listFiles();
//		try {
//			if (allContents != null) {
//				for (File file : allContents) {
//					if (file.isDirectory()) {
//						deleteRecursive(file);
//					} else {
//						Files.delete(file.toPath());
//					}
//				}
//			}
//			Files.delete(folder.toPath());
//		} catch (IOException e) {
//			throw new OtcEditorException(e);
//		}
//	}
}
