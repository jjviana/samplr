/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.io.*;
import org.openide.util.Exceptions;

/**
 *
 * Encapsulates the name and the contents of a results "file". Each file is a
 * set of data that should be packed together with the results. The set of data
 * may contain the stack traces, request parameters and other information.
 *
 * @author juliano
 */
public class ResultFile {

    private String name;
    /**
     * The stream connected to the file contents.
     *
     */
    private InputStream content;
    private File tempFile;

    public InputStream getContent() {
        if(content!=null)
            return content;
        
        if(tempFile==null)
            throw new RuntimeException("Internal error: both content and tempFile are null");
        try {
            return new FileInputStream(tempFile);
        } catch (FileNotFoundException ex) {
           throw new RuntimeException(ex);
        }
    }

    public void setContent(InputStream content) {


        if (content.markSupported()) {
            this.content = content;
        } else {
            FileOutputStream out = null;
            try {
                tempFile = File.createTempFile("ResultFileTmp", "samplr");
                byte[] buf = new byte[8192];
                int c;
                out = new FileOutputStream(tempFile);
                while ((c = content.read(buf)) != -1) {
                    out.write(buf, 0, c);
                }

                out.flush();



            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }


            }



        }
    }

    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    public void close() {
        
        if(tempFile!=null)
            tempFile.delete();
        
        
    }
}
