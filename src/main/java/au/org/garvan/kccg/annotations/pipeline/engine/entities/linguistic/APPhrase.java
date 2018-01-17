package au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.PhraseType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class APPhrase extends LinguisticEntity {

    PhraseType phraseType;
    List<APToken> tokens;

    boolean isEmbedded = false;
    List<Integer> parentPhraseIds = new ArrayList<>();


    public Set<Integer> getOffsetBegins(){
        return tokens.stream().map(APToken::beginPosition).collect(Collectors.toSet());
    }

    public boolean isSubSetOff(APPhrase secondPhrase){
        return  secondPhrase.getOffsetBegins().containsAll(getOffsetBegins());
    }

    @Override
    public String getOriginalText(){
        return tokens.stream().map(t->t.getOriginalText()).collect(Collectors.joining(" "));

    }
    @Override
    public String toString(){
        return getOriginalText();
    }

}
