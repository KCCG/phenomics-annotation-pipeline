package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class Constants {

    @Getter
    private static final List<String> drugFilters = Arrays.asList("rabbit","sulfur","pork","parsley","creatinine","glucose","duck","peppermint","lactose","caffeine","pear","nitrogen","rice","crab","silver","nadh","potato","ginger","chicken","garlic","lamb","almond","turkey","watermelon","paprika","grapefruit","silicon","cabbage","belladonna","cherry","strawberry","cucumber","squash","banana","water","peanut","clove","oyster","beef","beet","peach","radish","tomato","wheat","talc","cinnamon","goose","iron","opium","zinc","soybean","cotton","platinum","oxygen","plum","barley","orange","cholesterol","copper","trout","blueberry","lobster","coconut","apple","raspberry","date","onion","corn","cauliflower","shrimp","lime","carrot","catfish","balance","carbon","vessel","igf1","ggf2","herring","salmon","thyroid","success","silica","better","codfish","turnip");



    @Getter
    private static final List<String> commonFilterWords = Arrays.asList("t","poly","poly t", "staff", "equal", "at 10", "grape", "13-ra", "dilate","cashew", "same","there", "lamb", "blood");



}
