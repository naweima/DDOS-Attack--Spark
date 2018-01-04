package sparkStreaming

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext, SaveMode}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import util.PropertiesUtil
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.File

/**
  * Create Project
  */
object KafkaSpark {

  def main(args: Array[String]) {
    val util = new PropertiesUtil("ddosIp.properties")
    val zkQuorum = util.getProperty("zkQuorum")
    val group = util.getProperty("group")
    val topics = util.getProperty("topics")
    val seconds: Int = Integer.parseInt(util.getProperty("seconds"))

    val sparkConf = new SparkConf().setAppName("KafkaSpark")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(seconds))
    val sqlContext = new SQLContext(sc)
    val topicMap = topics.split(",").map((_, 1)).toMap
    val messages = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap, StorageLevel.MEMORY_AND_DISK_2)
    messages.foreachRDD { rdd =>
//      save(rdd, sqlContext)
      save2File(rdd,sqlContext)
    }
    ssc.start()
    ssc.awaitTermination()
  }
  def save2File(logRdd:RDD[(String, String)], sqlContext: SQLContext): Unit ={
    val structType = StructType(Array(
      StructField("ip", StringType, true),
      StructField("log", StringType, true)
    ))
    if (logRdd.isEmpty()) {
      println("null ")
    } else {
      try {
        print("youshuju")
        val util = new PropertiesUtil("ddosIp.properties")
        val user = util.getProperty("user")
        val password = util.getProperty("password")
        val url = util.getProperty("url")
        val tbname = util.getProperty("tbname")
        val rdd: RDD[Row] = logRdd.map(line => line._2.split("- -")).map(x => Row(x(0).trim, x(1)))
        val df = sqlContext.createDataFrame(rdd, structType)
        df.registerTempTable("KafkaSpark")
        df.show()
        val numIP = df.sqlContext.sql("select count(ip) as num,ip from KafkaSpark  group by ip ")
        numIP.registerTempTable("numIP")
        val ddosIP = numIP.sqlContext.sql(util.getProperty("ddosSql"))
        if(ddosIP.count()>0){

          ddosIP.foreach(r=>{
            appendToFile(r.toString())
          })
        }
      }
      catch {
        case t: Throwable => t.printStackTrace()
      }
    }
  }
  def appendToFile(rddstr:String ): Unit ={
    try {
      val util = new PropertiesUtil("ddosIp.properties")
      val filepath = util.getProperty("filePath")
      val file = new File(filepath)
      //if file doesnt exists, then create it
      if (!file.exists) file.createNewFile
      //true = append file
      val fileWritter = new FileWriter(file.getName, true)
      val bufferWritter = new BufferedWriter(fileWritter)
      bufferWritter.write(rddstr+"\n")
      bufferWritter.close()
      System.out.println("Done")
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

  def save(logRdd: RDD[(String, String)], sqlContext: SQLContext): Unit = {
    val structType = StructType(Array(
      StructField("ip", StringType, true),
      StructField("log", StringType, true)
    ))
    if (logRdd.isEmpty()) {
      println("null ")
    } else {
      try {
        print("youshuju")
        val util = new PropertiesUtil("ddosIp.properties")
        val user = util.getProperty("user")
        val password = util.getProperty("password")
        val url = util.getProperty("url")
        val tbname = util.getProperty("tbname")
        val rdd: RDD[Row] = logRdd.map(line => line._2.split("- -")).map(x => Row(x(0).trim, x(1)))
        val df = sqlContext.createDataFrame(rdd, structType)
        df.registerTempTable("KafkaSpark")
        df.show()
        val numIP = df.sqlContext.sql("select count(ip) as num,ip from KafkaSpark  group by ip ")
        numIP.registerTempTable("numIP")
        val ddosIP = numIP.sqlContext.sql(util.getProperty("ddosSql"))
        ddosIP.write.format("")
        ddosIP.show()
        val prop = new java.util.Properties
        prop.setProperty("user", user)
        prop.setProperty("password", password)
        ddosIP.write.mode(SaveMode.Append).jdbc(url,tbname, prop)
      }
      catch {
        case t: Throwable => t.printStackTrace()
      }
    }
  }
}
