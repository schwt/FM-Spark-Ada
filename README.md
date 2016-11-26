# spark-FM with Adagrad and Adam optimizer
A Spark-based implementation of Adam and Adagrad on Factorization Machine model (based on zhengruifeng/spark-libFM).
FM is powerful dealing with sparse data, together with Adam/Adagrad, they showed a better performance than FM with normalSGD method

#How to try
After getting Spark build environment ready, run 'sbt package' under the root of the source to build the package then excute
$SPARKHOME/bin/spark-submit --class TestFMAda --master local[*] target/scala-2.10/libfm_2.10-0.0.1.jar

#FAQ

#Contact & Feedback
feel free to submit an issue or pull request.
