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
            val idx = iter.indexOf(cursor) 
            val mail: ArrayBuffer[Email] = ArrayBuffer()
            if(idx != -1) 
                tableImpl.get(entries(idx)).foreach{ item => mail.append(item)}

            var cursorNext: Int = 0
            takeNWhile[MailID](iter, count){ 
                case(entry) => 
                    val email = tableImpl.get(entry)            
                    if(email.isDefined){
                        mail.prepend(email.get) //prepend to order by recency
                        cursorNext = entry
                        true
                    } else {
                        logger warn(s"Cache miss on $entry $this")
                        false
                    }
            } 
            mail.size match {
                case 0  => new PageResult(Cursor(cursor, None), 0, Nil)
                case _ => {
                    val next = if(iter.hasNext) Some(cursorNext) else None
                    val (results: ArrayBuffer[Email], size: Int) = if(mail.size == 1){
                        (mail, 1)
                    }  else if(!next.isDefined && mail.size != 1) {
                        (mail, mail.size) 
                    }else 
                        (mail.tail, mail.size-1)
                    new PageResult(Cursor(cursor, next), size, results)
                }
            }
        }
    }  

    override val idGen: IDGenerator[Address] = new IDGenerator[Address]{
        val rand = scala.util.Random
        def generate: Address = this.rand.alphanumeric.take(10).mkString.toLowerCase + "@heap-mail.com"
    }

    override val evict = new EvictionPolicy[Email]{
        val frequency = 5.minutes
        def pred(item: Email): Boolean = {
            val now = java.util.Calendar.getInstance.toInstant
            item.received.plusSeconds(frequency.toSeconds).isBefore(now)
        }
        def evict(item: Email) = deleteMail(item.id).isDefined


        val task = new TimerTask{
            logger info(s"Evictor thread started at ${java.util.Calendar.getInstance.toInstant}. Running every ${frequency}")
            def run() = {
                val expired = tableImpl.valuesIterator.collect{case x if pred(x) => x}.toList
                logger info(s"Collected ${expired.size} items to purge")
                val now = java.util.Calendar.getInstance.toInstant
                logger info(s"Eviction started at $now ")
                expired.foreach{ item => evict(item) match{
                        case true => logger debug(s"Evicted $item")
                        case false => logger warn(s"Failed to evict $item")
                    }
                }
                logger info(s"Eviction finished at ${java.util.Calendar.getInstance.toInstant}, next run in ${frequency}") 
            }
        }
    }
    evict.start
}
