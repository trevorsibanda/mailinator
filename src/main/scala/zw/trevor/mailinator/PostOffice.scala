package zw.trevor.mailinator

import MailBox._

abstract class PostOffice() extends Table[Address, MailBox] with Logging{
    val tableImpl: Table[MailID, Email]
    def createMailBox(addr: Address): MailBox

    //lookup a mailbox based on email address
    def lookup(address: Address): Option[MailBox] = this.get(address)

    //create a random mailbox  
    def createRandom: Option[Address] = this.put(this.createMailBox(idGen.generate))
    
    //fetch an email given its id
    def fetch(id: MailID): Option[Email] = tableImpl.get(id)
    
    //fetch an email given its email and id. faster as it first look in the address mailbox
    def fetch(address: Address)(id: MailID): Option[Email] = for{
        mailbox <- this.lookup(address)
        email <- mailbox.fetch(id)  
    } yield email

    //fetch multiple results given an address and a cursor
    def fetchMultiple(address: Address)(page: Page): Option[PageResult[Email]] = for{
        mailbox <- this.lookup(address)
        results = mailbox.fetchMultiple(page)
    } yield results

    //create a new email 
    def createMail(to: Address)(from: Address,  subject: String, body: String): Option[MailID] = for{
        mbox <- this.lookup(to)
        mid  <- mbox.create(from, subject, body) 
    } yield mid

    //destroy a mailbox given an email address
    def destroyBox(address: Address): Option[Address] = for{
        mbox <- this.lookup(address)
        _ = mbox.purge
    } yield address

    //delete a particular email given its id. Does not perform mbox lookup
    def deleteMail(id: MailID) = for{
        email <- tableImpl.remove(id)
        //misses in each mailbox will result in deletion
    } yield id

}
