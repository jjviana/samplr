
package com.logicstyle.samplr.examples;

import com.logicstyle.samplr.Request;


public class MyRequest extends Request {
    
    private Integer requestParam1;
    private String requestParam2;
    

    public MyRequest(Integer requestParam1, String requestParam2) {
        this.requestParam1 = requestParam1;
        this.requestParam2 = requestParam2;
    }

    @Override
    public String toString() {
        return super.toString()+
                "\nMyRequest{" + "requestParam1=" + requestParam1 + ", requestParam2=" + requestParam2 + "}\n";
    }
    
    
    
    
}
