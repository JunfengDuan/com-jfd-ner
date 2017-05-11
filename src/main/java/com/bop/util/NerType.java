package com.bop.util;

/**
 * Created by jfd on 5/4/17.
 */
public interface NerType {

    String ORGANIZATION = "ORGANIZATION";

    String PERSON = "PERSON";

    String O = "O";

    String REGEX = "<.*?>([^a-zA-Z]+)</.*?>";

//    String QUERY = "select AJNAME from GASJ01 order by AJNAME offset %s ROWS FETCH NEXT %s ROWS ONLY";
    String QUERY = "select id,%s from (select rownum rn, id, %s from %s " +
        "where rownum<= %s order by id ) where rn > %s";

    String SAVE = "insert into %s (id,name,organization,person,others) values (%s)";

    String THREAD_NAME_FORMAT = "ner-thread-%d";


    /**
     * 误报率(假阳性)
     */
    String FP = "select organization from N_FAZHI not in (select org_name from org01)";

    /**
     *漏报率(假阴性)
     */
    String FN = "select count(*) from N_FAZHI where organization is null and person is null and length(name)>4";

}
