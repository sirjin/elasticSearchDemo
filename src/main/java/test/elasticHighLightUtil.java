package test;


import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengjin on 2016/12/5.
 */
public class elasticHighLightUtil {

    public static final String CLUSTER_NAME = "elasticsearch"; //实例名称
    private static final String IP = "127.0.0.1";
    private static final int PORT = 9300;  //端口

    //1.设置集群名称：默认是elasticsearch，并设置client.transport.sniff为true，使客户端嗅探整个集群状态，把集群中的其他机器IP加入到客户端中
    //对ES2.0有效
    private static Settings settings = Settings
            .settingsBuilder()
            .put("cluster.name",CLUSTER_NAME)
            .put("client.transport.sniff", true)
            .build();

    //创建私有对象
    private static TransportClient client;

    //反射机制创建单例的TransportClient对象
    //ES2.0版本
    static {
        try {
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP), PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 索引内全局检索关键字
     * @param index 索引
     * @param key 关键字
     */
    public static void searchKey(String index,String key){
        QueryBuilder query = QueryBuilders.queryStringQuery(key);//此时匹配全局字段，若需指定字段则添加field方法

        // 设置搜索的内容和现实的大小
        SearchResponse res = client.prepareSearch(index).setQuery(query)
                .setFrom(0).setSize(60).execute().actionGet();
        SearchHits shs = res.getHits();
        System.out.println("总共有数据：" + shs.getHits().length);
        for (SearchHit it : shs) {
            System.out.println(it.getSource());
        }
    }

    /**
     * 组合查询
     * @param index
     * @param type
     */
    // QUERY
    public static void search03(String index,String type) {
        // 按照字段进行索引，只要内容含有即可不用全部符合
        QueryBuilder queryBuilder = QueryBuilders.fuzzyQuery("message",
                "家在黄图高坡的一片草原上");

        //整个 数据内容中只要 有 这个字即可
        QueryBuilder qb =QueryBuilders.queryStringQuery("家");

        //组合查询 user为 byor，context为 偏 的记录 must表示必须有，mustNot表示不包含，should表示可以存在
        QueryBuilder queryBuilder2 = QueryBuilders.fuzzyQuery("user", "byor");
        QueryBuilder queryBuilder3 = QueryBuilders.fuzzyQuery("context", "偏");
        QueryBuilder qbmust = QueryBuilders.boolQuery().must(queryBuilder2).should(queryBuilder3);

        //过滤查询
        QueryBuilder qb3 = QueryBuilders.filteredQuery(queryBuilder2,QueryBuilders.prefixQuery("message", "time"));

        SearchResponse res = client.prepareSearch(index).setTypes(type)
                .setSearchType(SearchType.DEFAULT)
                .setQuery(queryBuilder).execute().actionGet();
        SearchHits shs = res.getHits();
        System.out.println("共查到数据：" + shs.getHits().length);
        for (SearchHit it : shs) {
            System.out.println(it.getSource());
        }
    }

    /**
     * 指定字段查询关键字并高亮显示
     * 注：termQuery全局查询，fuzzyQuery模糊查询
     * @param index
     * @param type
     */
    // 高亮显示
    public static void search05(String index,String type, String field,String key) {
        // FilterBuilder filter =FilterBuilders.prefixFilter("name", "张三");
        SearchResponse res = client.prepareSearch(index).setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.fuzzyQuery(field, key))
                .addHighlightedField(field).setHighlighterPreTags("<span>")
                .setHighlighterPostTags("</span>").execute().actionGet();

        SearchHits shs = res.getHits();
        System.out.println("总共有数据：" + shs.getHits().length);
        for (SearchHit it : shs) {
            System.out.println(it.getSource());
            // 获取对应的高亮域
            Map<String, HighlightField> result = it.highlightFields();
            // 从设定的高亮域中取得指定域,FIXME 此时获取全局变量就会得到空值吗？
            HighlightField titleField = result.get("message");
            // 取得定义的高亮标签
            Text[] titleTexts = titleField.fragments();
            // 为title串值增加自定义的高亮标签
            String title = "";
            for (Text text : titleTexts) {
                title += text;
            }
            // 将追加了高亮标签的串值重新填充到对应的对象
            // product.setTitle(title);
            // 打印高亮标签追加完成后的实体对象
            System.out.println(title);

        }
    }
}
