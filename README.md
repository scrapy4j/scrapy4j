# scrapy4j
直接上手

直接通过yaml配置即可构造好我们所需的一切；如果需要一些定制化的业务逻辑，可以通过spring bean的形式注入进来；后续将支持在线 groovy代码编辑的注入；

1、在xxl-job添加YamlSpiderJobHandler

注意xxl-job的源码我们要做一些调整，以便于支持我们的使用，调整点如下

yaml job传参需要超过512长度
-------------------------------------------------
jsp页面：
templates\jobinfo\jobinfo.index.ftl 搜索512 改为 10000；共有3处

数据表 sql：
alter xxl_job_info alter column executor_param varchar 10000;
alter xxl_job_log alter column executor_param varchar 10000;

2、yaml配置文件编写
```
spider:
  startUrls:  //初始请求地址，get方法
    - 'http://www.baidu.com'
    - 'http://www.google.com'
  startRequests:
    name: simple  //目前支持 simple 和 sql 两种方式
    args:
      requests:
        - url: 'http://203.91.37.98:8083/services/BiqCertService'
          method: POST // 注意这里大写，GET POST PUT 等
/*支持{xxx} 和 @{#xxx} 两种写法
1、从当前map取值
2、执行对应的spring bean进行取值（该bean需要实现IFunctionResolver接口）
3、从variables变量集合取值，queries、url 同理
*/
          headers:
             host: 'test'
             access_token: '{access_token}' //支持{xxx}占位符写法,在发起请求的前一刻进行解析；注意这里要加单引号，不然会被yaml识别为对象而不是string
             access_token2: '@{#taijiTokenResolver}' //也支持@{#xxx} spel表达式写法，对应的spring bean 需要实现IFunctionResolver接口 ,在发起请求的前一刻进行解析
             anything: '{host}abc' //这里最后结果为testabc，注意不支持SpEL组合变量的写法，例如 'abc@{taijiTokenResolver}efg' 的组合是暂不支持的
/*headers组的解析，这里仅支持SpEL表达式，用于需要复杂计算的，
例如签名字段；HeadersResolver、QueriesResolver、VariablesResolver
（注意当headers和headersResolver同时使用时，会只取headersResolver为准；QueriesResolver、VariablesResolver同理）
对应的spring bean 要实现 IMapResolver
*/
          headersResolver: '#{@xx}'
/*公用变量的集合，用于给headers、queries、body中的占位符做替换
支持 {xxx} 和 '#{@taijiTokenResolver}'  mustache和spel两种写法
*/
          variables:
             access_token: '#{@taijiTokenResolver}' //支持通过spel加载spring bean 进行解析执行
             secret: 'abcd{access_token}efg'   //支持取当前variables map里其他变量
/*body这里支持 {xxx} 变量进行替换的写法
（如果yaml这里是map格式也支持 @{#xxx} Spel写法， String 类型则仅支持{xxx}变量替换，例如 body: '<soapenv:Envelope xmlns:soapenv="ht...'）
如果有写{xxx} 变量则
1、从当前map取值（如果当前body是map的话）
2、从variables变量集合取值
*/
          body:
             pageSize: 10
             pageIndex: 1
//body这里也支持双大括号的写法 {{secret}}（有且仅有body这里支持） ，为了应对请求body里包含json对象的情况，例如：
//<xml><param>{"token": "{{token}}","TYSHXYDM":"92440300MA5GEATRXJ"}</param></xml>
             secret: '{secret}'
  startRequests:
    name: sql  //适用于通过数据库集合执行一批请求的场景
    args:
        url: 'www.xxx.com?id={id}'
        method: POST
        queries:
            'name': '{name}'
        body:
            'age': '{age}'
        startSql: //这里通过sql查询出的结果将会添加进variables
           sql: 'select id,name,age from res_corp where create_time>={#createTime}'
           pageSize: 10
           variables:
             createTime: '2020-1-1'
        sqlSessionFactory: '#{@sqlSessionFactory}'
  itemPipelines:  //目前支持 rdb redis 两种itempipeline
    - name: rdb
      args:
        sqlSessionFactory: '#{@sqlSessionFactory}'
    - name: redis
      args:
        redisService: '@{#redisService}'
  parser:
    name: json2item //目前提供的唯一一种parser，可满足90%web开发使用场景
    args:
      items:// 一个请求结果可以构建不同的items存储至不同的表
        - name: 'rdb' //目前支持 rdb  redis 两种item处理
          args:
            tableName: tb_abcd   //数据库的表名
            propertyMappings: // 支持如下两种配置方式，复杂版本和简化版本
              - propertyExpression: category[*].id  //复杂版的配置，可自由定义TransformStrategy，注意这里的expression支持[*]写法，这里即取当前json所有category集合里的id属性
                columnName: id
                primaryKey: true
                idType: INPUT //支持mybatis-plus的所有id生成策略（开发中）
                insertStrategy: DEFAULT //支持mybatis-plus的所有FieldStrategy
                updateStrategy: NEVER
                transformStrategy: //将通过propertyExpression获取的json的propertyValue转至数据表columnValue的策略,目前支持的 propertyValue 转换至 columnValue的方法有： defaultValue、json2string、sql
                  - name: '#{@customTransformStrategy}'
              - columnName: status
                propertyValue: 1 //支持固定值及request对象的variables里取值，也可以定义transformStrategy做数据转换
              - columnName: name
                propertyValue: '{name}'
              - 'category[*].name,name' //简化版的配置， 'propertyName,columnName'
              - 'category[*].pub_point,pub_point'
        - name: 'rdb'
          args:
            tableName: tb_def
            propertyMappings:
              - propertyExpression: category.parent
                columnName: id
                primaryKey: true
                idType: INPUT
              - 'category.keywords,name'
              - 'category.description,pub_point'
      interceptors:
//parser处理的拦截器，同mvc interceptor；分为preHandle、postHandle、afterCompletion；
//preHanle方法返回true则parser继续执行，否则return；preHandle适用场景：json返回的code值判断，例如返回的json数据包含code属性=200时继续；
//postHandle适用场景：构造分页请求，请求的是分页接口，则需要构建新的request返回给调度器执行
        - name: '#{@xmlResponseSelectInterceptor}'
          args:
              xpath: '//return'
        - name: '#{@htmlDecodeInterceptor}'
        - name: '#{@taijiResponsePredicateInterceptor}'
settings:
  connectTimeoutSecs: 10  //请求发起连接超时秒数
  concurrentRequests: 16  //请求并发线程数
  downloadDelayMillis: 1000 //请求延时毫秒
  downloadTimeoutSecs: 60  //请求响应超时时间
  retryTimes: 1  //重试次数
  logLevel: FULL //日志级别 NONE、BASIC、HEADERS、FULL




请求结果包含多层json的配置示例（未完待续）

如果请求结果包含多层json嵌套，那么我们应该怎么处理

例如：

{
	"data": {
		"list": [{
			"id": "001",
			"title": "标题001",
			"attatchmentInfo": [{
				"fileName": "附件01",
				"fileUrl": "http://xxxx/file01.jpg"
			}, {
				"fileName": "附件02",
				"fileUrl": "http://xxxx/file02.jpg"
			}],
			"refrence": [{
				"id": "A1",
				"title": "标题A1",
				"attatchmentInfo": [{
					"fileName": "附件A01",
					"fileUrl": "http://xxxx/file01.jpg"
				}, {
					"fileName": "附件A02",
					"fileUrl": "http://xxxx/file02.jpg"
				}]
			}]
		},{
			"id": "002",
			"title": "标题002",
			"attatchmentInfo": [{
				"fileName": "附件0201",
				"fileUrl": "http://xxxx/file01.jpg"
			}, {
				"fileName": "附件0202",
				"fileUrl": "http://xxxx/file02.jpg"
			}],
			"refrence": [{
				"id": "B1",
				"title": "标题B1",
				"attatchmentInfo": [{
					"fileName": "附件B01",
					"fileUrl": "http://xxxx/file01.jpg"
				}, {
					"fileName": "附件B02",
					"fileUrl": "http://xxxx/file02.jpg"
				}]
			}]
		}]
	}
}
```






