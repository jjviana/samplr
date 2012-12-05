/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr.web.test;

import com.logicstyle.samplr.TestProcessingThread;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author juliano
 */
public class SamplrWebTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        int iterations=Integer.parseInt(req.getParameter("n"));
        TestProcessingThread  tp=new TestProcessingThread(iterations);
        tp.run();
    }

    
    
    
}
