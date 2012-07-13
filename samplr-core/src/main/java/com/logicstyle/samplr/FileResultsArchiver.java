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
