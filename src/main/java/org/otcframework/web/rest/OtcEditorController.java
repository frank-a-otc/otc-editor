/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.otcframework.common.OtcConstants.TARGET_SOURCE;
import org.otcframework.common.dto.otc.OtcFileDto;
import org.otcframework.common.exception.OtcException;
import org.otcframework.common.exception.OtcUnsupportedJdkException;
import org.otcframework.common.util.OtcUtils;
import org.otcframework.web.CompilerTest;
import org.otcframework.web.commons.dto.ClassMetadataDto;
import org.otcframework.web.commons.exception.OtcEditorException;
import org.otcframework.web.commons.service.OtcEditorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller
public class OtcEditorController {

	private static Logger LOGGER = LoggerFactory.getLogger(OtcEditorController.class);
	
	@Autowired
	private OtcEditorService otcEditorService;

	public static final String URL_SHOW_TYPES ="/getTypes";
	public static final String URL_GET_SOURCE_TREE ="/getSourceTree";
	public static final String URL_GET_TARGET_TREE ="/getTargetTree";
	public static final String URL_GET_TREE ="/getTree";
	public static final String URL_CREATE_OTCFILE ="/createOtcFile";
	public static final String URL_FLIP_OTC ="/flipOtc";
	public static final String COMPILE ="/compile";
	public static CompilerTest compilerTest = new CompilerTest();

	@GetMapping(value=URL_SHOW_TYPES, produces={"application/json;charset=UTF-8"})
	public <T> ResponseEntity<T> getFullyQualifiedNames(@RequestParam(name = "pkgName") String pkgName) {
		Set<String> clsNames = null;
		try {
			clsNames = otcEditorService.findTypeNamesInPackage(pkgName);
			if (clsNames == null) {
				return (ResponseEntity<T>) ResponseEntity.unprocessableEntity().body(
						String.format("Could not find types for package '%s'", pkgName));
			}
			return (ResponseEntity<T>) ResponseEntity.ok(clsNames);
		} catch (Exception e) {
			return (ResponseEntity<T>) ResponseEntity.unprocessableEntity().body(e.getMessage());
		}
	}

	@GetMapping(value=URL_GET_SOURCE_TREE)
	public <T> ResponseEntity<T> getSourceTree(@RequestParam(name = "srcClsName") String srcClsName) {
		return getTreeEntity(srcClsName, TARGET_SOURCE.SOURCE);
	}

	@GetMapping(value=URL_GET_TARGET_TREE)
	public <T> ResponseEntity<T> getTargetTree(@RequestParam(name = "targetClsName") String targetClsName) {
		return getTreeEntity(targetClsName, TARGET_SOURCE.TARGET);
	}

	@GetMapping(value=URL_GET_TREE)
	public <T> ResponseEntity<T> getBothTrees(@RequestParam(name = "srcClsName")
			String srcClsName, @RequestParam(name = "targetClsName") String targetClsName) {
		Map<String, List<ClassMetadataDto>> mapJsTreeNodes = null;
		try {
			if (srcClsName != null) {
				mapJsTreeNodes = getTree(srcClsName, TARGET_SOURCE.SOURCE);
			}
			if (targetClsName != null) {
				if (mapJsTreeNodes == null) {
					mapJsTreeNodes = getTree(targetClsName, TARGET_SOURCE.TARGET);
				} else {
					mapJsTreeNodes.putAll(getTree(targetClsName, TARGET_SOURCE.TARGET));
				}
			}
			return (ResponseEntity<T>) ResponseEntity.ok(mapJsTreeNodes);
		} catch (OtcException e) {
			return (ResponseEntity<T>) ResponseEntity.unprocessableEntity().body(e.getMessage());
		}
	}

	private <T> ResponseEntity<T> getTreeEntity(String targetClsName, TARGET_SOURCE target_source) {
		try {
			Map<String, List<ClassMetadataDto>> mapJsTreeNodes = getTree(targetClsName, target_source);
			return (ResponseEntity<T>) ResponseEntity.ok(mapJsTreeNodes);
		} catch (OtcException e) {
			return (ResponseEntity<T>) ResponseEntity.unprocessableEntity().body(e.getMessage());
		}
	}

	private Map<String, List<ClassMetadataDto>> getTree(String targetClsName, TARGET_SOURCE target_source) {
		Map<String, List<ClassMetadataDto>> mapJsTreeNodes = new HashMap<>();
		if (!StringUtils.isEmpty(targetClsName)) {
			List<ClassMetadataDto> lstTargetFields = null;
			try {
				lstTargetFields = otcEditorService.createTree(targetClsName, target_source);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				if (e instanceof OtcUnsupportedJdkException) {
					throw e;
				}
				throw new OtcEditorException(e);
			}
			String key = TARGET_SOURCE.TARGET == target_source ? "targetTreeData" : "sourceTreeData";
			mapJsTreeNodes.put(key, lstTargetFields);
		}
		return mapJsTreeNodes;
	}

	@PostMapping(value=URL_CREATE_OTCFILE)
	public void createOtclFile(@RequestParam(name = "otcInstructions") String otcInstructions, HttpServletResponse response) {
		response.setContentType("text/plain");
	    ServletOutputStream out;
		try {
			OtcFileDto otcFileDto = otcEditorService.createOtcFileDto(otcInstructions, false);
			String sourceClsName  = otcFileDto.metadata.objectTypes.source; 
			String targetClsName = otcFileDto.metadata.objectTypes.target;
			String otcFileName = OtcUtils.createOtcFileName(sourceClsName, targetClsName);
		    response.setHeader("Content-Disposition","attachment;filename=" + otcFileName);
			out = response.getOutputStream();
		    out.println(otcInstructions);
		    out.flush();
		    out.close();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	@PostMapping(value=URL_FLIP_OTC)
	public ResponseEntity<String> flipOtcl(@RequestParam(name = "otcInstructions") String otcInstructions) {
		OtcFileDto otcFileDto = otcEditorService.createOtcFileDto(otcInstructions, true);
		try {
			otcInstructions = otcEditorService.createYaml(otcFileDto);
		} catch (Exception e) {
			return ResponseEntity.unprocessableEntity().body(e.getMessage());
		}
		return ResponseEntity.ok(otcInstructions);
	}

	@PutMapping(value=COMPILE)
	public ResponseEntity<String> compile() {
		try {
			return ResponseEntity.ok(compilerTest.compile());
		} catch (Exception e) {
			return ResponseEntity.unprocessableEntity().body(e.getMessage());
		}
	}
}
