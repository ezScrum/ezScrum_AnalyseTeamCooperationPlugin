<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<%@ page import="ntut.csie.ezScrum.plugin.PluginExtensioner" %>
</head>
<body>
<% PluginExtensioner pluginExtensioner = new PluginExtensioner("AnalyseTeamCooperationPlugin"); %>

<!-- javascript has only one way(request to action) to talk with action -->
<script type="text/javascript" src="<%=pluginExtensioner.getWebPluginRoot() %>webApp/plugin/analyseTeamCooperation/protocol/action/ProjectLeftTree.js"></script>
<script type="text/javascript" src="<%=pluginExtensioner.getWebPluginRoot() %>webApp/plugin/analyseTeamCooperation/protocol/action/ProjectPages.js"></script>
</body>
</html>