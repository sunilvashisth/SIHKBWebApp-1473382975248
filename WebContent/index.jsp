<%@ page language="java"
      contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true"
    import="java.util.Set"
    import="java.security.Principal"
    import="javax.security.auth.Subject"
    import="com.ibm.websphere.security.auth.WSSubject"
   %>
   <%
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-control", "no-store");
    Subject s = WSSubject.getCallerSubject();
    String username="unknown";
    if (s != null) {
        Set<Principal> principals = s.getPrincipals();
        if (principals != null && principals.size() > 0) {
            // in production this should be html encoded for safety
            username = principals.iterator().next().getName();
        }
    }
   %>
   <html>
      <h3>Hello  <%=username%></h3>
      <p class='description'></p> Thanks for trying SAML enterprise SSO demo with Liberty for Java Application</span>. 
   </html>