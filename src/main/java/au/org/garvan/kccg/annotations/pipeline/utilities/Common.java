package au.org.garvan.kccg.annotations.pipeline.utilities;

import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APToken;
import org.json.simple.JSONArray;

import java.util.List;

/**
 * Created by ahmed on 5/10/17.
 */
public final class Common {

    public static String getTrimmedText(APToken token) {
        return token.getOriginalText()
                .trim()
                .replace("(", "")
                .replace(")", "")
                .replace(".", "")
                .replace(";", "")
                .replace(":", "")
                .replace(",", "")
                .replace("-", "")
                .trim();

    }

    public JSONArray getJsonArrayFromStringList(List<String> lstString){
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(lstString);
        return jsonArray;

    }
}
