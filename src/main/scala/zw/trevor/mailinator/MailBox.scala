package zw.trevor.mailinator
import java.util.{Calendar, Date}
import MailBox._

case class Email(val id: MailID, from: String, to: String, subject: String, body: String, received: Date)

abstract class MailBox(val address: Address)(implicit val tableImpl: Table[MailID, Email]) extends FIFO[MailID] with PageReader[Email] with Logging{
    //create a new email
    def create(from: Address, subject: String, body: String): Option[MailID] = {
        val now: Date = Calendar.getInstance.getTime
        val email = new Email(0, from, address, subject, body, now)
        tableImpl.put(email) match{
            case None => None
            case Some(id) => {
                this.put(id) //i own this
                Some(id) 
            }
        }
    }

    //fetch a particular email
    def fetch(id: MailID): Option[Email] = for{
        email <- tableImpl.get(id) //can add constraint to ensure email belongs to address 
    } yield email

    //fetch multiple email addresses
    def fetchMultiple(page: Page): PageResult[Email] = this.read(page)

    //destroy this mailbox and all emails
    def purge: Unit = this.entries.foreach{ entry =>
       this.remove(entry)
       tableImpl.remove(entry)
    }

    //delete a particular email
    def delete(id: MailID): Option[MailID] = this.entries.indexOf(id) match{
        case -1 => None
        case x  => {
            this.remove(x)
            tableImpl.remove(id)
            Some(id)
        } 
    }

}

object MailBox{
    type MailID = Int
    type Address = String
}