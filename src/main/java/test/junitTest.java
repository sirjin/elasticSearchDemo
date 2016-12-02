package test;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengjin on 2016/12/1.
 */
public class junitTest {

    private static final String INDEX = "fengtest";
    private static final String TYPE = "fengtype";
    private static final String ID = "1";

    @Test
    public void test(){

        Map<String, Object> json = new HashMap<String, Object>();
        json.put("user", "byor");
        json.put("postDate", new Date());
        json.put("message", "time out");
        json.put("context", "偏yaogggg");

        //elasticUtil.updateDocument(json,INDEX,TYPE,"5");
        //elasticUtil.upsertDocument(json,INDEX,TYPE,"6");
        //elasticUtil.createDocument(json,INDEX,TYPE,ID);

        //elasticUtil.getDocument(INDEX,TYPE,ID);
        elasticUtil.search(INDEX,TYPE,"message","偏");
        //elasticUtil.deleteDocument(INDEX,TYPE,ID);
        //elasticUtil.count(INDEX,"_type",TYPE);
    }
}
