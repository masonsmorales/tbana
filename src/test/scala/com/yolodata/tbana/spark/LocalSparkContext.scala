package com.yolodata.tbana.spark

import org.scalatest.{Suite, BeforeAndAfterAll, BeforeAndAfterEach}
import spark.SparkContext

/** Manages a local `sc` {@link SparkContext} variable, correctly stopping it after each test. */
trait LocalSparkContext extends BeforeAndAfterEach with BeforeAndAfterAll { self: Suite =>

   @transient var sc: SparkContext = _

   override def beforeAll() {
     super.beforeAll()
   }

   override def afterEach() {
     resetSparkContext()
     super.afterEach()
   }

   def resetSparkContext() = {
     if (sc != null) {
       LocalSparkContext.stop(sc)
       sc = null
     }
   }

 }

object LocalSparkContext {
   def stop(sc: SparkContext) {
     sc.stop()
     // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
     System.clearProperty("spark.driver.port")
     System.clearProperty("spark.hostPort")
   }

   /** Runs `f` by passing in `sc` and ensures that `sc` is stopped. */
   def withSpark[T](sc: SparkContext)(f: SparkContext => T) = {
     try {
       f(sc)
     } finally {
       stop(sc)
     }
   }

 }