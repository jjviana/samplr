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
 * Defines the interface of a filter that can decide wether results for a specific request
 * should be output.
 * @author oracle
 */
public interface ResultsFilter {
    
    public boolean shouldRecord(Request request,List<ResultFile> results);
    
}
