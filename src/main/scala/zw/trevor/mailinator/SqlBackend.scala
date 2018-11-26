package zw.trevor.mailinator

import scala.concurrent.duration._

import scala.collection.mutable.{ArrayBuffer}
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Timer, TimerTask}

import MailBox._

/**
 * Store emails in sql database
 */
class SqlBackend() extends Backend[Address, MailID, Email]{
    
    override val tableImpl: Table[MailID, Email] = ???

    override def createMailBox(addr: Address) = ???  

    override val idGen: IDGenerator[Address] = ???

    override val evict = ???

}
