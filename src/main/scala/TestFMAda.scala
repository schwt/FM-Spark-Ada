import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.regression._
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD



object TestFMAda extends App {

  override def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("TESTFMADA").setMaster("local[4]")
    val sc = new SparkContext(sparkConf)

    val training = MLUtils.loadLibSVMFile(sc, "data/a9a").cache()
    val testing = MLUtils.loadLibSVMFile(sc, "data/a9a.t")
    //    val task = args(1).toInt
    //    val numIterations = args(2).toInt
    //    val stepSize = args(3).toDouble
    //    val miniBatchFraction = args(4).toDouble
    val currentTime = System.currentTimeMillis()
    val fm1 = FMWithAda.train(training, task = 1, numIterations = 50, stepSize = 0.01, miniBatchFraction = 1.0, dim = (true, true, 2), regParam = (0, 0.0, 0.01), initStd = 0.01)
    val elapsedTime = System.currentTimeMillis() - currentTime

    val scores: RDD[(Int, Int)] = fm1.predict(testing.map(_.features)).map(x => if(x >= 0.5) 1 else -1).zip(testing.map(_.label.toInt))
    val accuracy = scores.filter(x => x._1 == x._2).count().toDouble / scores.count()

    println(s"Accuracy = $accuracy, time elapsed: $elapsedTime millisecond.")
    sc.stop()
  }
}
