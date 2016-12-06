# elasticSearchDemo

## 2016.12.2

> 1.创建项目，学习elasticSearch;

> 2.elasticUtil.class创建一些增、删、改、查的基本功能

>> elasticUtil.class中均以static synchronized方法实现，部分方法说明：

>>    | 方法  | 说明 |
      |---|
      | updateDocument()  | 传入参数不可错误或者传递原本不存在的参数，否则将抛出异常
      | search() | 在指定字段中检索指定内容，已做空值预算
      | count() | 注意方法注释说明，queryName参数需要注意是搜索引擎中的自定义参数还是引擎自带参数
      | addNode() | 该方法做添加索引测试，可用
      | close() | 断开集群方法，因改项目均做junit测试，所以没用上，可用，配合getTransportClient()使用

## 2016.12.6

> 1.新建elasticHighLightUtil.class，高亮显示，主要做些搜索方法

> 更多使用方式正在研究中，现有部分方法说明:

>>  | 方法 | 说明 |
    |---|
    | search03() | 组合查询，具体使用方法见方法体注释（不可全部符合功能因使用jar版本问题，暂时不成功，正在研究中）
    | search05() | 高亮显示，搜索关键中追加html代码
