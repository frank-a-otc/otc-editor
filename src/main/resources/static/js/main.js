String.prototype.replaceAt=function(index, character) {
    return this.substr(0, index) + character + this.substr(index+character.length);
};

let map = new Map();

var isInitialized = false;

var selectMsg = "Select...";
var pageUrl = "";
var header = 
"metadata:\r\n" +
"  labels:\r\n" +
"    targetType: <targetType-placeholder>\r\n" +
"    sourceType: <sourceType-placeholder>\r\n" +
"  objectFactory:\r\n" +
"scripts:\r\n";

var normalScript =
"  - targetOtclChain: <targetOtclChain-placeholder>\r\n" +
"    sourceOtclChain: <sourceOtclChain-placeholder>\r\n";

var setValue = 
"  - targetOtclChain: <targetOtclChain-placeholder>\r\n" +
"    setValue:\r\n" +
"      values: []\r\n";

var executeOtclConverter =
normalScript +
"    executeOtclConverter:\r\n" +
"      otclConverter: <otclConverter-placeholder>\r\n";

var executeOtclModule =
normalScript +
"    executeOtclModule:\r\n" +
"      otclNamespace:\r\n";

$(document).ready(function() {
	var url = pageUrl.concat('showConverters');
	getClassNames(url, 'converters');
	
});

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

$("#otclScriptType").on('change', function() {
	var val = this.value;
	if (val == "executeOtclConverter") {
		$("#converters").prop('disabled', false);
	} else {
		$("#converters")[0].selectedIndex = 0;
		$("#converters").prop('disabled', true);
	}
})

$("#displayTree").click(function() {
	$("#otclInstructions").val('');
	var didSucceed = fetchAndPopulateJstree(pageUrl.concat('fetchJsTreeData'));
	if (!didSucceed) {
		return;
	}
    $('#targetTree').on('loaded.jstree', function() {
   	 $("#targetTree").jstree("open_node", $("#otcl-root"));
   });
    $('#srcTree').on('loaded.jstree', function() {
   	 $("#srcTree").jstree("open_node", $("#otcl-root"));
   });
});

function popMenu($node) {
    var otclChain = $node.id;
	var items = {
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
		        	var leftOtclChain = otclChain.substring(0, lastIndexOf + 1);
		        	var rightOtclChain;
		        	if (otclChain.charAt(lastIndexOf + 1) == "*") {
		        		rightOtclChain = otclChain.substring(lastIndexOf + 2);
		            } else {
		        		rightOtclChain = otclChain.substring(lastIndexOf + 1);
		            }
		        	var anchoredOtclChain = leftOtclChain + "^" + rightOtclChain;
		        	map.set(otclChain, anchoredOtclChain);
		        }
			}
	   	}
	};
    if (map.has(otclChain)) {
    	items.anchor.label = "Anchor off"
    } else {
    	items.anchor.label = "Anchor on"
    }
	if (otclChain.includes("]")) {
		return items;
	}
};

