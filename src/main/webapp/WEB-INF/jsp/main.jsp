<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>OTCL Editor</title>
	
	<link rel="icon" href="./images/otcl-logo.png">
	
	<link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
	<link href="<%=request.getContextPath()%>/jquery/dist/fixed/jquery.contextMenu.min.css" rel="stylesheet">
	<link href="<%=request.getContextPath()%>/jquery/dist/jquery.contextMenuCommon.min.css" rel="stylesheet">

	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
	
	<!-- from https://anseki.github.io/jquery-contextmenu-common/ -->
	<script src="<%=request.getContextPath()%>/jquery/dist/jquery-ui-position.min.js"></script>
	<script src="<%=request.getContextPath()%>/jquery/dist/fixed/jquery.contextMenu.min.js"></script>
	<script src="<%=request.getContextPath()%>/jquery/dist/jquery.contextMenuCommon.min.js"></script>
	
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />
	
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" />

</head>

<body>
	<div id="header">
		<img class="logo" src="./images/otcl-logo.png">OTCL Script Editor
	</div>

	<form id="otclEditor" action="createOtclFile" accept-charset="utf-8" method="post">
		<div class="box columnHdr">
			<div class="sourceDiv">
				<label><u><b>From</b></u></label>
			</div>
			<div class="targetDiv">
				<label><u><b>To</b></u></label>
			</div>
		</div>
		<div class="box">
			<div class="sourceDiv">
				<label class="pkgNType">Pkg/NS:</label>
				<input id="srcPkgName" type="text" value="com.kronos.airlines.dto" class="inpt"/>
				<input id='fetchSrc' type='button' value="Fetch Types"  class="btn"/>
			</div>
			<div class="targetDiv">
				<label class="pkgNType">Pkg/NS:</label>
				<input id="targetPkgName" type="text" value="com.athena.airlines.dto" class="inpt"/>
				<input id='fetchTarget' type='button' value="Fetch Types"  class="btn"/>
			</div>
		</div>
		
		<div class="box">
 			<div class="sourceDiv">
				<label class="pkgNType">Types:</label>
				<select type="select" id="srcClsNames" name="srcClsNames" class="inptSingle"> 
				</select>
			</div>
			<div class="targetDiv">
				<label class="pkgNType">Types:</label>
				<select type="select" id="targetClsNames" name="targetClsNames" class="inptSingle">
			 	</select>
			</div>
		</div>
		<div class="box" id="divTextArea">
			<textarea id="otclInstructions" name="otclInstructions" class="txtArea context-menu-one"
					placeholder="Right-click inside this text-area to select command-type."></textarea>
		</div>
		<div class="box">
			<div class="actions">
				<input id="showTree" type="button" value="Show Tree" class="actionBtn" >
			</div>

			<div class="actions">
				<input id="addScript" type="button" value="Add Script" class="actionBtn" >
			</div>
			<div class="actions">
				<input id="reset" type="button" value="Clear" class="actionBtn" >
			</div>
			<div class="actions">
				<input id="createOtclFile" type="button" value="Create file" class="actionBtn" />
			</div>
			<div class="actions">
				<input id="flipOtcl" type="button" value="Flip OTCL" class="actionBtn" />
			</div>
		</div>
		<div class="box">
			<div id="srcTree" class="sourceTree">&nbsp;
			</div>
			<div id="targetTree" class="targetTree">&nbsp;
			</div>
		</div>
	</form>

<!--  error messages -->
	<div id="typesNotSelected" title="Target/Source types" style="display: none">
	  <p>Cannot proceed - please select both Target and Source types.</p>
	</div>
	<div id="nothingToSave" title="Nothing to Save" style="display: none">
	  <p>Cannot proceed - Nothing to save!</p>
	</div>
	<div id="rootOtclChain" title="Root OTCL-Chain" style="display: none">
	  <p>Tree Root on any side cannot be selected to create OTCL-Script block!</p>
	</div>
	<div id="targetOtclChain" title="Target OTCL-Chain" style="display: none">
	  <p>Target OTCL-chain not selected!</p>
	</div>

	<div id="singleSideCollectionOnly" title="OTCL-Script::Collections on one side Only" style="display: none">
	  <p>For executeOtclConverter and executeOtclModule Otcl-scripts collections are allowed on any one side only.</p>
	</div>
	<div id="multipleAnchors" title="OTCL Chain::Multiple Anchors" style="display: none">
	  <p>OTCL-chain with multiple Anchors (^) is not valid.</p>
	  <p id="otclChain" />
	</div>

	<div id="sourceType" title="Source-Type" style="display: none">
	  <p>Please define value for &lt;&lt;sourceType&gt;&gt; in 'metadata: objectTypes: source:'.</p>
	</div>
	<div id="targetType" title="Target-Type" style="display: none">
	  <p>Please define value for &lt;&lt;targetType&gt;&gt; in 'metadata: objectTypes: target:'.</p>
	</div>
	<div id="mainClassName" title="Main-Class Name" style="display: none">
	  <p>Please define value for &lt;&lt;mainClassName&gt;&gt; in 'copy: or metadata: mainClassName:'.</p>
	</div>
	<div id="helperClassName" title="Helper-Class Name" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;helperClassName&gt;&gt; in 'copy: or metadata: helperClassName:'.</p>
	</div>
	<div id="factoryclass" title="Factory-Class Name" style="display: none">
	  <p>Please define value for &lt;&lt;factoryclass&gt;&gt; in 'copy: or execute: factoryclass:'.</p>
	</div>
	<div id="getter" title="Getter-Name" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;getter&gt;&gt; in 'copy: or execute: getter:'.</p>
	</div>
	<div id="setter" title="Setter-Name" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;setter&gt;&gt; in 'copy: or execute: setter:'.</p>
	</div>
	<div id="dateFormat" title="Date-Format String" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;dateFormat&gt;&gt; in 'copy: or execute: dateFormat:'.</p>
	</div>
	<div id="concreteType" title="Concrete-Type" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;concreteType&gt;&gt; in 'copy: or execute: concreteType:'.</p>
	</div>
	<div id="otclConverter" title="OTCL-Converter" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;otclConverter&gt;&gt; in 'execute: otclConverter:'.</p>
	</div>
	<div id="otclNamespace" title="OTCL-Namespace" style="display: none">
	  <p>Please define value or remove line for &lt;&lt;otclNamespace&gt;&gt; in 'execute: otclNamespace:'.</p>
	</div>
	<div id="infoLoss" title="Some info can be lost" style="display: none">
	  <p>Some information can be lost.</p>
	</div>

</body>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/main.js"></script>

</html>