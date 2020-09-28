String.prototype.replaceAt=function(index, character) {
    return this.substr(0, index) + character + this.substr(index+character.length);
};

var pageUrl = "";

$(document).ready(function() {
	var url = pageUrl.concat('showConverters');
	getClassNames(url, 'converters');
	$("#otclInstructions").val('');
});


let map = new Map();
let sourceMap = new Map();
let targetMap = new Map();

//var isInitialized = false;

var copyCounter = 0;
var executeCounter = 0;
var selectMsg = "Select...";

const CONSTANTS = {
	SOURCE_ROOT: "source-root",
	TARGET_ROOT: "target-root",
	GETTER: "getter",
	SETTER: "setter",
	ACTIVATE_GETTER: "activateGetter",
	ACTIVATE_SETTER: "activateSetter",
	CONCRETE_TYPE: "concreteType",
	DATE_FORMAT: "dateFormat",
	CMD_COPY: "Copy",
	CMD_EXECUTE: "Execute"
};
Object.freeze(CONSTANTS);

var headerTemplate = 
	"metadata:\r\n" +
	"  mainClassName: <<mainClassName>>\r\n" +
	"  helper: <<helperClassName>>\r\n" +
	"  objectTypes:\r\n" +
	"    source: <<source-type>>\r\n" +
	"    target: <<target-type>>\r\n" +
	"otclScripts:\r\n";

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
	"    otclConverter: <<otclConverter>>\r\n" +
	"    otclModule:\r\n" +
	"      otclNamespace: <<otclNamespace>>\r\n" +
	"    executionOrder:\r\n" +
	"    - otclModule\r\n" +
	"    - otclConverter\r\n" +
	"    source:\r\n" +
	"      <<from>>\r\n" +
	"      <<fromOverrides>>\r\n" +
	"    target:\r\n" +
	"      <<to>>\r\n" +
	"      <<toOverrides>>\r\n" +
	"    factoryClassName: <<factoryclass>>";

var otclChainTemplate = 
	"otclChain: <<otclChain>>";

var valuesTemplate = 
	"values:\r\n" +
	"      - <<values>>";

var overridesTemplate = 
	"overrides:\r\n" +
	"      - tokenPath: <<tokenPath>>\r\n";

var concreteTypeTemplate = 
	"        concreteType: <<concreteType>>\r\n";

var dateFormatTemplate = 
	"        dateFormat: <<dateFormat>>\r\n";

var setterTemplate = 
	"        setter: <<setter>>\r\n";

var getterTemplate = 
	"        getter: <<getter>>\r\n";

var activateSetterInHelperTemplate =
	"        activateSetterInHelper: true\r\n";

var activateGetterInHelperTemplate =
	"        activateGetterInHelper: true\r\n";

var idPlaceholder = "<<id>>";
var sourceTypePlaceholder = "<<source-type>>";
var targetTypePlaceholder = "<<target-type>>";
var fromPlaceholder = "<<from>>";
var toPlaceholder = "<<to>>";
var otclChainPlaceholder = "<<otclChain>>";
var tokenPathPlaceholder = "<<tokenPath>>";
var fromOverridesPlaceholder = "      <<fromOverrides>>\r\n";
var toOverridesPlaceholder = "      <<toOverrides>>\r\n";

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
	        	$('#'+eleName).html(selectHtml);
        	}
        }
    });	
}

$("#fetchSrc").click(function() {
	var srcPkgName = $('#srcPkgName').val();
	if (!srcPkgName) {
		return;
	}
	var url = pageUrl.concat('showTypes').concat('?pkgName=').concat(srcPkgName);
	getClassNames(url, 'srcClsNames');
});

$("#fetchTarget").click(function() {
	var targetPkgName = $('#targetPkgName').val();
	if (!targetPkgName) {
		return;
	}
	var url = pageUrl.concat('showTypes').concat('?pkgName=').concat(targetPkgName);
	getClassNames(url, 'targetClsNames');
});

