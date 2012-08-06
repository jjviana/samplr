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

package com.logicstyle.samplr;

import java.util.List;

/**
 * Defines the common interface for all result processors.
 *
 * @author juliano
 */
public interface  ResultsProcessor {
    
    /**
     * Processes the provided request results.
     * Once this method returns it is assumed that the request processor has kept no references 
     * to the request or the resultFiles.
     * @param request - the request related to this result.
     * @param resultFiles  - the list of result files available from this request.
     */
    
    public void processResult(Request request,List<ResultFile> resultFiles);
    
    
    
}
