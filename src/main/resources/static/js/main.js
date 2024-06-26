String.prototype.replaceAt=function(index, character) {
    return this.substr(0, index) + character + this.substr(index+character.length);
};

var pageUrl = "";

$(document).ready(function() {
	$("#otcInstructions").val('');
});


let anchorsMap = new Map();
let sourceMap = new Map();
let targetMap = new Map();

var copyCounter = 0;
var executeCounter = 0;
var selectMsg = "Select...";
var hasSrcTree = true;

const CONSTANTS = {
	ANCHOR: "anchor",
	ROOT: "$",
	SOURCE_ROOT: "source-root",
	TARGET_ROOT: "target-root",
	GETTER: "getter",
	SETTER: "setter",
	GETTER_HELPER: "getterHelper",
	SETTER_HELPER: "setterHelper",
	CONCRETE_TYPE: "concreteType",
	CMD_COPY: "Copy",
	CMD_EXECUTE: "Execute"
};
Object.freeze(CONSTANTS);

var command = CONSTANTS.CMD_COPY;

var headerTemplate = 
	"metadata:\r\n" +
	"  objectTypes:\r\n" +
	"    source: <<sourceType>>\r\n" +
	"    target: <<targetType>>\r\n" +
	"  entryClassName: <<entryClassName>>\r\n" +
	"  helper: <<helperClassName>>\r\n" +
	"commands:\r\n";

var copyScriptTemplate =
	"- copy:\r\n" +
	"    id: <<id>>\r\n" +
	"    from:\r\n" +
	"      <<from>>\r\n" +
	"      <<fromOverrides>>\r\n" +
	"    to:\r\n" +
	"      <<to>>\r\n" +
	"      <<toOverrides>>\r\n" +
	"    factoryClassName: <<factoryclass>>\r\n";

var executeScriptTemplate =
	"- execute:\r\n" +
	"    id: <<id>>\r\n" +
	"    converter: <<otcConverter>>\r\n" +
	"    module:\r\n" +
	"      namespace: <<otcNamespace>>\r\n" +
	"    executionOrder:\r\n" +
	"    - module\r\n" +
	"    - converter\r\n" +
	"    source:\r\n" +
	"      <<from>>\r\n" +
	"      <<fromOverrides>>\r\n" +
	"    target:\r\n" +
	"      <<to>>\r\n" +
	"      <<toOverrides>>\r\n" +
	"    factoryClassName: <<factoryclass>>";

var objectPathTemplate = 
	"objectPath: <<objectPath>>";

var valuesTemplate = 
	"values:\r\n" +
	"      - <<values>>";

var overridesTemplate = 
	"      overrides:\r\n" +
	"      - tokenPath: <<tokenPath>>\r\n";

var concreteTypeTemplate = 
	"        concreteType: <<concreteType>>\r\n";

var setterTemplate = 
	"        setter: <<setter>>\r\n";

var getterTemplate = 
	"        getter: <<getter>>\r\n";

var setterHelperTemplate =
	"        setterHelper: <<setterHelper>>\r\n";

var getterHelperTemplate =
	"        getterHelper: <<getterHelper>>\r\n";

var idPlaceholder = "<<id>>";
var sourceTypePlaceholder = "<<sourceType>>";
var targetTypePlaceholder = "<<targetType>>";
var fromPlaceholder = "<<from>>";
var valuesPlaceholder = "<<values>>";

var toPlaceholder = "<<to>>";
var objectPathPlaceholder = "<<objectPath>>";
var tokenPathPlaceholder = "<<tokenPath>>";
var fromOverridesPlaceholder = "      <<fromOverrides>>\r\n";
var toOverridesPlaceholder = "      <<toOverrides>>\r\n";

var entryClassNamePlaceholder = "<<entryClassName>>";
var helperClassNamePlaceholder = "<<helperClassName>>";
var getterPlaceholder = "<<getter>>";
var setterPlaceholder = "<<setter>>";
var getterHelperPlaceholder = "<<getterHelper>>";
var setterHelperPlaceholder = "<<setterHelper>>";
var dateFormatPlaceholder = "<<dateFormat>>";
var concreteTypePlaceholder = "<<concreteType>>";
var factoryclassPlaceholder = "<<factoryclass>>";
var otcConverterPlaceholder = "<<otcConverter>>";
var otcNamespacePlaceholder = "<<otcNamespace>>";

