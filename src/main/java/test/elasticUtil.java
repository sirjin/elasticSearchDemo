package test;


import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author fengjin on 2016/12/1.
 */
public class elasticUtil {

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

    //取得实例
    public static synchronized TransportClient getTransportClient(){
        return client;
    }

    //断开集群
    public static synchronized void close(){
        client.close();
    }

    //为集群添加新的节点
    public static synchronized void addNode(String name){
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(name),PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //删除集群中的某个节点
    public static synchronized void removeNode(String name){
        try {
            client.removeTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(name),PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //创建文档，相当于新增一行数据
    public static synchronized void createDocument(Map<String,Object> map,String index,String type,String id){
        if(map!=null&&map.size()>0){
            IndexResponse indexResponse = client.prepareIndex(index,type,id).setSource(map).execute().actionGet();
            System.out.println(indexResponse.getVersion());
        }
    }

    /**
     * 更新文档，相当于sql中的update
     * @param map
     * @param index
     * @param type
     * @param id 越界将报错，而不是直接新增新数据
     */
    public static synchronized void updateDocument(Map<String,Object> map,String index,String type,String id){
        if(map!=null&&map.size()>0){
            UpdateResponse updateResponse = client.prepareUpdate(index,type,id).setDoc(map).execute().actionGet();
            System.out.println(updateResponse.getVersion());
        }
    }

    /**
     * 新增或者修改文档
     * @param map
     * @param index
     * @param type
     * @param id 存在则修改，不存在则新增
     */
    public static synchronized void upsertDocument(Map<String,Object> map,String index,String type,String id){
        if(map!=null&&map.size()>0){
            UpdateRequest updateRequest = new UpdateRequest(index,type,id).doc(map).upsert();
            UpdateResponse updateResponse = client.update(updateRequest).actionGet();
            System.out.println(updateResponse.getVersion());
        }
    }

    //获取文档，相当于获取一行数据
    public static synchronized void getDocument(String index,String type,String id){
        GetResponse getResponse = client.prepareGet(index,type,id).execute().actionGet();
        System.out.println(getResponse.getSourceAsString());
    }

    //删除文档，相当于删除一行数据（文档相当于关系型数据库中的‘行’）
    public static synchronized void deleteDocument(String index,String type,String id){
        DeleteResponse deteleResponse = client.prepareDelete(index,type,id).execute().actionGet();
        System.out.println(deteleResponse.getVersion());
    }

    /**
     * 查询数据，相当于关系型数据库的select
     * @param index 索引-->数据库
     * @param type 类型-->表
     * @param field 字段-->列
     * @param context 内容-->需要检索的内容
     */
    public static synchronized void search(String index,String type,String field,String context){
        SearchResponse response=elasticUtil.getTransportClient().prepareSearch(index)//设置要查询的索引(index)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setTypes(type)//设置type, 这个在建立索引的时候同时设置了, 或者可以使用head工具查看
                .setQuery(QueryBuilders.matchQuery(field, context))
                .setFrom(0)
                .setSize(10)
                .setExplain(true)
                .execute()
                .actionGet();
        if(response.getHits().getTotalHits()>0){
            for(SearchHit hit:response.getHits()){
                System.out.println(hit.getSourceAsString());
            }
        }else{
            System.out.println("木有");
        }
    }

    /**
     * 统计分析结果
     * @param index
     * @param queryName 需要统计的类型,支持field、type (注意匹配元素的下划线，比如type，此时为‘_type’;内容参数则不需要下划线)
     * @param queryValue 需要统计的类型（支持模糊匹配）
     */
    public static synchronized void count(String index,String queryName,String queryValue){
        CountResponse countresponse = client.prepareCount(index)
                .setQuery(QueryBuilders.termQuery(queryName, queryValue))
                .execute()
                .actionGet();

        System.out.println(countresponse.getCount());

    }
}
