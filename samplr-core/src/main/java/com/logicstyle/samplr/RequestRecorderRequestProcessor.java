/**
 * Copyright 2012  Juliano Viana
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * This request processor records the request parameters.
 * @author juliano
 */
public class RequestRecorderRequestProcessor extends RequestProcessor<RequestRecorderRequestProcessor>{

    @Override
    public void startMeasuring(RequestContext context) {
        
        
        
    }

    @Override
    public void stopMeasuring(RequestContext context) {
       
        Request request=context.getRequest();
        
        ByteArrayInputStream in = new ByteArrayInputStream(request.toString().getBytes());
        
        ResultFile rf=new ResultFile();
        rf.setName("request.txt");
        rf.setContent(in);
        
        List<ResultFile> results=new ArrayList<ResultFile>();
        results.add(rf);
        
        if(request instanceof RequestInfoProvider) {
            results.addAll(((RequestInfoProvider)request).getRequestFiles());
            
        }
        
        context.measurementFinished(request, this, results);
        
        
    }
    
}
