package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.negation;

//Author: Junebae Kye, Supervisor: Imre Solti
//Date: 06/27/2010
//
//Purpose: this program calls GenNegEx and defines negation scopes of sentences.  It determines whether a keyword is in the negation scope or not.
//If a keyword is in the negation scope, for example, 3 air hunger  "Denies shortness of breath, stridor, or AIR HUNGER."  Negated   Negated 
//Output looks as the following: Number TAB Phrase TAB Sentence TAB Dummystring TAB Decision TAB Decision by computer 


public class CallKit {

  // post: returns true if a keyword is in the negation scope. otherwise, returns false 
  public static boolean contains(String scope, String line, String keyWords) {
    //System.out.println("scope:"+scope);
    //System.out.println("line:"+line);
    //System.out.println("keys:"+keyWords);

    String[] token = line.split("\\s+");  
    String[] s = keyWords.trim().split("\\s+");  
    String[] number = scope.split("\\s+");
    int counts = 0;  
    
    int end = Integer.valueOf(number[2]);
    if(end >= token.length) end--;

    for (int i = Integer.valueOf(number[0]); i <= end; i++)
	    if (s.length == 1) {
        if (token[i].equals(s[0]))
          return true;
	    } else 
        if ((token.length - i) >= s.length) {
          String firstWord = token[i];
          if (firstWord.equals(s[0])) {
            counts++;
            for (int j = 1; j < s.length; j++) { 
              if (token[i + j].equals(s[j]))
                counts++;
              else {
                counts = 0;
                break;
              }
              if (counts == s.length)
                return true;
            }
          }
        }
    return false;
  }

  // post: removes punctuations
  public static String cleans(String line) {
    line = line.toLowerCase();
    if (line.contains("\""))
	    line = line.replaceAll("\"", "");
    if (line.contains(","))
	    line = line.replaceAll(",", "");  
    if (line.contains("."))
	    line = line.replaceAll("\\.", "");
    if (line.contains(";"))
	    line = line.replaceAll(";", "");
    if (line.contains(":"))
	    line = line.replaceAll(":", "");
    return line;
  }
}
