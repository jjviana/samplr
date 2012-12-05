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

import java.util.List;

/**
 *
 * @author oracle
 */
public class RequestTimeResultsFilter implements ResultsFilter  {

    private long outputThreshold;

    public long getOutputThreshold() {
        return outputThreshold;
    }

    public void setOutputThreshold(long outputThreshold) {
        this.outputThreshold = outputThreshold;
    }

    public RequestTimeResultsFilter(long outputThreshold) {
        this.outputThreshold = outputThreshold;
    }
    
    
    
    public boolean shouldRecord(Request request, List<ResultFile> results) {
        
        return (request.getEndTime()-request.getStartTime())>=outputThreshold;
    }
    
}