$("#showTree").click(function() {
	$("#otclInstructions").val('');
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	var url;
	if (srcClsName != null && targetClsName != null) {
		url = pageUrl.concat('fetchJsTreeData');
	} else if (srcClsName != null) {
		url = pageUrl.concat('fetchSourceJsTreeData');
	} else {
		url = pageUrl.concat('fetchTargetJsTreeData');
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

function jstreeContextmenu($node) {
	var otclChain = $node.id;
	if (CONSTANTS.SOURCE_ROOT == otclChain) {
		return sourceOverrideItems;
	} else if (CONSTANTS.TARGET_ROOT == otclChain) {
		return targetOverrideItems;
	} else {
	    if (map.has(otclChain)) {
	    	anchorItems.anchor.label = "Anchor off"
	    } else {
	    	anchorItems.anchor.label = "Anchor on"
	    }
		if (otclChain.includes("]")) {
			return anchorItems;
		}
	}
};

var anchorItems = {
   	"anchor": {
   		"label": "Anchor",
   		"icon" : false,
	   	"action": function (obj) { 
	        if (map.has(otclChain)) {
	        	map.delete(otclChain);
	        } else if (otclChain.includes("]")) {
	        	var lastIndexOf = otclChain.lastIndexOf("[<");
	        	if (lastIndexOf < 0) {
	        		lastIndexOf = otclChain.lastIndexOf("[");
	        	}
	        	var sourceOtclChain = otclChain.substring(0, lastIndexOf + 1);
	        	if (otclChain.charAt(lastIndexOf + 1) == "*") {
	        		targetOtclChain = otclChain.substring(lastIndexOf + 2);
	            } else {
	        		targetOtclChain = otclChain.substring(lastIndexOf + 1);
	            }
	        	var anchoredOtclChain = sourceOtclChain + "^" + targetOtclChain;
	        	map.set(otclChain, anchoredOtclChain);
	        }
		}
   	}
};

var sourceOverrideItems = {
    'getterItem' : {
        'label' : 'getter',
    	'icon' : false,
        'action' : function (menuItem) {
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		sourceMap.set(CONSTANTS.GETTER, getterTemplate);
        	} else {
        		menuItem.item.icon = false;
        		sourceMap.delete(CONSTANTS.GETTER);
        	}
        }
    },
    'activateGetterInHelperItem' : {
        'label' : 'activateGetterInHelper',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		sourceMap.set(CONSTANTS.ACTIVATE_GETTER, activateGetterInHelperTemplate);
        	} else {
        		menuItem.item.icon = false;
        		sourceMap.delete(CONSTANTS.ACTIVATE_GETTER);
        	}
        }
    },
    'dateFormatItem' : {
        'label' : 'dateFormat',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		sourceMap.set(CONSTANTS.DATE_FORMAT, dateFormatTemplate);
        	} else {
        		menuItem.item.icon = false;
        		sourceMap.delete(CONSTANTS.DATE_FORMAT);
        	}
        }
    }
};

var targetOverrideItems = {
    'getterItem' : {
        'label' : 'getter',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		targetMap.set(CONSTANTS.GETTER, getterTemplate);
        	} else {
        		menuItem.item.icon = false;
        		targetMap.delete(CONSTANTS.GETTER);
        	}
        }
    },
    'setterItem' : {
        'label' : 'setter',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		targetMap.set(CONSTANTS.SETTER, setterTemplate);
        	} else {
        		menuItem.item.icon = false;
        		targetMap.delete(CONSTANTS.SETTER);
        	}
        }
    },
    'activateGetterInHelperItem' : {
        'label' : 'activateGetterInHelper',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		targetMap.set(CONSTANTS.ACTIVATE_GETTER, activateGetterInHelperTemplate);
        	} else {
        		menuItem.item.icon = false;
        		targetMap.delete(CONSTANTS.ACTIVATE_GETTER);
        	}
        }
    },
    'activateSetterInHelperItem' : {
        'label' : 'activateSetterInHelper',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		targetMap.set(CONSTANTS.ACTIVATE_SETTER, activateSetterInHelperTemplate);
        	} else {
        		menuItem.item.icon = false;
        		targetMap.delete(CONSTANTS.ACTIVATE_SETTER);
        	}
        }
    },
    'concreteTypeItem' : {
        'label' : 'concreteType',
    	'icon' : false,
        'action' : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		menuItem.item.icon = 'images/tick.png';
        		targetMap.set(CONSTANTS.CONCRETE_TYPE, concreteTypeTemplate);
        	} else {
        		menuItem.item.icon = false;
        		targetMap.delete(CONSTANTS.CONCRETE_TYPE);
        	}
        }
    }
};

