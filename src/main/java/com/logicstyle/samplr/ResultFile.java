/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.io.InputStream;

/**
 *
 * Encapsulates the name and the contents of a results "file".
 * Each file is a set of data that should be packed together with the results.
 * The set of data may contain the stack traces, request parameters and other information.
 * @author juliano
 */
class ResultFile {
    private String name;
    
    private InputStream content;

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