restful api 请求的配置示例
```
spider:
  startRequests:
    name: simple
    args:
      requests:
        - url: 'http://www.baoan.gov.cn/postmeta/s/110680.json'
          method: GET
          headers:
            'host': 'test'
          variables: 
             token: '#{@taijiTokenResolver}'
  itemPipelines:
    - name: rdb
      args:
        sqlSessionFactory: '#{@sqlSessionFactory}'
  parser:
    name: json2item
    args:
      items:
        - name: 'rdb'
          args:
            tableName: tb_abcd
            propertyMappings:
              - propertyExpression: category.id
                columnName: id
                primaryKey: true
                idType: INPUT
              - 'category.name,name'
              - 'category.pub_point,pub_point'
        - name: 'rdb'
          args:
            tableName: tb_def
            propertyMappings:
              - propertyExpression: category.parent
                columnName: id
                primaryKey: true
                idType: INPUT
              - 'category.keywords,name'
              - 'category.description,pub_point'
settings:
  connectTimeoutSecs: 10
  concurrentRequests: 16
  downloadDelayMillis: 1000
  downloadTimeoutSecs: 60
  retryTimes: 1
  logLevel: FULL
```



restful api 请求的配置示例- SqlStartRequest
```
spider:
  startRequests:
    name: sql
    args:
          url: 'https://apis.map.qq.com/ws/coord/v1/translate'
          method: GET
          queries:
             locations: '{LATITUDE},{LONGITUDE}'
             type: 1
             key: 'XFYBZ-N2OR5-UUPI2-QI2JS-SQMAK-RFFWU'
          headers:
             Referer: 'https://lbs.qq.com/'
          startSql: 
            sql: "
select iz.ZONE_ID,iz.LATITUDE,iz.LONGITUDE 
from industry_zone iz
left join industry_zone_extend ize on iz.zone_id=ize.zone_id
where ifnull(ize.GCJ02_LATITUDE,'')='' or ifnull(ize.GCJ02_LONGITUDE,'')=''
"
            pageSize: 10
          sqlSessionFactory: '#{@sqlSessionFactory}'
  itemPipelines:
    - name: rdb
      args:
        sqlSessionFactory: '#{@sqlSessionFactory}'
  parser:
    name: json2item
    args:
      items:
        - name: 'rdb'
          args:
            tableName: industry_zone_extend
            propertyMappings:
              - 'locations[0].lng,GCJ02_LONGITUDE'
              - 'locations[0].lat,GCJ02_LATITUDE'
              - columnName: ZONE_ID
                primaryKey: true
                idType: INPUT
                propertyValue: '{ZONE_ID}'
settings:
  connectTimeoutSecs: 10
  concurrentRequests: 16
  downloadDelayMillis: 1000
  downloadTimeoutSecs: 60
  retryTimes: 1
  logLevel: FULL
```