var helperParam = "helper:";
var getterHelperParam = "getterHelper:";
var setterHelperParam = "setterHelper:";


function getClassNames(url, eleName) {
	$.ajax({
        url : url,
    	type:"GET",
        contentType:"application/json;charset=utf-8",
        success : function(data) {
        	if (data) {
				var selectHtml = "<option value=''>".concat(selectMsg, "</option>");
	        	$.each(data, function(i, item){
	                selectHtml = selectHtml.concat("<option>", item, "</option>");
	            });
	        	$('#' + eleName).html(selectHtml);
        	}
        },
        error: function(xhr, status, error) {
            var errorHeader = xhr.status;
            var errorMessage = xhr.responseText;
            if (errorHeader == 500) {
                errorMessage = errorMessage + ' - Pls check if JDK versions of both OTC-Editor and the jar(s) in ${OTC_HOME}/lib' +
                    ' are compatible.';
            }
            showMsgDialog('Error: ' + errorHeader, errorMessage);
        }
    });	
}

$("#fetchSrc").click(function() {
	var srcPkgName = $('#srcPkgName').val();
	if (!srcPkgName) {
		return;
	}
	var url = pageUrl.concat('getTypes').concat('?pkgName=').concat(srcPkgName);
	getClassNames(url, 'srcClsNames');
});

$("#fetchTarget").click(function() {
	var targetPkgName = $('#targetPkgName').val();
	if (!targetPkgName) {
		return;
	}
	var url = pageUrl.concat('getTypes').concat('?pkgName=').concat(targetPkgName);
	getClassNames(url, 'targetClsNames');
});

$("#showTree").click(function() {
	$("#otcInstructions").val('');
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (targetClsName == null || targetClsName.trim() == '') {
		showMsgDialog($("#targetTypeNotSelected").data('title'), $("#msgTargetTypeNotSelected").text());
		return;
	}
	var url;
	if (srcClsName != null && srcClsName.trim() != '' && targetClsName != null && targetClsName.trim() != '') {
		url = pageUrl.concat('getTree');
	} else if (srcClsName != null && srcClsName.trim() != '') {
		url = pageUrl.concat('getSourceTree');
	} else if (targetClsName != null && targetClsName.trim() != '') {
		url = pageUrl.concat('getTargetTree');
	}
	var didSucceed = fetchAndPopulateJstree(url);
	if (!didSucceed) {
		return;
	}
    $('#srcTree').on('loaded.jstree', function() {
      	 $("#srcTree").jstree("open_node", $("#" + CONSTANTS.SOURCE_ROOT));
    });

    $('#targetTree').on('loaded.jstree', function() {
   	    $("#targetTree").jstree("open_node", $("#" + CONSTANTS.TARGET_ROOT));
   });
});

var commandItems = {
    "copy": {
    	name: "Copy", 
        type: 'radio', 
        callback: function() {
        	command = CONSTANTS.CMD_COPY;
            return true;
        }
    },
    "execute": {
    	name: "Execute", 
        type: 'radio',
        callback: function() {
        	command = CONSTANTS.CMD_EXECUTE;
            return true;
        }
   	}
};

$(function() {
    $.contextMenuCommon({
        selector: '.context-menu-one', 
        autoHide: true,
        callback: function(key, options) {
            var m = "clicked: " + key;
            window.console && console.log(m) || alert(m); 
        },
        items: commandItems
    });
 
});

var sourceOverrideItems = {
    "none": {
    	name: "None", 
        type: 'radio', 
        callback: function(key, options) {
    		sourceMap.delete(CONSTANTS.GETTER);
        	return false;
        }
    },
    "getterItem": {
    	name: "getter", 
        type: 'radio', 
        callback: function(key, options) {
    		sourceMap.set(CONSTANTS.GETTER, getterTemplate);
        	return false;
        }
    },
    'getterHelperItem' : {
    	name: "getterHelper", 
        type: 'radio', 
        callback: function(key, options) {
    		sourceMap.set(CONSTANTS.GETTER_HELPER, getterHelperTemplate);
        	return false;
        }
    }
};

