/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr.web;

import com.logicstyle.samplr.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author juliano
 */
public class SamplrWebFilter implements Filter {

    private static final boolean debug = false;
    private FilterConfig filterConfig = null;
    public static String REQUEST_SAMPLING_THRESHOLD_PARAM = "requestSamplingThreshold";
    public static String REQUEST_TIMEOUT_PARAM = "requestTimeout";
    public static String REQUEST_FILTER_THRESHOLD = "requestFilterThreshold";
    
    public static String OUTPUT_DIRECTORY_PARAM="outputDirectory";
    
    private long requestSamplingThreshold;
    private long requestTimeout;
    private long requestFilterThreshold;
    
    private File outputDirectory;
    
    private RequestManager requestManager;
    private static final long DEFAULT_REQUEST_SAMPLING_THRESHOLD=30000; // 30 seconds
    private static final long DEFAULT_REQUEST_TIMEOUT=300000; // 5 minutes

    public long getRequestFilterThreshold() {
        return requestFilterThreshold;
    }

    public void setRequestFilterThreshold(long requestFilterThreshold) {
        this.requestFilterThreshold = requestFilterThreshold;
    }

    public long getRequestSamplingThreshold() {
        return requestSamplingThreshold;
    }

    public void setRequestSamplingThreshold(long requestSamplingThreshold) {
        this.requestSamplingThreshold = requestSamplingThreshold;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public SamplrWebFilter() {
    }

    

    

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (debug) {
            log("SamplrWebFilter:doFilter()");
        }
        
        
        SamplrWebRequest samplrRequest=new SamplrWebRequest(request);
        requestManager.requestStarting(samplrRequest);
        
        Throwable problem = null;

        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            // If an exception is thrown somewhere down the filter chain,
            // we still want to execute our after processing, and then
            // rethrow the problem after that.
            problem = t;
            t.printStackTrace();
        }

        requestManager.requestFinished(samplrRequest);

        // If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }
    }

   
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

   
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    
    public void destroy() {
        if(requestManager!=null)
            requestManager.shutdown();
    }

    
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("SamplrWebFilter: Initializing filter");

            }
            requestSamplingThreshold = getLongParamIfAvailable(REQUEST_SAMPLING_THRESHOLD_PARAM);
            requestTimeout = getLongParamIfAvailable(REQUEST_TIMEOUT_PARAM);
            requestFilterThreshold = getLongParamIfAvailable(REQUEST_FILTER_THRESHOLD);
            
            
            if(requestSamplingThreshold==0)
                requestSamplingThreshold=DEFAULT_REQUEST_SAMPLING_THRESHOLD;
            
            if(requestTimeout==0)
                requestTimeout=DEFAULT_REQUEST_TIMEOUT;
            
            
            if(filterConfig!=null && filterConfig.getInitParameter(OUTPUT_DIRECTORY_PARAM)!=null) {
                outputDirectory=new File(filterConfig.getInitParameter(OUTPUT_DIRECTORY_PARAM));
                if(!outputDirectory.exists()) {
                    if(!outputDirectory.mkdirs()) {
                        throw new RuntimeException("Unable to create directory "+outputDirectory.getAbsolutePath());
                    }
                }
                else {
                    if(!outputDirectory.isDirectory())
                        throw new RuntimeException("Path is not a directory: "+outputDirectory.getAbsolutePath());
                    
                }
                    
                
            }
            else {
                File tempDir=(File) filterConfig.getServletContext().getAttribute("javax.servlet.context.tempdir" );
                outputDirectory=new File(tempDir,"samplrOutput");
                if(!outputDirectory.mkdir()) {
                    throw new RuntimeException("Unable to make temporary directory: "+outputDirectory.getAbsolutePath());
                }
            }
            
            requestManager=new RequestManager();
            
            requestManager=new RequestManager()
                .withRequestProcessor(new ThreadSamplingRequestProcessor()
                                           .withRequestLengthSamplingThreshold(requestSamplingThreshold))
                .withRequestProcessor(new RequestRecorderRequestProcessor())
                .withResultsProcessor(new FileResultsArchiver()
                                          .withOutputDirectory(outputDirectory))
                .withRequestTimeout(requestTimeout);
               
            
            if(requestFilterThreshold!=0) {
                 for (RequestProcessor rp: requestManager.getRequestProcessors()) {
                     rp.setResultsFilter(new RequestTimeResultsFilter(requestFilterThreshold));
            
                }
            }
           
            requestManager.start();
            
            if(debug) {
                log("Started Samplr request manager");
            }
 



        }
    }

    
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("SamplrWebFilter()");
        }
        StringBuffer sb = new StringBuffer("SamplrWebFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());

    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

    private long getLongParamIfAvailable(String param) {

        String paramValue = getFilterConfig().getInitParameter(param);
        if (getFilterConfig() != null && paramValue != null) {

            try {
                return Long.parseLong(paramValue);
            } catch (Exception e) {
                log("Error parsing parameter value for parameter " + param);
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        }
        return 0;

    }

    
   
}
