package zw.trevor.mailinator

import scala.concurrent.duration._
import java.util.{Timer, TimerTask}

import org.slf4j.LoggerFactory
import ch.qos.logback.core.util.StatusPrinter
import ch.qos.logback.classic.LoggerContext

trait Logging{
    val logger = LoggerFactory.getLogger(this.getClass)
}

trait EvictionPolicy[A] extends Logging{
    val frequency: Duration
    val task: TimerTask
    def pred(item: A): Boolean
    def evict(item: A): Boolean

    def start() = {
       val timer = new java.util.Timer()
       timer.schedule(task, frequency.toMillis, frequency.toMillis)
    }

    def stop = task.cancel()
}

abstract class Backend[A, K, V]() extends PostOffice() with Logging{ 
    val evict: EvictionPolicy[V]
}

