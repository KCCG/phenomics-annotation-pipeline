package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 29/9/17.
 */
public class BaseLexiconHandler {
    private final Logger slf4jLogger = LoggerFactory.getLogger(BaseLexiconHandler.class);


    protected String fileName;
    
    @Getter
    @Setter
    protected List<List<String>> data;

    @Getter
    @Setter
    protected List<String> fileHeader;


    public BaseLexiconHandler() {
        data = new ArrayList<>();
        fileName = "";
    }

//
//    protected void readFile(String delim) throws FileNotFoundException {
//
//
//        File file = new File(getClass().getClassLoader().getResource("annotators/" + fileName).getFile());
//
//        Scanner scan = new Scanner(file);
//        String headerLine = scan.nextLine();
//        fileHeader = Arrays.asList(headerLine.split(delim));
//        while(scan.hasNext()){
//            String curLine = scan.nextLine();
//            data.add(Arrays.asList(curLine.split(delim)));
//            }
//        scan.close();
//    }



    protected void readFile(String delim) throws IOException {

        slf4jLogger.info(String.format("Reading lexicon. Filename:%s", fileName));
        String path = "lexicons/" + fileName;
        InputStream input = getClass().getResourceAsStream("resources/" + path);
        if (input == null) {
            // this is how we load file within editor (eg eclipse)
            input = BaseLexiconHandler.class.getClassLoader().getResourceAsStream(path);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String headerLine = reader.readLine();
        fileHeader = Arrays.asList(headerLine.split(delim));
        String line;
        while ((line = reader.readLine()) != null) {
            data.add(Arrays.asList(line.split(delim)));
        }

        reader.close();

    }






     protected boolean verifyHeader(List<String> customeHeader) {
        //Point: Check the Header and in case file got different header than reject the file.
        return fileHeader.toString().equals(customeHeader.toString());

    }


}
