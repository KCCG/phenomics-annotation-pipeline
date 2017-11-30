package au.org.garvan.kccg.annotations.pipeline.engine.lexicons;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ahmed on 29/9/17.
 */
public class BaseLexiconHandler {

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


    protected void readFile(String delim) throws FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource("lexicons/" + fileName).getFile());
        Scanner scan = new Scanner(file);
        String headerLine = scan.nextLine();
        fileHeader = Arrays.asList(headerLine.split(delim));
        while(scan.hasNext()){
            String curLine = scan.nextLine();
            data.add(Arrays.asList(curLine.split(delim)));
            }
        scan.close();
    }


     protected boolean verifyHeader(List<String> customeHeader) {
        //Point: Check the Header and in case file got different header than reject the file.
        return fileHeader.toString().equals(customeHeader.toString());

    }


}
