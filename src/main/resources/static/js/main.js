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

var copyCounter = 0;
var executeCounter = 0;
var selectMsg = "Select...";
var isSrcTree = true;

const CONSTANTS = {
	ANCHOR: "anchor",
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

var command = CONSTANTS.CMD_COPY;

var headerTemplate = 
	"metadata:\r\n" +
	"  mainClassName: <<mainClassName>>\r\n" +
	"  helper: <<helperClassName>>\r\n" +
	"  objectTypes:\r\n" +
	"    source: <<sourceType>>\r\n" +
	"    target: <<targetType>>\r\n" +
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
	"      overrides:\r\n" +
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
var sourceTypePlaceholder = "<<sourceType>>";
var targetTypePlaceholder = "<<targetType>>";
var fromPlaceholder = "<<from>>";
var toPlaceholder = "<<to>>";
var otclChainPlaceholder = "<<otclChain>>";
var tokenPathPlaceholder = "<<tokenPath>>";
var fromOverridesPlaceholder = "      <<fromOverrides>>\r\n";
var toOverridesPlaceholder = "      <<toOverrides>>\r\n";

var mainClassNamePlaceholder = "<<mainClassName>>";
var helperClassNamePlaceholder = "<<helperClassName>>";
var getterPlaceholder = "<<getter>>";
var setterPlaceholder = "<<setter>>";
var dateFormatPlaceholder = "<<dateFormat>>";
var concreteTypePlaceholder = "<<concreteType>>";
var factoryclassPlaceholder = "<<factoryclass>>";
var otclConverterPlaceholder = "<<otclConverter>>";
var otclNamespacePlaceholder = "<<otclNamespace>>";


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
	if (srcClsName != null && srcClsName.trim() != '' && targetClsName != null && targetClsName.trim() != '') {
		url = pageUrl.concat('fetchJsTreeData');
	} else if (srcClsName != null && srcClsName.trim() != '') {
		url = pageUrl.concat('fetchSourceJsTreeData');
	} else if (targetClsName != null && targetClsName.trim() != '') {
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

    $('.context-menu-one').on('click', function(e){
        console.log('clicked', this);
    })    
});

var sourceOverrideItems = {
    "getterItem": {
    	name: "getter", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (sourceMap.has(CONSTANTS.GETTER)) {
        		sourceMap.delete(CONSTANTS.GETTER);
        	} else {
        		sourceMap.set(CONSTANTS.GETTER, getterTemplate);
        	}
        }
    },
    'activateGetterInHelperItem' : {
    	name: "activateGetterInHelper", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (sourceMap.has(CONSTANTS.ACTIVATE_GETTER)) {
        		sourceMap.delete(CONSTANTS.ACTIVATE_GETTER);
        	} else {
        		sourceMap.set(CONSTANTS.ACTIVATE_GETTER, activateGetterInHelperTemplate);
        	}
        }
    },
    'dateFormatItem' : {
    	name: "dateFormat", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (sourceMap.has(CONSTANTS.DATE_FORMAT)) {
        		sourceMap.delete(CONSTANTS.DATE_FORMAT);
        	} else {
        		sourceMap.set(CONSTANTS.DATE_FORMAT, dateFormatTemplate);
        	}
        }
    }
};

var targetContextMenuItems = {
    'getterItem' : {
    	name: "getter", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.GETTER)) {
        		targetMap.delete(CONSTANTS.GETTER);
        	} else {
        		targetMap.set(CONSTANTS.GETTER, getterTemplate);
        	}
        }
    },
    'activateGetterInHelperItem' : {
    	name: "activateGetterInHelper", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.ACTIVATE_GETTER)) {
        		targetMap.delete(CONSTANTS.ACTIVATE_GETTER);
        	} else {
        		targetMap.set(CONSTANTS.ACTIVATE_GETTER, activateGetterInHelperTemplate);
        	}
        }
    },
    'setterItem' : {
    	name: "setter", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.SETTER)) {
        		targetMap.delete(CONSTANTS.SETTER);
        	} else {
        		targetMap.set(CONSTANTS.SETTER, setterTemplate);
        	}
        }
    },
    'activateSetterInHelperItem' : {
    	name: "activateSetterInHelper", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.ACTIVATE_SETTER)) {
        		targetMap.delete(CONSTANTS.ACTIVATE_SETTER);
        	} else {
        		targetMap.set(CONSTANTS.ACTIVATE_SETTER, activateSetterInHelperTemplate);
        	}
        }
    },
    'concreteTypeItem' : {
    	name: "concreteType", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
        		targetMap.delete(CONSTANTS.CONCRETE_TYPE);
        	} else {
        		targetMap.set(CONSTANTS.CONCRETE_TYPE, concreteTypeTemplate);
        	}
        }
    }
};

