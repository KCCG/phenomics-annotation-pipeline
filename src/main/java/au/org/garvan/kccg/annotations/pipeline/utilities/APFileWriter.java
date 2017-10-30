package au.org.garvan.kccg.annotations.pipeline.utilities;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 26/7/17.
 */
public class APFileWriter {
    public static void main(String[] args) throws IOException {
        List<String> lines = Arrays.asList(new String[] { "This is the content to write into file" });
        writeSmallTextFile(lines, "test.txt");
    }

    public static void writeSmallTextFile(List<String> aLines, String aFileName) throws IOException {

        String localPath =  System.getProperty("user.dir") + "/Analysis/";
        Path path = Paths.get(localPath+ aFileName + ".txt");
        Files.write(path, aLines, StandardCharsets.UTF_8);
    }
}