var targetContextMenuItems = {
    "noSetter": {
    	name: "None", 
        type: 'radio', 
        radio: "set",
        callback: function(key, options) {
        	targetMap.delete(CONSTANTS.SETTER);
        	return false;
        }
    },
    'setterItem' : {
    	name: "setter", 
        type: 'radio', 
        radio: "set",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.SETTER, setterTemplate);
        	return false;
        }
    },
    'setterHelperItem' : {
    	name: "setterHelper", 
        type: 'radio', 
        radio: "set",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.SETTER_HELPER, setterHelperTemplate);
        	return false;
        }
    },
    separator1: "-----",
    "noGetter": {
    	name: "None", 
        type: 'radio', 
        radio: "get",
        callback: function(key, options) {
        	targetMap.delete(CONSTANTS.GETTER);
        	return false;
        }
    },
    'getterItem' : {
    	name: "getter", 
        type: 'radio', 
        radio: "get",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.GETTER, getterTemplate);
        	return false;
        }
    },
    'getterHelperItem' : {
    	name: "getterHelper", 
        type: 'radio', 
        radio: "get",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.GETTER_HELPER, getterHelperTemplate);
        	return false;
        }
    },
    separator2: "-----",
    'concreteTypeItem' : {
    	name: "concreteType", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
        		targetMap.delete(CONSTANTS.CONCRETE_TYPE);
        	} else {
        		targetMap.set(CONSTANTS.CONCRETE_TYPE, concreteTypeTemplate);
        	}
        	return false;
        }
    }
};

function jstreeContextmenu(node) {
	var otcChain = node.id;
	if (CONSTANTS.SOURCE_ROOT == otcChain) {
	    $.contextMenuCommon({
	        selector: '#srcTree', 
	        autoHide: true,
	        className: 'data-title',
	        callback: function(key, options) {
	            var m = "clicked: " + key;
	        },
	        items: sourceOverrideItems
	    });
	} else {
		$.contextMenu('destroy', "#targetTree");
		var menuItems;
		var className = 'data-title';
		if (CONSTANTS.TARGET_ROOT == otcChain) {
			menuItems = targetContextMenuItems; 
		} else {
			if (!otcChain.endsWith("]")) {
				return;
	        }
			var isChecked = false;
			if (anchorsMap.has(otcChain)) {
				isChecked = true;
			}
			var anchorItems = {
			   	anchor: {
			   		name: "Anchor", 
			        type: 'checkbox', 
			        selected: isChecked,
			        callback: function(key, opt) { 
			        	if (anchorsMap.has(otcChain)) {
			        		anchorsMap.delete(otcChain);
			        	} else if (otcChain.endsWith("]")) {
			        		var subKey;
			        		for (const [k, v] of anchorsMap.entries()) {
			        	   		if (otcChain.startsWith(k) || k.startsWith(otcChain)) {
			        	   			subKey = k;
			        	   			break;
			        	   		}
			        	   	};
			        	   	if (subKey) {
			        	   		anchorsMap.delete(subKey);
			        	   	}
				        	var lastIndexOf = otcChain.lastIndexOf("[");
				        	var anchoredOtcChain = otcChain.substring(0, lastIndexOf + 1) + "^" + otcChain.substring(lastIndexOf + 1);
				        	anchorsMap.set(otcChain, anchoredOtcChain);
				        }
					}
				}
			};
			menuItems = anchorItems;
			className = '';
		}
		$.contextMenuCommon({
	        selector: '#targetTree', 
	        autoHide: true,
	        className: className,
	        callback: function(key, options) {
	            var m = "clicked: " + key;
	        },
	        items: menuItems
	    });
	}
};

