package au.org.garvan.kccg.annotations.pipeline.Utils;

import au.org.garvan.kccg.annotations.pipeline.entities.linguistic.APToken;

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
}
