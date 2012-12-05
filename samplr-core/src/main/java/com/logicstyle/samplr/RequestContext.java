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
 * Allows RequestProcessots to notify the RequestManager that they are have stopped
 * recording measurements for a specific request. The results list can be null or empty indicating that
 * this processor has no measurements for the specified request.
 * @author juliano
 */
public interface RequestContext {
    
    
    public Request getRequest();
    
    public void measurementFinished(Request request,RequestProcessor processor,List<ResultFile> results);
    
}
