package zw.trevor.mailinator

import scala.concurrent.duration._

import java.util.concurrent.atomic.AtomicInteger
import java.util.{Timer, TimerTask}

import MailBox._

/**
 * Store emails in Memory
 */
class HeapBackend() extends Backend[Address, MailID, Email]{
    val evict: EvictionPolicy[Email] = null

    override val tableImpl: Table[MailID, Email] = new Table[MailID, Email] with PageReader[Email]{
        //implement to enable cursor navigation on all emails
        def read(page: Page): PageResult[Email] = ???

        val idGenerator: IDGenerator[MailID] = new IDGenerator[MailID]{
            val counter = new AtomicInteger()
            def generate: MailID = (counter.incrementAndGet)
        }

        //bad hack. fix this
        //ignores id provided by email and opts for self generated
        override def put(m: Email): Option[MailID] = {
            val id = idGenerator.generate
            val nM = m.copy(id = id)
            table.put(id, nM)
            Some(id)
        }
    }   
}