function fetchAndPopulateJstree(url) {
	$('#srcTree').jstree("destroy").empty();
	$('#targetTree').jstree("destroy").empty();
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (srcClsName != null && srcClsName != selectMsg) {
		url = url.concat('?srcClsName=').concat(srcClsName);
	}
	if (targetClsName != null) {
		if (srcClsName == null) {
			url = url.concat('?targetClsName=').concat(targetClsName);
		} else {
			url = url.concat('&targetClsName=').concat(targetClsName);
		}
	}
	$.jstree.defaults.core.force_text = true;
    $.ajax({
        url : url,
        dataType: "json",
        success : function(response) {
        	if (srcClsName != null) {
        		$('#srcTree').jstree({
	        		'core': {
	        			'data': response.sourceTreeData
	        		},
	        		"plugins": ["contextmenu"],
	        		"contextmenu": {
	        			"items": jstreeContextmenu
	        		}
	        	});
        		hasSrcTree = true;
        	} else {
        		hasSrcTree = false;
        	}
        	if (targetClsName != null) {
        		$('#targetTree').jstree({
	        		'core': {
	        			'data': response.targetTreeData
	        		},
	        		"plugins": ["contextmenu", "themes", "ui", "types"],
	        		"contextmenu": {
	        			"items": jstreeContextmenu
	        		}
	        	});
        	}
        },
        error: function(xhr, status, error) {
            var errorHeader = xhr.status;
            var errorMessage = xhr.responseText;
            if (errorHeader == 500) {
                errorMessage = errorMessage + ' - Pls check if JDK versions of both OTC-Editor and the jar(s) in ${OTC_HOME}/lib' +
                    ' are compatible.';
            }
            showMsgDialog('Error: ' + errorHeader, errorMessage);
        }
    });
   	return true;
}

