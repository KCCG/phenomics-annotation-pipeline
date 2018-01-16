package au.org.garvan.kccg.annotations.pipeline.model;

import lombok.Data;

/**
 * Created by ahmed on 8/1/18.
 */
@Data
public class PaginationRequestParams {

    Integer pageSize;
    Integer pageNo;
    Integer totalArticles;
    Integer totalPages;


    public PaginationRequestParams(Integer pSize, Integer pNo){
        pageSize = pSize==null? 50: pSize;
        pageNo = pNo==null? 1: pNo;
        totalArticles =0;
        totalPages=0;
    }

}
