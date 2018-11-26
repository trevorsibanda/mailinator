package zw.trevor.mailinator

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class PaginationSpec extends org.specs2.mutable.Specification {
  "Cursor Pagination" >> {
    "read from horizon" >> {
      stub
    }
    "read N items from horizon" >> {
      stub
    }
    "read N items from Cursor" >> {
      stub
    }
    "read next cursor" >> {
      stub
    }
    "read all cursors till end" >> {
      stub
    }
    "read cursor after delete" >> {
      stub
    }
    "read past next cursor" >> {
      stub
    }
    "read empty mailbox" >> {
      stub
    }
  }

  def stub = {
      true
  }

}