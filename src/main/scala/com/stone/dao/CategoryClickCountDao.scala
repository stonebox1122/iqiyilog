package com.stone.dao

import com.stone.domain.CategoryClickCount
import com.stone.tools.HBaseUtils

import scala.collection.mutable.ListBuffer

/**
  * @author stone
  * @date 2019/5/27 14:18
  *       description
  */
object CategoryClickCountDao {

  val tableName = "iqiyi.category_clickcount"
  val cf = "info"
  val qualifier = "click_count"

  /**
    * 保存数据
    * @param list
    */
  def save(list:ListBuffer[CategoryClickCount]): Unit ={
    for(els <- list){
      HBaseUtils.incrementColumnValue(tableName,els.categoryId,cf,qualifier,els.clickCount)
    }
  }

  /**
    * 读取数据
    * @param day_category
    * @return
    */
  def count(day_category:String): Long ={
    val value = HBaseUtils.incrementColumnValue(tableName,day_category,cf,qualifier,0)
    if(value == null){
      0L
    }else{
      value;
    }
  }

  def main(args: Array[String]): Unit = {
    val list = new ListBuffer[CategoryClickCount];
    list.append(CategoryClickCount("20181122_1",100))
    list.append(CategoryClickCount("20181122_2",200))
    list.append(CategoryClickCount("20181122_3",300))
    save(list)
    println(count("20181122_1")+"---"+count("20181122_2")+"---"+count("20181122_3"))
  }
}