function fetchAndPopulateJstree(url) {
	$('#srcTree').jstree("destroy").empty();
	$('#targetTree').jstree("destroy").empty();
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (srcClsName != null) {
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
        dataType:"json",
        success : function(response) {
        	if (srcClsName != null) {
        		$('#srcTree').jstree({
	        		'core': {
	        			'data': response.sourceFieldNames
	        		},
	        		"plugins": ["contextmenu"],
	        		"contextmenu": {
	        			"items": jstreeContextmenu
	        		}
	        	});
        	}
        	if (targetClsName != null) {
        		$('#targetTree').jstree({
	        		'core': {
	        			'data': response.targetFieldNames
	        		},
	        		"plugins": ["contextmenu"],
	        		"contextmenu": {
	        			"items": jstreeContextmenu
	        		}
	        	});
        	}
        },
        error : function(xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
   	return true;
}

function areTypesSelected() {
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (!srcClsName || !targetClsName) {
        $("#typesNotSelected").show();
       	$("#typesNotSelected").dialog({resizable: false}).dialog("open");
   		return false;
	}
	return true;
}

$("#addScript").click(function( event ) {
    var targetNode = $('#targetTree').jstree(true).get_selected(true);
   	var targetOtclChain;
   	if (targetNode[0]) {
   		if (targetNode[0].id == CONSTANTS.TARGET_ROOT) {
        	$("#rootOtclChain").show();
        	$("#rootOtclChain").dialog({resizable: false}).dialog("open");
   			return;
   		}
   		targetOtclChain = targetNode[0].id;
   	}
   	if (!targetOtclChain) {
        $( "#targetOtclChain").show();
        $( "#targetOtclChain").dialog({resizable: false}).dialog("open");
		return;
	}
	var isValid = isAnchorsValid(targetOtclChain);
	if (!isValid) {
		return;
	}
	
    var srcNode = $('#srcTree').jstree(true).get_selected(true);
	var sourceOtclChain;
   	if (srcNode[0]) {
   		if (srcNode[0].id == CONSTANTS.SOURCE_ROOT) {
        	$("#rootOtclChain").show();
        	$("#rootOtclChain").dialog({resizable: false}).dialog("open");
        	return;
   		}
	  	sourceOtclChain = srcNode[0].id;
   	}

   	var command = $('input[name="command"]:checked').val();
   	var scriptBlock = null;
	if (CONSTANTS.CMD_COPY == command) {
		copyCounter++;
		scriptBlock = copyScriptTemplate.replace(idPlaceholder, "Copy-" + copyCounter);
	} else {
		executeCounter++;
		scriptBlock = executeScriptTemplate.replace(idPlaceholder, "Execute-" + executeCounter);
	}
   	console.log(executeScriptTemplate);
	if (sourceOtclChain == null) {
		scriptBlock = scriptBlock.replace(fromPlaceholder, valuesTemplate);
	} else {
    	var isValid = isValidCollections(targetOtclChain, sourceOtclChain);
   		if (!isValid) {
   			return;
   		}
		var from = otclChainTemplate.replace(otclChainPlaceholder, sourceOtclChain);
		scriptBlock = scriptBlock.replace(fromPlaceholder, from);
	}
   	console.log(scriptBlock);
	if (sourceMap.has(CONSTANTS.GETTER) || sourceMap.has(CONSTANTS.ACTIVATE_GETTER) ||
			sourceMap.has(CONSTANTS.DATE_FORMAT)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, sourceOtclChain);
		if (sourceMap.has(CONSTANTS.GETTER)) {
			overrides += getterTemplate;
		}
		if (sourceMap.has(CONSTANTS.ACTIVATE_GETTER)) {
			overrides += activateGetterInHelperTemplate;
		}
		if (sourceMap.has(CONSTANTS.DATE_FORMAT)) {
			overrides += dateFormatTemplate;
		}
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, "");
	}
   	console.log(scriptBlock);
	// to
	var to = otclChainTemplate.replace(otclChainPlaceholder, targetOtclChain);
	scriptBlock = scriptBlock.replace(toPlaceholder, to);
	
	if (targetMap.has(CONSTANTS.GETTER) || targetMap.has(CONSTANTS.ACTIVATE_GETTER) ||
			targetMap.has(CONSTANTS.SETTER) || targetMap.has(CONSTANTS.ACTIVATE_SETTER) ||
			targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, targetOtclChain);
		if (targetMap.has(CONSTANTS.GETTER)) {
			overrides += getterTemplate;
		}
		if (targetMap.has(CONSTANTS.SETTER)) {
			overrides += setterTemplate;
		}
		if (targetMap.has(CONSTANTS.ACTIVATE_GETTER)) {
			overrides += activateGetterInHelperTemplate;
		}
		if (targetMap.has(CONSTANTS.ACTIVATE_SETTER)) {
			overrides += activateSetterInHelperTemplate;
		}		
		if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
			overrides += concreteTypeTemplate;
		}
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, "");
	}
   	console.log(scriptBlock);

   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	
   	if (otclInstructionsValue.trim() == "") {
//   		isInitialized = true;
   		var sourceType = $("#srcClsNames").val();
   		var targetType = $("#targetClsNames").val();
   		var header = headerTemplate.replace(sourceTypePlaceholder, sourceType).replace(targetTypePlaceholder, targetType);
   		otclInstructionsValue = header;
   		otclInstructions.val(header);
   	} else {
   		otclInstructionsValue = otclInstructionsValue.concat('\n\n');
   	}
   	otclInstructionsValue = otclInstructionsValue.concat(scriptBlock);
   	otclInstructions.val(otclInstructionsValue);
    if (otclInstructions.length) {
    	otclInstructions.scrollTop(otclInstructions[0].scrollHeight - otclInstructions.height());
    }
   	$('#converters').val("");
   	$('#otclScriptType').val("");
});

