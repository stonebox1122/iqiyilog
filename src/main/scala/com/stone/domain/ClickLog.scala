package com.stone.domain

/**
  * @author stone
  * @date 2019/5/27 13:53
  * description：日志记录的样例类：29.143.10.100   2019-05-27 13:40:01     "GET toukouxu/821 HTTP/1.0"     -       200
  */
case class ClickLog (ip:String,time:String,categoryId:Int,refer:String,statusCode:Int)
