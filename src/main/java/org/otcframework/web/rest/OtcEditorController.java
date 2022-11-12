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
import org.otcframework.common.util.OtcUtils;
import org.otcframework.web.commons.dto.ClassMetadataDto;
import org.otcframework.web.commons.service.OtcEditorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

	@GetMapping(value=URL_SHOW_TYPES, produces={"application/json;charset=UTF-8"})
	public @ResponseBody Set<String> getFullyQualifiedNames(@RequestParam(name = "pkgName") String pkgName) {
		Set<String> clsNames = otcEditorService.findTypeNamesInPackage(pkgName);
		return clsNames;
	}

	@GetMapping(value=URL_GET_SOURCE_TREE)
	public @ResponseBody Map<String, List<ClassMetadataDto>> getSourceTree(@RequestParam(name = "srcClsName")
			String srcClsName) {
		Map<String, List<ClassMetadataDto>> mapJsTreeNodes = new HashMap<>();
		if (!StringUtils.isEmpty(srcClsName)) { 
			List<ClassMetadataDto> lstSrcFields = otcEditorService.createTree(srcClsName,
					TARGET_SOURCE.SOURCE);
			mapJsTreeNodes.put("sourceFieldNames", lstSrcFields);
		}
		return mapJsTreeNodes;
	}

	@GetMapping(value=URL_GET_TARGET_TREE)
	public @ResponseBody Map<String, List<ClassMetadataDto>> getTargetTree(@RequestParam(name = "targetClsName")
			String targetClsName) {
		Map<String, List<ClassMetadataDto>> mapJsTreeNodes = new HashMap<>();
		if (!StringUtils.isEmpty(targetClsName)) { 
			List<ClassMetadataDto> lstTargetFields = otcEditorService.createTree(targetClsName,
					TARGET_SOURCE.TARGET);
			mapJsTreeNodes.put("targetFieldNames", lstTargetFields);
		}
		return mapJsTreeNodes;
	}

	@GetMapping(value=URL_GET_TREE)
	public @ResponseBody Map<String, List<ClassMetadataDto>> getTree(@RequestParam(name = "srcClsName")
			String srcClsName, @RequestParam(name = "targetClsName") String targetClsName) {
		Map<String, List<ClassMetadataDto>> mapJsTreeNodes = null;
		if (srcClsName != null) {
			mapJsTreeNodes = getSourceTree(srcClsName);
		}
		if (targetClsName != null) {
			if (mapJsTreeNodes == null) {
				mapJsTreeNodes = getTargetTree(targetClsName);
			} else {
				mapJsTreeNodes.putAll(getTargetTree(targetClsName));
			}
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
        return;
	}

	@PostMapping(value=URL_FLIP_OTC)
	public @ResponseBody String flipOtcl(@RequestParam(name = "otcInstructions") String otcInstructions) {
		OtcFileDto otcFileDto = otcEditorService.createOtcFileDto(otcInstructions, true);
		otcInstructions = otcEditorService.createYaml(otcFileDto);
		return otcInstructions;
	}

}
