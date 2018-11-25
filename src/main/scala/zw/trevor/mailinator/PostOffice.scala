package zw.trevor.mailinator

import MailBox._

abstract class PostOffice(){
    //lookup a mailbox based on email address
    def lookup(email: String): MailBox 

    //create a new mailbox
    def createMailBox: MailBox

    //create a random mailbox
    def createRandom: MailBox

    //fetch a mail given a mail id
    def fetch(id: MailID): Email

    //fetch multiple emails given a cursor
    def fetchMultiple(addr: String)(page: Page): Iterable[Email]

    //create a new email
    def createMail(addr: String)(to: String, subject: String, body: String): Email

    //delete a particular email
    def deleteMail(id: MailID): Email

    //destroy an email
    def destroyMail(addr: String): MailBox 
}
