/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr.web.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author juliano
 */

@RunWith(Arquillian.class)
public class SamplrWebTest {
    
    public SamplrWebTest() {
    }

    
    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        
        WebArchive archive= ShrinkWrap.create(WebArchive.class,"test.war")
                .addClass(SamplrWebTestServlet.class);
                
        archive.setWebXML(SamplrWebTest.class.getResource("/test-config/WEB-INF/web.xml"));
       
        return archive;
        
    }
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testSamplrWebFilter() throws Exception {
        
        URL testURL=new URL("http://localhost:8181/test/test?n=5");
        
        InputStream in=testURL.openStream();
        
        
    }
    
    @BeforeClass
    public static void initOutputDirectory() {
        
         File testOutputDir=new File("samplr-output");
        if(testOutputDir.exists())
           deleteDir(testOutputDir);
        
        //testOutputDir.mkdir();
            
    }
    
    private static boolean deleteDir(File dir) {
        
        if(dir.isDirectory()) {
            String[] children=dir.list();
            for(int i=0;i<children.length;i++) {
                if(!children[i].equals(".") && !children[i].equals("..")) {
                    boolean success=deleteDir(new File(dir,children[i]));
                    if(!success)
                        return false;
                }
            }
            
        }
        
        return dir.delete();
        
        
    }
    
}
