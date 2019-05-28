package com.stone

import com.stone.dao.{CategoryClickCountDao, CategorySearchClickCountDao}
import com.stone.domain.{CategoryClickCount, CategorySearchClickCount, ClickLog}
import com.stone.tools.DateUtil
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.ListBuffer

/**
  * @author stone
  * @date 2019/5/24 14:53
  *       description
  */
object StatStreamingApp {
  def main(args: Array[String]): Unit = {
    val ssc = new StreamingContext("local[*]", "StatStreamingApp", Seconds(5))
    //    val kafkaParams = Map[String, Object](
    ////      "bootstrap.servers" -> "172.30.60.62:9092,172.30.60.63:9092,172.30.60.64:9092",
    ////      "key.deserializer" -> classOf[StringDeserializer],
    ////      "value.deserializer" -> classOf[StringDeserializer],
    ////      "group.id" -> "iqiyi",
    ////      "auto.offset.reset" -> "latest",
    ////      "enable.auto.commit" -> (false: java.lang.Boolean)
    ////    )
//    val sparkConf = new SparkConf().setAppName("StatStreamingApp").setMaster("yarn-client")
//
//    //新建一个StreamingContext入口
//    val ssc = new StreamingContext(sparkConf,Seconds(5))

    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> "172.30.60.62:9092,172.30.60.63:9092,172.30.60.64:9092",
      "group.id" -> "iqiyi",
      "auto.offset.reset" -> "largest"
    )
    val topics = Array("iqiyilog").toSet
    val stream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc, kafkaParams,topics)

    val logs = stream.map(word => (word._2))
    logs.print()

//    清洗日志：29.143.10.100   2019-05-27 13:40:01     "GET toukouxu/821 HTTP/1.0"     -       200
    val clearnLog = logs.map(line => {
      val infos = line.split("\t")
      val url = infos(2).split(" ")(1)
      var categoryId = 0
      if (url.startsWith("www")) {
        categoryId = url.split("/")(1).toInt
      }
      ClickLog(infos(0),DateUtil.getDate(infos(1)),categoryId,infos(3),infos(4).toInt)
    }).filter(log => log.categoryId != 0)

    clearnLog.print()

    // 获取计算点击量需要的字段，并保存到HBase
    clearnLog.map(log => {
      (log.time.substring(0,8)+"_"+log.categoryId,1)
    }).reduceByKey(_+_).foreachRDD(rdd => {
      rdd.foreachPartition(partitions => {
        val list = new ListBuffer[CategoryClickCount]
        partitions.foreach(pair => {
          list.append(CategoryClickCount(pair._1,pair._2))
        })
        CategoryClickCountDao.save(list)
      })
    })

    // 获取计算每个渠道过来的点击量的字段，并保存到HBase 20190527_2（渠道）_1（类别）100
    // 187.30.124.132  20190527     "GET www/1 HTTP/1.0"    https://www.sogou.com/web?qu=快乐人生   200
    // create 'iqiyi.category_search_count','info'
    clearnLog.map(log => {
      val url = log.refer.replace("//","/")
      val splits = url.split("/")
      var host = ""
      if(splits.length > 2){
        host = splits(1)
      }
      (log.time,host,log.categoryId)
    }).filter(x => x._2 != "").map(x => {
      (x._1.substring(0,8)+"_"+x._2+"_"+x._3,1)
    }).reduceByKey(_+_).foreachRDD(rdd => {
      rdd.foreachPartition(partitions => {
        val list = new ListBuffer[CategorySearchClickCount]
        partitions.foreach(pair => {
          list.append(CategorySearchClickCount(pair._1,pair._2))
        })
        CategorySearchClickCountDao.save(list)
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
