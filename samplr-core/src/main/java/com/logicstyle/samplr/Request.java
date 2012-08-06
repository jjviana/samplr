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

import java.util.Date;

/**
 * Contains basic information about a Request.
 * A Request is any work being performed by a thread that can be of interest
 * for the sampling system.
 * @author juliano
 */
public class Request {
    
    
    /**
     * The request id. It identifies uniquely a request within a Samplr instance.
     */
    private long id;

   
    
    private long startTime;
    private long endTime;
    
    
   
    
    
    
    
    /**
     * The id of the thread associated with this request 
     **/
     
    private long threadId;
    
    
    
    public Request() {
        
        assignRequestId();
        threadId=Thread.currentThread().getId();
        startTime=System.currentTimeMillis();
        
    }
    
    
    public Request(long threadId,long startTime) {
        
        assignRequestId();
        this.threadId=threadId;
        this.startTime=startTime;
        
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

   
    
    public boolean isFinished() {
        
        return endTime>0;
    }
    
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    private static long requestIdCounter;
    
    private synchronized void assignRequestId() {
        id=requestIdCounter++;
    }

    @Override
    public String toString() {
        
        StringBuffer sb=new StringBuffer();
        sb.append("Request: ");
        sb.append(getId());
        sb.append("\n");
        sb.append("Start Time: ");
        sb.append(new Date(startTime));
        sb.append("\n");
        sb.append("End time: ");
        if(endTime!=0)
            sb.append(new Date(endTime));
        else
            sb.append("not available");
        
        sb.append("\n");
        sb.append("Thread id: ");
        sb.append(threadId);
        sb.append("\n");
        
        return sb.toString();
        
    }

    
    
    
    
    


    
    
}
