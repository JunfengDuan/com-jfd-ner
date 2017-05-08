package com.bop.util;

/**
 * Created by jfd on 5/4/17.
 */
public interface NerType {

    String ORGANIZATION = "ORGANIZATION";

    String PERSON = "PERSON";

    String O = "O";

    String REGEX = "<.*?>([^a-zA-Z]+)</.*?>";

    String QUERY = "select AJNAME from GASJ01 order by AJNAME offset %s ROWS FETCH NEXT %s ROWS ONLY";

    String SAVE = "insert into N_GASJ01 (text,organization,person,other) values (%s)";

}
