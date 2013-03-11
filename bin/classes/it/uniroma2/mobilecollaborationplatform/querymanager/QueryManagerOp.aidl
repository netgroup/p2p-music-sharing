package it.uniroma2.mobilecollaborationplatform.querymanager;

interface QueryManagerOp {
	String getQueryResponse(String query);
	String getFile(String digest);
}