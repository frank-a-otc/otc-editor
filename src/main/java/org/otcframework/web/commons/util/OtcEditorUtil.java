/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web.commons.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.otcframework.common.OtcConstants;
import org.otcframework.common.OtcConstants.TARGET_SOURCE;
import org.otcframework.common.config.OtcConfig;
import org.otcframework.common.util.CommonUtils;
import org.otcframework.common.util.OtcUtils;
import org.otcframework.common.util.PackagesFilterUtil;
import org.otcframework.web.commons.dto.ClassMetadataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtcEditorUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OtcEditorUtil.class);
	private static final String otcLibLocation = OtcConfig.getOtcLibLocation();
	private static final FileFilter jarFileFilter = CommonUtils.createFilenameFilter(".jar");
	private static Set<String> jarFilesLoaded;
	private static final int recursionDepth = 2;
	private static final String SOURCE_ROOT = "source-root";
	private static final String TARGET_ROOT = "target-root";

	public static Set<String> findTypeNamesInPackage(String basePackage) {
		File otcLibDirectory = new File(otcLibLocation);
//		if (clzLoader == null) {
//			clzLoader = OtcUtils.loadURLClassLoader(otcLibLocation);
//		}
		Set<String> lstClassNames = fetchFileNamesRecursive(otcLibDirectory, jarFileFilter, null, basePackage);
	    return lstClassNames;
	}
	
	private static Set<String> fetchFileNamesRecursive(File directory, FileFilter fileFilter, Set<String> clzNames,
			String basePackage) {
        if (!PackagesFilterUtil.isFilteredPackage(basePackage)) {
        	return null;
        }
		for (File file : directory.listFiles(fileFilter)) {
			if (file.isDirectory()) {
				if (clzNames == null) {
					clzNames = fetchFileNamesRecursive(file, fileFilter, clzNames, basePackage);
				} else {
					clzNames.addAll(fetchFileNamesRecursive(file, fileFilter, clzNames, basePackage));
				}
			} else {
				JarFile jarFile = null;
				try {
					jarFile = new JarFile(file);
					Enumeration<JarEntry> jarEntries = jarFile.entries();
	            	String fileName = file.getAbsolutePath();
	            	boolean isClzFileAlreadyLoaded = false;
					if (jarFilesLoaded != null && jarFilesLoaded.contains(fileName)) {
						isClzFileAlreadyLoaded = true;
					} else {
						if (jarFilesLoaded == null) {
							jarFilesLoaded = new HashSet<>();
						}
						jarFilesLoaded.add(fileName);
					}
					for (JarEntry jarEntry = null; jarEntries.hasMoreElements()
				            && ((jarEntry = jarEntries.nextElement()) != null);) {
				        String clzName = jarEntry.getName();
				        if (!clzName.endsWith(".class")) {
				        	continue;
				        }
				        clzName = clzName.substring(0, clzName.lastIndexOf(".")).replace('/', '.');
				        String pkgName = clzName.substring(0, clzName.lastIndexOf("."));
			            if (!pkgName.equals(basePackage)) {
			            	continue;
			            }
			            if (clzNames != null && clzNames.contains(clzName)) {
			            	continue;
			            }
						if (clzNames == null) {
							clzNames = new TreeSet<>();
						}
		            	clzNames.add(clzName);
		            	if (!isClzFileAlreadyLoaded) {
		            		OtcUtils.loadClass(clzName);
		            	}
					}
				} catch (Exception ex) {
					LOGGER.warn("", ex);
				} finally {
					try {
						jarFile.close();
					} catch (IOException e) {
						LOGGER.warn("Problem closing Jarfile!", e);
					}
				}
			}
		}
		return clzNames;
	}
	
	public static List<ClassMetadataDto> createMembersHierarchy(String clsName, TARGET_SOURCE targetSource) {
//		if (clzLoader == null) {
//			clzLoader = OtcUtils.loadURLClassLoader(otcLibLocation);
//		}
		Class<?> clz = null;
		try {
			clz = OtcUtils.loadClass(clsName);
		} catch (Exception ex) {
			LOGGER.warn(ex.getMessage());
		}
		return createClassMetadataDtos(clz, targetSource);		
	}
	
	private static List<ClassMetadataDto> createClassMetadataDtos(Class<?> clz, TARGET_SOURCE targetSource) {
		List<ClassMetadataDto> membersClassMetadataDtos = createClassMetadataDtos(null, null, clz, null, null,
				recursionDepth, null);
		String txt = clz.getName().concat(" - (right-click for context-menu)");
		ClassMetadataDto classMetadataDto = ClassMetadataDto.newBuilder()
			.addId(TARGET_SOURCE.TARGET == targetSource ? TARGET_ROOT : SOURCE_ROOT)
			.addText(txt)
			.addChildren(membersClassMetadataDtos)
			.build();
		List<ClassMetadataDto> classMetadataDtos = new ArrayList<>();
		classMetadataDtos.add(classMetadataDto);
		return classMetadataDtos;
	}

	private static List<ClassMetadataDto> createClassMetadataDtos(Field field, Class<?> parentClz, Class<?> clz,
			String displayId, Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledOtcChains) {
		if (!PackagesFilterUtil.isFilteredPackage(clz)) {
			return null;
		}
		if (mapRegistry != null) {
			if (mapRegistry.containsKey(clz)) {
				if (recursiveChildrenDepthCount < recursionDepth) {
					return mapRegistry.get(clz);
				}
				if (clz == parentClz) {
					recursiveChildrenDepthCount++;
				}
			}
		} else {
			mapRegistry = new HashMap<>();
			compiledOtcChains = new HashSet<String>();
		}
		List<ClassMetadataDto> classMetadataDtos = new ArrayList<>();
		mapRegistry.put(clz, classMetadataDtos);
		List<ClassMetadataDto> membersClassMetadataDtos = createClassMetadataDtosRecursive(field, clz, 
				mapRegistry, recursiveChildrenDepthCount, compiledOtcChains, displayId);
		if (membersClassMetadataDtos != null) {
			classMetadataDtos.addAll(membersClassMetadataDtos);
		}
		return classMetadataDtos; 
	}
	
	private static List<ClassMetadataDto> createClassMetadataDtosRecursive(Field field, Class<?> clz,  
			Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledIds, String displayId) {
		if (!PackagesFilterUtil.isFilteredPackage(clz)) {
			return null;
		}
		List<ClassMetadataDto> classMetadataDtos = null;
		Map<String, Field> fields = fetchfields(clz, null);
		if (fields == null || fields.size() == 0) {
			return null;
		}
		boolean isParentEnum = clz.isEnum();
		for (Entry<String, Field> entry : fields.entrySet()) {
			field = entry.getValue();
			Class<?> fieldType = field.getType();
			String propName = entry.getKey();
			boolean isEnum = fieldType.isEnum();
			if ((isEnum && isParentEnum) || (isParentEnum && propName.endsWith("$VALUES"))) {
				continue;
			}
			classMetadataDtos = createUiNode(field, clz, isParentEnum, mapRegistry, recursiveChildrenDepthCount,
					compiledIds, displayId, classMetadataDtos);
		}
		
		return classMetadataDtos;
	}
	
	private static List<ClassMetadataDto> createUiNode(Field field, Class<?> clz, boolean isParentEnum, 
			Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledIds, String displayId, List<ClassMetadataDto> classMetadataDtos) {
		Class<?> fieldType = field.getType();
		Type genericType = field.getGenericType();
		String propName = field.getName();
		StringBuilder otcChain = new StringBuilder();
		if (displayId == null) {
			otcChain.append(propName);
		} else {
			otcChain.append(displayId).append(".").append(propName);
		}
		if (Map.class.isAssignableFrom(fieldType)) {
			otcChain.append(OtcConstants.MAP_REF);
		} else if (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
			otcChain.append(OtcConstants.ARR_REF);
		}
		String otcId = otcChain.toString();
		if (compiledIds.contains(otcId)) {
			return classMetadataDtos;
		}
		StringBuilder txtBuilder = new StringBuilder();
		txtBuilder.append(propName);
		compiledIds.add(otcId);
		Class<?> keyType = null;
		Class<?> valueType = null;
		boolean isMap = false;
		ClassMetadataDto.Builder builder = ClassMetadataDto.newBuilder()
				.addId(otcId);
		Class<?> childType = null;
		StringBuilder partialTxtBuilder = new StringBuilder();
		if (Map.class.isAssignableFrom(fieldType)) {
			isMap = true;
			txtBuilder.append(" (").append(fieldType.getName()).append(OtcConstants.MAP_REF).append(")");	 
			builder.addText(txtBuilder.toString());
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
			valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
			String keyRef = otcId + OtcConstants.MAP_KEY_REF;
			String displayText = OtcConstants.MAP_KEY_REF;
			if (keyType.isEnum()) {
				displayText += " (*ENUM: ";
			} else {
				displayText += " (";
			}
			displayText += keyType.getName() + ")"; 
			ClassMetadataDto.Builder keyClassMetadataDtoBuilder = ClassMetadataDto.newBuilder()
					.addId(keyRef)
					.addText(displayText);
			if (PackagesFilterUtil.isFilteredPackage(keyType)) {
				keyClassMetadataDtoBuilder.addChildren(createClassMetadataDtos(field, null, keyType, keyRef, 
						mapRegistry, recursiveChildrenDepthCount, compiledIds));
			}
			builder.addChild(keyClassMetadataDtoBuilder.build());
			String valueRef = otcId + OtcConstants.MAP_VALUE_REF;
			displayText = OtcConstants.MAP_VALUE_REF;
			if (valueType.isEnum()) {
				displayText += " (*ENUM: ";
			} else {
				displayText += " (";
			}
			displayText += valueType.getName() + ")"; 
			ClassMetadataDto.Builder valueClassMetadataDtoBuilder = ClassMetadataDto.newBuilder()
					.addId(valueRef)
					.addText(displayText);
			if (PackagesFilterUtil.isFilteredPackage(valueType)) {
				valueClassMetadataDtoBuilder.addChildren(createClassMetadataDtos(field, null, valueType, valueRef, 
						mapRegistry, recursiveChildrenDepthCount, compiledIds));
			}
			builder.addChild(valueClassMetadataDtoBuilder.build());
		} else if (fieldType.isArray()) {
			txtBuilder.append("[]");
			genericType = fieldType.getComponentType();
			childType = (Class<?>) genericType;
			partialTxtBuilder.append(childType.getName()).append(")");	
		} else if (Collection.class.isAssignableFrom(fieldType)) {
			genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			if (genericType instanceof ParameterizedType) {
				genericType = Object.class;
			}
			childType = (Class<?>) genericType;
			partialTxtBuilder.append(fieldType.getName()).append("<").append(childType.getName()).append(">")
				.append(")");	
		} else {
			childType = fieldType;
			genericType = null;
			partialTxtBuilder.append(fieldType.getTypeName()).append(")");	
		}
		if (!isMap ) {
			if (childType.isEnum()) {
				txtBuilder.append(" (*ENUM: ");
			} else {
				txtBuilder.append(" (");
			}
			txtBuilder.append(partialTxtBuilder);
			builder.addText(txtBuilder.toString());
			if (PackagesFilterUtil.isFilteredPackage(childType)) {
				builder.addChildren(createClassMetadataDtos(field, clz, childType, otcId, mapRegistry, 
						recursiveChildrenDepthCount, compiledIds));
			}
		}
		ClassMetadataDto classMetadataDto = builder.build();
		if (classMetadataDtos == null) {
			classMetadataDtos = new ArrayList<>();
		}
		classMetadataDtos.add(classMetadataDto);
		return classMetadataDtos;
	}

	private static Map<String, Field> fetchfields(Class<?> clz, Map<String, Field> fields) {
		if (!PackagesFilterUtil.isFilteredPackage(clz)) {
			return fields;
		}
		for (Field field : clz.getDeclaredFields()) {
			if (fields == null) {
				fields = new LinkedHashMap<>();
			}
			String fieldName = field.getName();
			if (!fields.containsKey(fieldName)) {
				fields.put(fieldName, field);
			}
		}
		Class<?> superClz = clz.getSuperclass();
		Map<String, Field> superClzFields = fetchfields(superClz, fields);
		if (superClzFields != null) {
			if (fields == null) {
				fields = superClzFields;
			} else {
				fields.putAll(superClzFields);
			}
		}
		return fields;
	}
	
} 