function fetchAndPopulateJstree(url) {
	$('#srcTree').jstree("destroy").empty();
	$('#targetTree').jstree("destroy").empty();
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	var url = url.concat('?srcClsName=').concat(srcClsName);
	url = url.concat('&targetClsName=').concat(targetClsName);
	$.jstree.defaults.core.force_text = true;
    $.ajax({
        url : url,
        dataType:"json",
        success : function(response) {
        	$('#srcTree').jstree({
        		'core': {
        			'data': response.sourceFieldNames
        		}
        	});
        	$('#targetTree').jstree({
        		'core': {
        			'data': response.targetFieldNames
        		},
        		"plugins": ["contextmenu"],
        		"contextmenu": {
        			"items": popMenu
        		}
        	});
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

$("#createScript").click(function( event ) {
    var targetNode = $('#targetTree').jstree(true).get_selected(true);
   	var targetOtclChain;
   	if (targetNode[0]) {
   		if (targetNode[0].id == 'otcl-root') {
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
    var srcNode = $('#srcTree').jstree(true).get_selected(true);
	var sourceOtclChain;
   	if (srcNode[0]) {
   		if (srcNode[0].id == 'otcl-root') {
        	$("#rootOtclChain").show();
        	$("#rootOtclChain").dialog({resizable: false}).dialog("open");
        	return;
   		}
	  	sourceOtclChain = srcNode[0].id;
   	}
   	var otclExpression;
    var otclScriptType = $('#otclScriptType').val();
	var converter = null;
	if (map.has(targetOtclChain)) {
		targetOtclChain = map.get(targetOtclChain);
	}
    if (otclScriptType == "setValue") {
        otclExpression = setValue.replace("<targetOtclChain-placeholder>", targetOtclChain);
        $( "#setValue").show();
        $( "#setValue").dialog({width: 400,resizable: false}).dialog("open");
    } else if (otclScriptType == "executeOtclConverter" || otclScriptType == "executeOtclModule") {
    	if (!sourceOtclChain) {
        	$("#sourceOtclChain").show();
        	$("#sourceOtclChain").dialog({resizable: false}).dialog("open");
       		return;
    	}
    	var isValid = isValidCollections(targetOtclChain, sourceOtclChain);
   		if (!isValid) {
   			return;
   		}
   		isValid = isAnchorsValid(targetOtclChain);
   		if (!isValid) {
   			return;
   		}
        if (otclScriptType == "executeOtclConverter") {
            converter = $('#converters').val();
            otclExpression = executeOtclConverter.replace("<targetOtclChain-placeholder>", targetOtclChain)
					.replace("<sourceOtclChain-placeholder>", sourceOtclChain);
            if (!converter) {
                $( "#executeOtclConverter").show();
                $( "#executeOtclConverter").dialog({width: 500,resizable: false}).dialog("open");
        	} else {
        		otclExpression = otclExpression.replace("<otclConverter-placeholder>", converter);
        	}
        } else if (otclScriptType == "executeOtclModule") {
            otclExpression = executeOtclModule.replace("<targetOtclChain-placeholder>", targetOtclChain)
				.replace("<sourceOtclChain-placeholder>", sourceOtclChain);
            $( "#executeOtclModule").show();
            $( "#executeOtclModule").dialog({width: 500,resizable: false}).dialog("open");
        }
    } else {
//    	-- this is not applicable here cos this is map-key is user-entered - it should be done on submit.
//    	var isValid = isMapkeyValid(targetOtclChain);
//    	if (!isValid) {
//    		return;
//    	}
    	if (!sourceOtclChain) {
        	$("#sourceOtclChain").show();
        	$("#sourceOtclChain").dialog({resizable: false}).dialog("open");
       		return;
    	}
    	otclExpression = normalScript.replace("<targetOtclChain-placeholder>", targetOtclChain)
    		.replace("<sourceOtclChain-placeholder>", sourceOtclChain);
    }
   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	
   	if (!isInitialized || otclInstructionsValue.trim() == "") {
   		otclInstructions.val(header);
   		isInitialized = true;
   		otclInstructionsValue = header;
   	} else {
   		otclInstructionsValue = otclInstructionsValue.concat('\n');
   	}
   	otclInstructions.val(otclInstructionsValue.concat(otclExpression));
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

function isMapkeyValid(otclChain) {
	// not valid if map-key is present but is a leaf-node and key-type (<K>)
   	var idx = otclChain.indexOf("[<'");
   	if (idx < 0) {
   		idx = otclChain.indexOf("[<\"");
   	}
   	if (idx > 0) {
   		var idxDot = otclChain.indexOf(".", idx);
   		if (idxDot > 0) {
   	    	$("#mapkeyOnLeaf").show();
   	    	$("#mapkeyOnLeaf").dialog({resizable: false}).dialog("open");
   	    	return false;
   		}
   	}   	
	return true;
}

$("#reset").click(function() {
	$("#otclInstructions").val('');
	isInitialized = false;
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

$("#createFlippedFile").click(function() {
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
