
package com.logicstyle.samplr.examples;

import com.logicstyle.samplr.RequestManager;


public class MyApp {
    
    static  RequestManager requestManager;
    
    public  void processRequest(Integer param1,String param2) {
        
         MyRequest request=new MyRequest(param1, param2);
        
        requestManager.requestStarting(request);
        
        // Process the request as usual...
        
        
        requestManager.requestFinished(request);
          
    }
}