$("#addScript").click(function( event ) {
    var targetNode = $('#targetTree').jstree(true).get_selected(true);
   	var targetOtcChain;
	var isTargetSetterRequired = false;
	var isTargetSetterHelperRequired = false;
	var isTargetGetterRequired = false;
	var isTargetGetterHelperRequired = false;
	var targetSetter = "";
	var targetGetter = "";
	var target = targetNode[0];
   	if (targetNode && targetNode[0]) {
    	var target = targetNode[0];
   		if (target.id == CONSTANTS.TARGET_ROOT) {
			if (CONSTANTS.CMD_COPY == command) {
        		showMsgDialog($("#rootTargetOtcChain").data('title'), $("#msgRootTargetOtcChain").text());
	   			return;
	   		}
   			targetOtcChain = CONSTANTS.ROOT;
   		} else {
   			targetOtcChain = target.id;
   		}
        isTargetSetterRequired = target.original.isSetterRequired;
        isTargetSetterHelperRequired = target.original.isSetterHelperRequired;
        isTargetGetterRequired = target.original.isGetterRequired;
        isTargetGetterHelperRequired = target.original.isGetterHelperRequired;
        if (isTargetSetterRequired) {
            targetSetter = target.original.setter;
        }
        if (isTargetGetterRequired) {
            targetGetter = target.original.getter;
        }
   	}
   	if (!targetOtcChain) {
   		showMsgDialog($("#targetOtcChain").data('title'), $("#msgTargetOtcChain").text());
		return;
	}
	var srcNode;
	var sourceOtcChain;
	if (hasSrcTree) {
		srcNode = $('#srcTree').jstree(true).get_selected(true);
	}
	if (CONSTANTS.CMD_EXECUTE == command && !source) {
   		showMsgDialog($("#sourceOtcChain").data('title'), $("#msgSourceOtcChain").text());
		return;
	}
	var isSrcGetterRequired = false;
	var isSrcGetterHelperRequired = false;
	var srcGetter = "";
	if (srcNode && srcNode[0]) {
    	var source = srcNode[0];
		if (source.id == CONSTANTS.SOURCE_ROOT) {
			if (CONSTANTS.CMD_COPY == command) {
           		showMsgDialog($("#rootSourceOtcChain").data('title'), $("#msgRootSourceOtcChain").text());
				return;
			}
			sourceOtcChain = CONSTANTS.ROOT;
		} else {
			sourceOtcChain = source.id;
		}
		isSrcGetterRequired = source.original.isGetterRequired;
		isSrcGetterHelperRequired = source.original.isGetterHelperRequired;
        if (isSrcGetterRequired) {
            srcGetter = source.original.getter;
        }
	}

	for (const [key, value] of anchorsMap.entries()) {
   		if (targetOtcChain.startsWith(key)) {
   			targetOtcChain = targetOtcChain.replace(key, value);
   			break;
   		}
   	};   	
	var isValid = isAnchorsValid(targetOtcChain);
	if (!isValid) {
		return;
	}
   	var otcInstructions = $("#otcInstructions");
   	var otcInstructionsValue = otcInstructions.val();
   	
   	if (otcInstructionsValue.trim() == "") {
   		copyCounter = 0;
   		executeCounter = 0;
   	}
   	var scriptBlock = null;
	if (CONSTANTS.CMD_COPY == command) {
		copyCounter++;
		var scriptId = "CP" + copyCounter;
		scriptBlock = copyScriptTemplate.replace(idPlaceholder, scriptId);
		scriptBlock = scriptBlock.replace(factoryclassPlaceholder, scriptId);
	} else {
		executeCounter++;
		var scriptId = "XE" + executeCounter;
		scriptBlock = executeScriptTemplate.replace(idPlaceholder, scriptId);
		scriptBlock = scriptBlock.replace(factoryclassPlaceholder, scriptId);
	}
	
	if (sourceOtcChain == null) {
		scriptBlock = scriptBlock.replace(fromPlaceholder, valuesTemplate);
	} else {
		if (CONSTANTS.CMD_EXECUTE == command) {
	    	var isValid = isValidCollections(targetOtcChain, sourceOtcChain);
	   		if (!isValid) {
	   			return;
	   		}
		}
		var from = objectPathTemplate.replace(objectPathPlaceholder, sourceOtcChain);
		scriptBlock = scriptBlock.replace(fromPlaceholder, from);
	}

	if (sourceMap.has(CONSTANTS.GETTER) || sourceMap.has(CONSTANTS.GETTER_HELPER) ||
	        isSrcGetterRequired || isSrcGetterHelperRequired) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, sourceOtcChain);
		if (sourceOtcChain != null) {
			if (sourceMap.has(CONSTANTS.GETTER) || isSrcGetterRequired) {
				overrides += getterTemplate;
				overrides = overrides.replace(getterPlaceholder, srcGetter);
			} else if (sourceMap.has(CONSTANTS.GETTER_HELPER) || isSrcGetterHelperRequired) {
				overrides += getterHelperTemplate;
			}
		}
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, "");
	}
	// ---- to
	var to = objectPathTemplate.replace(objectPathPlaceholder, targetOtcChain);
	scriptBlock = scriptBlock.replace(toPlaceholder, to);

	if (targetMap.has(CONSTANTS.GETTER) || targetMap.has(CONSTANTS.GETTER_HELPER) ||
			targetMap.has(CONSTANTS.SETTER) || targetMap.has(CONSTANTS.SETTER_HELPER) ||
			targetMap.has(CONSTANTS.CONCRETE_TYPE) || isTargetSetterRequired || isTargetSetterHelperRequired ||
			isTargetGetterRequired || isTargetGetterHelperRequired) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, targetOtcChain.replace('^', ''));
		if (targetMap.has(CONSTANTS.GETTER) || isTargetGetterRequired) {
            overrides += getterTemplate;
			overrides = overrides.replace(getterPlaceholder, targetGetter);
        } else if (targetMap.has(CONSTANTS.GETTER_HELPER) || isTargetGetterHelperRequired) {
            overrides += getterHelperTemplate;
		}
		if (targetMap.has(CONSTANTS.SETTER) || isTargetSetterRequired) {
            overrides += setterTemplate;
			overrides = overrides.replace(setterPlaceholder, targetSetter);
        } else if (targetMap.has(CONSTANTS.SETTER_HELPER) || isTargetSetterHelperRequired) {
            overrides += setterHelperTemplate;
		}
		if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
			overrides += concreteTypeTemplate;
		}
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, "");
	}

   	if (otcInstructionsValue.trim() == "") {
   		var sourceType = $("#srcClsNames").val();
   		var targetType = $("#targetClsNames").val();
   		var entryClsName = "";
    	var lastIndexOf = 0;
    	if (sourceType) {
			if (sourceType.includes(".")) {
				sourceType = sourceType.replace("$", "_");
			}
			if (sourceType.includes(".")) {
	    		lastIndexOf = sourceType.lastIndexOf(".");
	    		entryClsName = sourceType.substring(lastIndexOf + 1);
	    	} else {
	    		entryClsName = sourceType;
	    	}
		}
    	entryClsName = entryClsName.concat("__");
		if (targetType.includes(".")) {
			targetType = targetType.replace("$", "_");
		}
    	if (targetType.includes(".")) {
    		lastIndexOf = targetType.lastIndexOf(".");
    		entryClsName = entryClsName.concat(targetType.substring(lastIndexOf + 1));
    	} else {
    		entryClsName = entryClsName.concat(targetType);
    	}
   		var header = headerTemplate.replace(sourceTypePlaceholder, sourceType)
							.replace(targetTypePlaceholder, targetType)
							.replace(entryClassNamePlaceholder, entryClsName);
   		otcInstructionsValue = header;
   		otcInstructions.val(header);
   	} else {
   		otcInstructionsValue = otcInstructionsValue.concat('\n\n');
   	}
   	otcInstructionsValue = otcInstructionsValue.concat(scriptBlock);
   	otcInstructions.val(otcInstructionsValue);
    if (otcInstructions.length) {
    	otcInstructions.scrollTop(otcInstructions[0].scrollHeight - otcInstructions.height());
    }
   	$('#converters').val("");
   	$('#otcScriptType').val("");
});

