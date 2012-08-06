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

import java.io.*;
import java.util.List;

/**
 * Implements a results processor that saves results to a set of directories in
 * a file system.
 *
 * @author juliano
 */
public class FileResultsArchiver implements ResultsProcessor {

    private File outputDirectory;

    public void processResult(Request request, List<ResultFile> resultFiles) {

        File resultsDir = new File(outputDirectory, String.valueOf(request.getId()));

        if (!resultsDir.mkdirs()) {
            throw new RuntimeException("Cannot create diretory to output results: " + resultsDir.getAbsolutePath());
        }

        try {
            for (ResultFile resultFile : resultFiles) {
                writeFile(resultsDir, resultFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }




    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        

        this.outputDirectory = outputDirectory;
    }

    public FileResultsArchiver withOutputDirectory(File outputDirectory) {
        setOutputDirectory(outputDirectory);
        return this;
    }

    private void writeFile(File directory, ResultFile resultFile) throws FileNotFoundException, IOException {


        File outputFile = new File(directory, resultFile.getName());
        FileOutputStream out = new FileOutputStream(outputFile);

        byte[] tmp = new byte[8192];
        int c;
        try {

            InputStream in = resultFile.getContent();
            while ((c = in.read(tmp)) != -1) {
                out.write(tmp, 0, c);
            }


            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }



    }
}
