package train

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression._
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import mylib.utils.Timer
import mylib.utils.plog

import math._

/**
  * Created by wyb on 17/1/18.
  */
object fm {

  def main(args: Array[String]): Unit = {

    println("start main\n\n")
    println(s"#args: ${args.length}")

    val path_sample = args(0)
    val date_train  = args(1)
    val date_test   = args(2)
    val f_predict   = args(3)
    val f_model     = args(4)
    val dimFeature  = args(5).toInt
    val dimFactor   = args(6).toInt
    val iteration   = args(7).toInt
    val regular     = args(8).toFloat
    val step        = args(9).toFloat
    var train_type  = "Ada"
    if (args.length > 10) {
      train_type = args(10)
    }

    println(s"@dimFactor = ${dimFactor}")
    println(s"@step      = ${step}")
    println(s"@regular   = ${regular}")

    val timer = new Timer
    val sparkConf = new SparkConf().set("spark.shuffle.blockTransferService", "nio")
    val sc = new SparkContext(sparkConf)
    sc.setLogLevel("ERROR")

    plog("load data...")
    val trainSet: RDD[LabeledPoint] = loadSamples(sc, dimFeature, path_sample, date_train)
    val testSet:  RDD[LabeledPoint] = loadSamples(sc, dimFeature, path_sample, date_test)
    val featureIdSet = trainSet.map{x => x.features.toSparse.indices.toSet}.treeReduce((set1, set2) => set1 ++ set2, 2).toList.sorted
    plog(s"#training: ${trainSet.count()}")
    plog(s"#testing : ${testSet.count()}")
    plog(s"#feautres: ${featureIdSet.size}")

    val model = train_type match {
      case "Ada" => {
        plog("Ada training...")
        FMWithAda.train(trainSet, task=1, numIterations=iteration, stepSize=step, miniBatchFraction=1.0, dim=(true,true,dimFactor), regParam=(0.1,regular,regular), initStd=0.01)
      }
      case "SGD" => {
        plog("SGD training...")
        FMWithSGD.train(trainSet, task=1, numIterations=iteration, stepSize=step, miniBatchFraction=1.0, dim=(true,true,dimFactor), regParam=(0.1,regular,regular), initStd=0.01)
      }
      case "LBFGS" => {
        plog("LBFSG training...")
        FMWithLBFGS.train(trainSet, task=1, numIterations=iteration, numCorrections=1, dim=(true,true,dimFactor), regParam=(0.1,regular,regular), initStd=0.01)
      }
      case _ => plog(s"Error train_type: ${train_type}"); return
    }
    plog("train done.")
    plog(s"time elapsed: ${timer.usedTime}\n")

    model.mySaveHdfs(sc, f_model, featureIdSet)
    plog(s"output model done.\n")

    plog("evaluation...")
    val scores_train: RDD[(Double, Double)] = model.predict(trainSet.map(_.features)).zip(trainSet.map(_.label))
    val scores_test:  RDD[(Double, Double)] = model.predict(testSet.map(_.features)).zip(testSet.map(_.label))
    val accuracy_test  = scores_test.filter(x => abs(x._1-x._2) < 0.5).count().toDouble / scores_test.count()
    val accuracy_train = scores_train.filter(x => abs(x._1-x._2) < 0.5).count().toDouble / scores_train.count()
    val metric_train = new BinaryClassificationMetrics(scores_train)
    val metric_test  = new BinaryClassificationMetrics(scores_test)

    scores_test.map{case (a,b) => s"$a, $b"}.saveAsTextFile(f_predict)

    plog(s"train Accuracy = $accuracy_train")
    plog(s" test Accuracy = $accuracy_test")
    plog(s"     train AUC = ${metric_train.areaUnderROC()}")
    plog(s"      test AUC = ${metric_test.areaUnderROC()}")
    plog(s"time elapsed: ${timer.usedTime}\n\n")

    sc.stop()
  }

  def loadSamples(sc: SparkContext, dimFeature: Int, hdfs_path: String, dates: String): RDD[LabeledPoint] = {
    dates.split(",")
      .map(date => MLUtils.loadLibSVMFile(sc, hdfs_path + date, dimFeature))
      .reduce((rdd1, rdd2) => rdd1.union(rdd2))
      .coalesce(500)
  }

  /*
  def load_effect_fid(src: String) = {
    Source.fromFile(src).getLines().filter(_.length > 0).map(_.toInt).toSet
  }
  */
}