function isValidCollections(targetOtclChain, sourceOtclChain) {
   	var idxTarget = targetOtclChain.indexOf("[");
   	var idxSource = sourceOtclChain.indexOf("[");
	if (idxTarget > 1 && idxSource > 1) {
    	$("#singleSideCollectionOnly").show();
    	$("#singleSideCollectionOnly").dialog({resizable: false}).dialog("open");
    	return false;
	}
	return true;
}

function isAnchorsValid(otclChain) {
	var count = (otclChain.match(/^/g) || []).length;
	if (count > 1) {
		$("#otclChain").val(" in " + otclChain);
    	$("#multipleAnchors").show();
    	$("#multipleAnchors").dialog({resizable: false}).dialog("open");
    	return false;		
	}
	return true;
}


$("#reset").click(function() {
	$("#otclInstructions").val('');
	copyCounter = 0;
	executeCounter = 0;
//	isInitialized = false;
});

$("#otclEditor").submit(function(event) {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	$('<input />').attr('type', 'text')
    	.attr('name', 'reverseOtclFile')
    	.attr('value', $('reverseOtclFile').val());
	
   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	otclInstructionsValue = otclInstructionsValue.replace("<targetType-placeholder>", $('#targetClsNames').val())
   		.replace("<sourceType-placeholder>", $('#srcClsNames').val());
   	otclInstructions.val(otclInstructionsValue);
});

$("#createOtclFile").click(function() {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var otclInstructions = $("#otclInstructions").val();
	if (!otclInstructions) {
		$("#nothingToSave").show();
   		$("#nothingToSave").dialog({resizable: false}).dialog("open");
		return;
	}
	$("#otclEditor").submit();
});

$("#flipOtcl").click(function() {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var otclInstructions = $("#otclInstructions").val();
	if (!otclInstructions) {
		$("#nothingToSave").show();
   		$("#nothingToSave").dialog({resizable: false}).dialog("open");
		return;
	}
	$("#reverseOtclFile").val("true");
	$("#otclEditor").submit();
	$("#reverseOtclFile").val("false");
});
