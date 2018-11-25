package zw.trevor.mailinator

import scala.concurrent.duration._

import scala.collection.mutable.{ArrayBuffer}
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Timer, TimerTask}

import MailBox._

/**
 * Store emails in Memory
 */
class HeapBackend() extends Backend[Address, MailID, Email]{
    
    override val tableImpl: Table[MailID, Email] = new Table[MailID, Email] with PageReader[Email]{
        //implement to enable cursor navigation on all emails
        def read(page: Page): PageResult[Email] = ???

        override val idGen: IDGenerator[MailID] = new IDGenerator[MailID]{
            val counter = new AtomicInteger()
            def generate: MailID = (counter.incrementAndGet)
        }
        //bad hack. fix this
        //ignores id provided by email and opts for self generated
        override def put(m: Email): Option[MailID] = {
            val id = idGen.generate
            val nM = m.copy(id = id)
            table.put(id, nM)
            Some(id)
        }
    }

    override def createMailBox(addr: Address) = new MailBox(addr)(tableImpl){
        def read(page: Page): PageResult[Email] = {
            val count = if(page.count <= 0) 1 else page.count
            val cursor = if(page.cursor <= 0) entries.headOption.getOrElse(0) else page.cursor  
            val iter = entries.iterator
            val idx = iter.indexOf(cursor) //unsafe. Walks entire Seq until it reaches cursor, maybe fix??? 
            val mail: ArrayBuffer[Email] = ArrayBuffer()
            if(idx != -1) 
                tableImpl.get(entries(idx)).foreach{ item => mail.append(item)}

            var cursorNext: Int = 0
            takeNWhile[MailID](iter, count-1/*already fetched one*/){ //result discarded since we are appending to mail. bad, must change
                case(entry) => 
                    val email = tableImpl.get(entry)            
                    if(email.isDefined){
                        mail.append(email.get) 
                        cursorNext = entry
                        true
                    } else {
                        
                        false //record cache miss
                    }
            } 
            mail.size match {
                case 0  => new PageResult(Cursor(cursor, None), 0, Nil)
                case _ => new PageResult(Cursor(cursor, if(iter.hasNext) Some(cursorNext) else None), mail.size, mail)
            }
        }
    }  

    override val idGen: IDGenerator[Address] = new IDGenerator[Address]{
        val rand = scala.util.Random
        def generate: Address = this.rand.alphanumeric.take(10).mkString.toLowerCase + "@heap-mail.com"
    }

    override val evict = new EvictionPolicy[Email]{
        val frequency = 15.seconds
        var lastRun = java.util.Calendar.getInstance.getTime
        def pred(item: Email): Boolean = item.received.before(lastRun)
        def evict(item: Email) = deleteMail(item.id).isDefined

        val task = new TimerTask{
            logger info(s"Evictor started at ${java.util.Calendar.getInstance.getTime}")
            def run() = {
                val expired = tableImpl.valuesIterator.collect{case x if pred(x) => x}
                logger info(s"Collected ${expired.size} items to purge")
                lastRun = java.util.Calendar.getInstance.getTime
                logger info(s"Eviction started at $lastRun ")
                expired.foreach{ item => evict(item) match{
                        case true => logger debug(s"Evicted $item")
                        case false => logger warn(s"Failed to evict $item")
                    }
                }
                logger info(s"Eviction finished at ${java.util.Calendar.getInstance.getTime}, next run in ${frequency}") 
            }
        }
    }
    evict.start

}