function isValidCollections(targetOtcChain, sourceOtcChain) {
   	var idxTarget = targetOtcChain.indexOf("[");
   	var idxSource = sourceOtcChain.indexOf("[");
	if (idxTarget > 1 && idxSource > 1) {
        showMsgDialog($("#singleSideCollectionOnly").data('title'), $("#msgSingleSideCollectionOnly").text());
    	return false;
	}
	return true;
}

function isAnchorsValid(otcChain) {
	var count = (otcChain.match(/^/g) || []).length;
	if (count > 1) {
		$("#otcChain").val(" in " + otcChain);
        showMsgDialog($("#multipleAnchors").data('title'), $("#msgMultipleAnchors").text());
		$("#otcChain").val("");
    	return false;		
	}
	return true;
}

$("#reset").click(function() {
	$("#otcInstructions").val('');
	copyCounter = 0;
	executeCounter = 0;
});

$("#otcEditorForm").submit(function(event) {
   	var otcInstructions = $("#otcInstructions");
   	var otcInstructionsValue = otcInstructions.val();

   	if (otcInstructionsValue.includes(helperClassNamePlaceholder)) {
        showMsgDialog($("#helperClassName").data('title'), $("#msgHelperClassName").text());
		return false;
	}
   	if (otcInstructionsValue.includes(valuesPlaceholder)) {
        showMsgDialog($("#values").data('title'), $("#msgValues").text());
		return false;
	}
   	if (otcInstructionsValue.includes(getterPlaceholder)) {
        showMsgDialog($("#getter").data('title'), $("#msgGetter").text());
		return false;
	}
   	if (otcInstructionsValue.includes(setterPlaceholder)) {
        showMsgDialog($("#setter").data('title'), $("#msgSetter").text());
		return false;
	}
   	if (otcInstructionsValue.includes(getterHelperPlaceholder)) {
        showMsgDialog($("#getterHelper").data('title'), $("#msgGetterHelper").text());
		return false;
	}
   	if (otcInstructionsValue.includes(getterHelperParam) || otcInstructionsValue.includes(setterHelperParam)) {
   		if (!otcInstructionsValue.includes(helperParam)) {
            showMsgDialog($("#helperClassNotDefined").data('title'), $("#msgHelperClassNotDefined").text());
   			return false;
   		}
	}
   	if (otcInstructionsValue.includes(setterHelperPlaceholder)) {
        showMsgDialog($("#setterHelper").data('title'), $("#msgSetterHelper").text());
		return false;
	}
   	if (otcInstructionsValue.includes(concreteTypePlaceholder)) {
        showMsgDialog($("#concreteType").data('title'), $("#msgConcreteType").text());
		return false;
	}
   	if (otcInstructionsValue.includes(otcConverterPlaceholder)) {
        showMsgDialog($("#otcConverter").data('title'), $("#msgOtcConverter").text());
		return false;
	}
   	if (otcInstructionsValue.includes(otcNamespacePlaceholder)) {
        showMsgDialog($("#otcNamespace").data('title'), $("#msgOtcNamespace").text());
		return false;
	}
});

