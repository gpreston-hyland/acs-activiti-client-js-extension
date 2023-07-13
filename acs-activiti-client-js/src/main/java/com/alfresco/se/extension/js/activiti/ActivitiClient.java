package com.alfresco.se.extension.js.activiti;

import com.alfresco.se.extension.js.http.HTTPRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
//import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.extensions.webscripts.json.JSONUtils;

public class ActivitiClient extends BaseProcessorExtension {
  private static Log logger = LogFactory.getLog(ActivitiClient.class);
  
  private String activitiEndpoint;
  
  private String user;
  
  private String password;
  
  private String alfrescoActivitiRepositoryId = "alfresco-1";
  
  private ContentService contentService;
  
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }
  
  public ScriptableObject startProcess(String processName, String name, ScriptableObject scriptableObject) throws JSONException, IOException {
    return startDocumentProcess(processName, name, scriptableObject, null, null);
  }
  
  public ScriptableObject startDocumentProcess(String processName, String name, ScriptableObject scriptableObject, String[] documentPropertyNames, ScriptNode[] documents) throws JSONException, IOException {
    String[] documentIds = new String[documents.length];
    for (int i = documents.length; i > 0; i--)
      documentIds[i - 1] = postContent(documents[i - 1]); 
    return start(processName, name, scriptableObject, documentPropertyNames, documentIds);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
private ScriptableObject start(String processName, String name, ScriptableObject scriptableObject, String[] documentPropertyNames, String[] documentIds) throws IOException, JSONException {
    JSONObject json = new JSONObject();
    JSONUtils jsonUtils = new JSONUtils();
    JSONObject values = jsonUtils.toJSONObject(scriptableObject);
    for (int i = documentPropertyNames.length; i > 0; i--)
      values.put(documentPropertyNames[i - 1], documentIds[i - 1]); 
    json.put("name", name);
    json.put("values", (Map)values);
    json.put("processDefinitionId", getProcessDefinitionId(processName));
    HTTPRequest request = new HTTPRequest();
    String httpUrl = this.activitiEndpoint + "/api/enterprise/process-instances";
    logger.debug("URL:" + httpUrl);
    logger.debug("Paylod:" + json.toString());
    String response = request.post(httpUrl, json.toString(), "application/json", this.user, this.password);
    return jsonUtils.toObject(response);
  }
  
  private String postContent(ScriptNode document) throws IOException, JSONException {
    return postContent(document.getName(), document.getId(), document.getSiteShortName(), document.getMimetype());
  }
  
  @SuppressWarnings("unchecked")
private String postContent(String documentName, String documentId, String siteShortName, String mimeType) throws IOException, JSONException {
    HTTPRequest request = new HTTPRequest();
    JSONObject json = new JSONObject();
    JSONUtils jsonUtils = new JSONUtils();
    json.put("name", documentName);
    json.put("link", true);
    json.put("source", this.alfrescoActivitiRepositoryId);
    json.put("sourceId", documentId + "@" + siteShortName);
    json.put("mimeType", mimeType);
    String response = request.post(this.activitiEndpoint + "/api/enterprise/content", json.toString(), "application/json", this.user, this.password);
    JSONObject answer = jsonUtils.toJSONObject(jsonUtils.toObject(response));
    return String.valueOf(answer.get("id"));
  }
  
  @SuppressWarnings("unchecked")
private String getProcessDefinitionId(String processName) throws IOException, JSONException {
    HTTPRequest request = new HTTPRequest();
    JSONUtils jsonUtils = new JSONUtils();
    String response = request.get(this.activitiEndpoint + "/api/enterprise/process-definitions", this.user, this.password);
    JSONObject json = jsonUtils.toJSONObject(jsonUtils.toObject(response));
    JSONArray data = (JSONArray)json.get("data");
    Iterator<JSONObject> iter = data.iterator();
    while (iter.hasNext()) {
      JSONObject definition = iter.next();
      if (definition.get("name").equals(processName))
        return (String)definition.get("id"); 
    } 
    return null;
  }
  
  public void setActivitiEndpoint(String activitiEndpoint) {
    this.activitiEndpoint = activitiEndpoint;
  }
  
  public void setUser(String user) {
    this.user = user;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public void setAlfrescoActivitiRepositoryId(String alfrescoActivitiRepositoryId) {
    this.alfrescoActivitiRepositoryId = alfrescoActivitiRepositoryId;
  }
  
  public void saveLocalDocument(int activitiContentId, String documentName, ScriptNode parent, String mimeType) throws IOException {
    HTTPRequest request = new HTTPRequest();
    InputStream response = null;
    OutputStream outputStream = null;
    try {
      response = request.getStream(this.activitiEndpoint + "/api/enterprise/content/" + activitiContentId + "/raw", this.user, this.password);
      ScriptNode doc = parent.createFile(documentName);
      ContentWriter writer = this.contentService.getWriter(doc.getNodeRef(), ContentModel.PROP_CONTENT, true);
      if (mimeType != null)
        writer.setMimetype(mimeType); 
      outputStream = writer.getContentOutputStream();
      int read = 0;
      byte[] bytes = new byte[1024];
      while ((read = response.read(bytes)) != -1)
        outputStream.write(bytes, 0, read); 
    } finally {
      if (response != null)
        try {
          response.close();
        } catch (Exception e1) {} 
      if (outputStream != null)
        try {
          outputStream.close();
        } catch (Exception e1) {} 
    } 
  }
  
//  public static void main(String[] args) {
//    ActivitiClient client = new ActivitiClient();
//    client.setActivitiEndpoint("http://192.168.99.223:9090/activiti-app");
//    client.setUser("rui");
//    client.setPassword("rui");
//    try {
//      HTTPRequest request = new HTTPRequest();
//      InputStream response = null;
//      OutputStream outputStream = null;
//      try {
//        response = request.getStream("http://192.168.99.223:9090/activiti-app/api/enterprise/content/9024/raw", "rui", "rui");
//        outputStream = new FileOutputStream(new File("test.docx"));
//        int read = 0;
//        byte[] bytes = new byte[1024];
//        while ((read = response.read(bytes)) != -1)
//          outputStream.write(bytes, 0, read); 
//      } finally {
//        if (response != null)
//          try {
//            response.close();
//          } catch (Exception e1) {} 
//        if (outputStream != null)
//          try {
//            outputStream.close();
//          } catch (Exception e1) {} 
//      } 
//      System.out.println("Done!");
//    } catch (Exception e) {
//      e.printStackTrace();
//    } 
//  }
  
  
}