webservice请求的配置示例
```
spider:
  startRequests:
    name: simple
    args:
      requests:
        - url: 'http://203.91.37.98:8083/services/BiqCertService'
          method: POST
          headers:
            'host': 'test'
          variables: 
             token: '#{@taijiTokenResolver}'
          body: '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.rest2.cms.jeecms.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:getCertList>
         <!--Optional:-->
         <param>{"token": "{{token}}","TYSHXYDM":"92440300MA5GEATRXJ"}
         </param>
      </ser:getCertList>
   </soapenv:Body>
</soapenv:Envelope>'
  itemPipelines:
    - name: rdb
      args:
        sqlSessionFactory: '#{@sqlSessionFactory}'
  parser:
    name: json2item
    args:
      interceptors: 
        - name: '#{@xmlResponseSelectInterceptor}'
          args:
              xpath: '//return'
        - name: '#{@htmlDecodeInterceptor}'
        - name: '#{@taijiResponsePredicateInterceptor}'
      items:
        - name: 'rdb'
          args:
              tableName: tb_abcd
              propertyMappings:
              - propertyExpression: category.id
                columnName: id
                primaryKey: true
                idType: INPUT
              - 'category.name,name'
              - 'category.pub_point,pub_point'
settings:
  connectTimeoutSecs: 10
  concurrentRequests: 16
  downloadDelayMillis: 1000
  downloadTimeoutSecs: 60
  retryTimes: 1
  logLevel: FULL
```





yaml配置说明
配置项	说明	备注
startRequests	

目前支持如下类型：

simple

sql

	

设计理念

参考python scrapy项目的设计理念https://scrapy.org/

类图（仅做参考，不在控制范围）：




关键对象
Spider

直翻过来即爬虫，读取一切互联网资源请求的结果并保存起来，即爬虫要干的事情；我们常用到的请求第三方数据接口，进行数据同步即被囊括在内；

关键属性：

属性	说明	备注
startUrls	开始的一批请求url地址，都是get请求方法	

startRequests	一组request的集合	

Request

发起请求的最小单位的定义

属性	说明	备注
headers	请求头；可以定义变量{access_token}，在发起请求的前一刻将从variables里取对应的值进行替换；	支持{xxx}  @{#xxx} 两种方式取值
queries	请求query param	

variables	请求的变量用于给headers queries body里面的变量替换	

body	请求的内容，可以为JsonObject  string等格式	

StartRequests

包含

SimpleStartRequests

内置属性requests，即一组Request对象，后面将会讲到

SqlStartRequests

顾名思义，即由sql语句得到的一批requests

关键属性：

属性	说明	备注
StartSql	配置sql查询的对象，包含如下属性	

    sql	查询数据的sql语句，支持mybatis的xml标签以及 #{} 、${} 占位符	

    sqlParameters	请求的变量用于给headers queries body里面的变量替换	

    pageSize	分页数量（可为null，即查询所有数据）	建议分页查询，这样scrapy4j会采用类似背压形式，按需取requests，而不用一次性加载至内存
SqlSessionFactory	即mybatis的SqlSessionFactory	

ItemPipeline

用于处理最终的数据集

RDBItemPipeline

relational database item pipeline

支持 RDBItem的存储

RedisItemPipeline

支持RedisItem的存储

Parser
JSONPropertyRDBItemParser
JSONPropertyMapper

关键属性：

属性	说明	备注
propertyExpression	内置的json选择器，支持  data[*].id 即取一组属性集合	

primaryKey	boolean 标识当前字段是否是主键	

IdType	IdType，即支持mybatis-plus的 IdType	

columnName	映射到对应表的columnName	

TransformStrategy	数据转换器，用于将propertyExpression取到的值进行转换，并存储至columnValue	

目前内置的TransformStrategy有：

1.

2.


InsertStrategy	insert时的策略，同mybatis-plus的InsertStrategy	

UpdateStrategy	update时的策略，同mybatis-plus的InsertStrategy	

Interceptor

Parser执行的流程：

1、Interceptors.preHandle（用于数据的验证，例如，判断返回的json数据是否包含状态码=200，如果false则直接return）

2、构建result

3、Interceptors.postHandle（parser的后置处理，例如构建分页返回的requests）




Settings
配置项	说明	备注

	
	


	
	



