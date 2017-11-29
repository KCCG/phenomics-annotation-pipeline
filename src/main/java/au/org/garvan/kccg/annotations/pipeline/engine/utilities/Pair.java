package au.org.garvan.kccg.annotations.pipeline.engine.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ahmed on 29/11/17.
 */
@AllArgsConstructor
public class Pair<F, S> {

    @Getter
    private final F first; //first member of pair

    @Getter
    private final S second; //second member of pair


    public static <F, S> Pair<F, S> of(F first, S second){
        return new Pair(first, second);
    }



}