function jstreeContextmenu(node) {
	var otclChain = node.id;
	if (CONSTANTS.SOURCE_ROOT == otclChain) {
	    $.contextMenuCommon({
	        selector: '#srcTree', 
	        autoHide: true,
	        callback: function(key, options) {
	            var m = "clicked: " + key;
	            window.console && console.log(m) || alert(m); 
	        },
	        items: sourceOverrideItems
	    });
	} else {
		$.contextMenu('destroy', "#targetTree");
		var menuItems;
		if (CONSTANTS.TARGET_ROOT == otclChain) {
			menuItems = targetContextMenuItems; 
		} else {
			if (!otclChain.includes("]")) {
				return;
	        }
			var anchorItems = {
			   	anchor: {
			   		name: "Anchor", 
			        type: 'checkbox', 
			        callback: function(key, opt) { 
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
			menuItems = anchorItems;
		}
		$.contextMenuCommon({
	        selector: '#targetTree', 
	        autoHide: true,
	        callback: function(key, options) {
	            var m = "clicked: " + key;
	            window.console && console.log(m) || alert(m); 
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
        		isSrcTree = true;
        	} else {
        		isSrcTree = false;
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
		showMsg($("#typesNotSelected"));
   		return false;
	}
	return true;
}

$("#addScript").click(function( event ) {
    var targetNode = $('#targetTree').jstree(true).get_selected(true);
   	var targetOtclChain;
   	if (targetNode[0]) {
   		if (targetNode[0].id == CONSTANTS.TARGET_ROOT) {
   			showMsg($("#rootOtclChain"));
   			return;
   		}
   		targetOtclChain = targetNode[0].id;
   	}
   	if (!targetOtclChain) {
		showMsg($("#targetOtclChain"));
		return;
	}
	var isValid = isAnchorsValid(targetOtclChain);
	if (!isValid) {
		return;
	}
   	var scriptBlock = null;
	if (CONSTANTS.CMD_COPY == command) {
		copyCounter++;
		scriptBlock = copyScriptTemplate.replace(idPlaceholder, "CPY" + copyCounter);
	} else {
		executeCounter++;
		scriptBlock = executeScriptTemplate.replace(idPlaceholder, "EXE" + executeCounter);
	}
    var srcNode;
	var sourceOtclChain;
    if (isSrcTree) {
    	srcNode = $('#srcTree').jstree(true).get_selected(true);
    }
   	if (srcNode && srcNode[0]) {
   		if (srcNode[0].id == CONSTANTS.SOURCE_ROOT) {
   			showMsg($("#rootOtclChain"));
   			return;
   		}
	  	sourceOtclChain = srcNode[0].id;
   	}
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
	if (sourceMap.has(CONSTANTS.GETTER) || sourceMap.has(CONSTANTS.ACTIVATE_GETTER) ||
			sourceMap.has(CONSTANTS.DATE_FORMAT)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, sourceOtclChain);
		if (sourceOtclChain != null) {
			if (sourceMap.has(CONSTANTS.GETTER)) {
				overrides += getterTemplate;
			}
			if (sourceMap.has(CONSTANTS.ACTIVATE_GETTER)) {
				overrides += activateGetterInHelperTemplate;
			}
		}
		if (sourceMap.has(CONSTANTS.DATE_FORMAT)) {
			overrides += dateFormatTemplate;
		}
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, "");
	}
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

   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	
   	if (otclInstructionsValue.trim() == "") {
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
		showMsg($("#singleSideCollectionOnly"));
    	return false;
	}
	return true;
}

function isAnchorsValid(otclChain) {
	var count = (otclChain.match(/^/g) || []).length;
	if (count > 1) {
		$("#otclChain").val(" in " + otclChain);
		showMsg($("#multipleAnchors"));
    	return false;		
	}
	return true;
}

$("#reset").click(function() {
	$("#otclInstructions").val('');
	copyCounter = 0;
	executeCounter = 0;
});

$("#otclEditor").submit(function(event) {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();

   	if (otclInstructionsValue.includes(sourceTypePlaceholder)) {
		showMsg($("#sourceType"));
		return false;
	}
   	if (otclInstructionsValue.includes(targetTypePlaceholder)) {
		showMsg($("#targetType"));
		return false;
	}
   	if (otclInstructionsValue.includes(mainClassNamePlaceholder)) {
		showMsg($("#mainClassName"));
		return false;
	}
   	if (otclInstructionsValue.includes(helperClassNamePlaceholder)) {
		showMsg($("#helperClassName"));
		return false;
	}
   	if (otclInstructionsValue.includes(getterPlaceholder)) {
		showMsg($("#getter"));
		return false;
	}
   	if (otclInstructionsValue.includes(setterPlaceholder)) {
		showMsg($("#setter"));
		return false;
	}
   	if (otclInstructionsValue.includes(dateFormatPlaceholder)) {
		showMsg($("#dateFormat"));
		return false;
	}
   	if (otclInstructionsValue.includes(concreteTypePlaceholder)) {
		showMsg($("#concreteType"));
		return false;
	}
   	if (otclInstructionsValue.includes(otclConverterPlaceholder)) {
		showMsg($("#otclConverter"));
		return false;
	}
   	if (otclInstructionsValue.includes(otclNamespacePlaceholder)) {
		showMsg($("#otclNamespace"));
		return false;
	}
   	if (otclInstructionsValue.includes(factoryclassPlaceholder)) {
		showMsg($("#factoryclass"));
		return false;
	}
});

$("#createOtclFile").click(function() {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var otclInstructions = $("#otclInstructions").val();
	if (!otclInstructions) {
		showMsg($("#nothingToSave"));
		return ;
	}
	$("#otclEditor").submit();
});

$("#flipOtcl").click(function() {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var otcl = $("#otclInstructions").val();
	if (!otcl) {
		showMsg($("#nothingToSave"));
		return;
	}
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	$("#infoLoss").show();
	$("#infoLoss").dialog({
		resizable: false,
	    modal: true,
		buttons: {
	        Ok: function() {
	          $( this ).dialog( "close" );
	      	var url = pageUrl.concat('flipOtcl');
	    	$.post( url, { otclInstructions: otcl }).done(function( data ) {
	      			$("#otclInstructions").val(data);
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
	        }
        }
	});
});

function showMsg(msgElement) {
	msgElement.show();
	msgElement.dialog({
		resizable: false,
	    modal: true,
		buttons: {
	        Ok: function() {
	          $( this ).dialog( "close" );
	        }
        }
	});
	return;
}
