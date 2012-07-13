/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.util.List;

/**
 * Interface implemented by custom implementations of Request in order to contribute
 *  more information (such as detailed parameter descriptions beyond what is possible with toString).
 * @author juliano
 */
public interface RequestInfoProvider {
    
    
    public List<ResultFile> getRequestFiles();
    
}
