package mylib
import java.text.SimpleDateFormat
import java.util.Date
import java.io.FileWriter

import breeze.linalg.sum

import scala.io.Source
import scala.util.{Success, Try}
import org.joda.time.DateTime

object utils {

  def plog(s: String): Unit = {
    val form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val dt = form.format(new Date())
    println(s"[${dt}] ${s}")
  }

  def wlog(dst:String, info:String, time_form:String = "[yyyy-MM-dd HH:mm:ss]") {
    // 带时间的日志,打印并输入文件
    val st = (new DateTime()).toString(time_form + " ")
    val wf = new FileWriter(dst, true)
    wf.write(st + info + "\n")
    println(st + info)
    wf.close()
  }

  def atoi(s: String) = {
    Try(s.toInt) match {
      case Success(_) => s.toInt;
      case _ => -1
    }
  }

  // 解析命令行参数,返回 Map。命令格式:
  // "-a arg1" "-b arg2" "-c arg3"
  def parse_parameter(paras:Array[String]) = {
    paras.map{ case para =>
    {
      val sep = para.trim.split(" ")
      (sep(0).trim.drop(1), sep(sep.length-1).trim)
    }
    }.toMap
  }

  // 计时器
  class Timer {
    private val ut = System.currentTimeMillis() / 1000
    def usedTime: String = {
      var diff = System.currentTimeMillis() / 1000 - ut
      val seconds = diff % 60
      diff /= 60
      val minutes = diff % 60
      val hours = diff / 60
      f"$hours%02d:$minutes%02d:$seconds%02d"
    }
  }

  def set_flag(dst:String, info:String): Unit = {
    val wf = new FileWriter(dst)
    wf.write(info)
    wf.close()
  }

  def get_flag(src:String): String = {
    Source.fromFile(src).getLines().foreach{ line => {if (line.length > 0) return line}}
    return "-1"
  }

  def main(args: Array[String]): Unit = {
    plog("main:")
    val s = "1,2,3,4"
    val arrS = s.split(",")
    val arr = arrS.map(x => x.toInt)
    val sum0 = sum(arr)
    val sum1 = arr.reduce((a,b) => a+b)
    plog(sum0.toString)
    plog(sum1.toString)

  }


}