$("#createOtcFile").click(function() {
	var targetClsName = $('#targetClsNames').val();
	if (!targetClsName) {
        showMsgDialog($("#typesNotSelected").data('title'), $("#msgTypesNotSelected").text());
   		return false;
	}
	var otcInstructions = $("#otcInstructions").val();
	if (!otcInstructions) {
        showMsgDialog($("#nothingToSave").data('title'), $("#msgNothingToSave").text());
		return false;
	}
	$("#otcEditorForm").submit();
});

$("#flipOtc").click(function() {
	var otc = $("#otcInstructions").val();
	if (!otc) {
        showMsgDialog($("#nothingToSave").data('title'), $("#msgNothingToSave").text());
		return;
	}
	var srcClsName = $('#srcClsNames').val();
	if (!srcClsName) {
        showMsgDialog($("#typesNotSelected").data('title'), $("#msgTypesNotSelected").text());
   		return false;
	}
	var targetClsName = $('#targetClsNames').val();
	if (!targetClsName) {
        showMsgDialog($("#typesNotSelected").data('title'), $("#msgTypesNotSelected").text());
   		return false;
	}
	$("#infoLoss").show();
	$("#infoLoss").dialog({
		resizable: false,
	    modal: true,
		buttons: {
	        Ok: function() {
	          $( this ).dialog( "close" );
	      		var url = pageUrl.concat('flipOtc');
		    	var entryClzName = $('#srcPkgName').val();
		    	var newEntryClzName = targetClsName + "To" + srcClsName;
		    	otc = otc.replace(entryClzName, newEntryClzName);
	    		$.post( url, { otcInstructions: otc }).done(function( data ) {
	      			$("#otcInstructions").val(data);
	    	  	});
		    	var srcPkgName = $('#srcPkgName').val();
		    	var targetPkgName = $('#targetPkgName').val();
		    	$('#srcPkgName').val(targetPkgName);
		    	$('#targetPkgName').val(srcPkgName);
	
		    	var $srcClsNames = $("#srcClsNames > option").clone();
		    	var $targetClsNames = $("#targetClsNames > option").clone();
		    	$('#srcClsNames').empty();
		    	$('#targetClsNames').empty();
		    	$('#srcClsNames').append($targetClsNames);
		    	$('#targetClsNames').append($srcClsNames);
		    	
		    	$('#srcTree').jstree("destroy").empty();
		    	$('#targetTree').jstree("destroy").empty();
	        },
			Cancel: function() {
				return;
			}
        }
	});
});

$("#compile").click(function() {
	var url = $('#compile').data('href');
    $.ajax({
        url : url,
        type: "PUT",
        success : function(response) {
            showMsgDialog('Compilation:', response);
        },
        error: function(xhr, status, error) {
            var errorHeader = xhr.status;
            var errorMessage = xhr.responseText;
            if (errorHeader == 500) {
                errorMessage == error + ' - probable cause - Pls check if JDK versions of the jar(s) in ${OTC_HOME}/lib' +
                    ' and otceditor match.';
            }
            showMsgDialog('Error: ' + errorHeader, errorMessage);
        }
    });
});

function showMsgDialog(caption, msg) {
    $("#messageDialog").empty();
    $("#messageDialog").html(msg);
//	$("#message").html(msg);
	$("#messageDialog").show();
	$("#messageDialog").dialog({
		title: caption,
		resizable: true,
	    modal: true,
		buttons: {
	        Ok: function() {
                $( this ).dialog("close");
	        }
        }
	});
	return;
}