/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr.web;

import com.logicstyle.samplr.Request;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author juliano
 */
public class SamplrWebRequest extends Request {
   
    private final HttpServletRequest request;
  
    public SamplrWebRequest(ServletRequest request) {
        if(!(request instanceof HttpServletRequest))
            throw new IllegalArgumentException("Only Http Servlet Requests supported at the moment");
        
                    
       this.request=(HttpServletRequest) request;
    }

    @Override
    public String toString() {
        StringBuffer result=new StringBuffer( super.toString());
        
        Map params=request.getParameterMap();
        
        result.append("\nWeb Request\n");
        result.append("======================\n\n");
        if(request instanceof HttpServletRequest) {
            
        }
        result.append(request.getMethod());
        result.append(" ");
        result.append(request.getRequestURI());
        
        if(request.getQueryString()!=null) {
            result.append("?");
            result.append(request.getQueryString());
        
        }
        
        result.append("\n");
        
        result.append("Client address:");
        result.append(request.getRemoteAddr());
        result.append(":");
        result.append(request.getRemotePort());
        result.append("\n");
        
        result.append("\nRequest Headers\n");
        result.append("======================\n\n");
        
        Enumeration headers=request.getHeaderNames();
        
        while(headers.hasMoreElements()) {
            String header=(String) headers.nextElement();
            String value=request.getHeader(header);
            result.append(header);
            result.append("=");
            result.append(value);
            result.append("\n");
        }
        
        result.append("\nRequest Parameters\n");
        result.append("======================\n\n");
        
        Map<String,String[]> parameters=request.getParameterMap();
        for(String param: parameters.keySet() ) {
            result.append(param);
            result.append("=");
            result.append(parameters.get(param)[0]);
        }
        
        if(request.getSession(false)!=null) {
            
            result.append("\nHttp Session Parameters\n");
            result.append("======================\n\n");
            HttpSession session=request.getSession(false);
            Enumeration attributes=session.getAttributeNames();
            while(attributes.hasMoreElements()) {
                String attr=(String) attributes.nextElement();
                String value=session.getAttribute(attr).toString();
                result.append(attr);
                result.append("=");
                result.append(value);
                result.append("\n");
                
            }
            
            
        }
        if(request.getCookies()!=null) {
            
            result.append("\nHttp Session Cookies\n");
            result.append("======================\n\n");
            for(Cookie cookie: request.getCookies()) {
                result.append("Name:\"");
                result.append(cookie.getName());
                result.append("\"");
                result.append(" Value:\"");
                result.append(cookie.getValue());
                result.append("\"");
                result.append(" Domain:\"");
                result.append(cookie.getDomain());
                result.append("\"");
                result.append(" Path:\"");
                result.append(cookie.getPath());
                result.append("\"");
                result.append(" Max Age:\"");
                result.append(cookie.getMaxAge());
                result.append("\"\n");
                
      
                
            }
            
        }
        
        return result.toString();
        
        
        
    }
    
    
    
}
