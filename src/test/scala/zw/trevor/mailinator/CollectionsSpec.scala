package zw.trevor.mailinator

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

import java.util.concurrent.atomic.AtomicInteger

class CollectionsSpec extends org.specs2.mutable.Specification {

  "Table[K, V]" >> {
    "put" >> {
      tablePut()
    }
    "get" >> {
      tableGet()
    }
    "remove" >> {
        tableRemove()
    }
    "keysIterator" >> {
        tableKeysIterator()
    }
    "valuesIterator" >> {
        tableValuesIterator()
    }
    
  }

  "PageReader[K, V]" >> {
    "takeNWhile" >> {
      testTakeNWhile()
    }
    "readPage" >> {
      testReadPage()
    }
  }

  "IDGenerator[V]" >> {
    "Atomic Sequential IDS" >> {
      testAtomicIDGenerator()
    }
  }

  private[this] val table  = new Table[Int, String]{
      override val idGen = new IDGenerator[Int]{
          val count = new AtomicInteger(scala.util.Random.nextInt)
          def generate = count.incrementAndGet
      }
  }

  private[this] val table2  = new Table[Int, Int] with PageReader[Int]{
      override val idGen = new IDGenerator[Int]{
          val count = new AtomicInteger(scala.util.Random.nextInt)
          def generate = count.incrementAndGet
      }

      def read(page: Page) = ???
  }

  private[this] def tablePut(): MatchResult[String] = {
    val v = scala.util.Random.alphanumeric.take(10).mkString.toLowerCase
    val k = table.put(v)
    table.get(k.get).get must beEqualTo(v)
  }

  private[this] def tableGet(): MatchResult[String] = tablePut()

  private[this] def tableRemove() = {
    val v = scala.util.Random.alphanumeric.take(10).mkString.toLowerCase
    val k = table.put(v)
    val res = table.remove(k.get)
    table.get(k.get) must beEqualTo(None)
  }

  private[this] def tableKeysIterator() = {
      val k = table.put("random string")
      table.keysIterator.contains(k.get) must beEqualTo(true)
  }

  private[this] def tableValuesIterator() = {
      val k = table.put("2018")
      table.valuesIterator.contains("2018") must beEqualTo(true)
  }

  private[this] def testReadPage() = {
      //add this test
      true must beEqualTo(true)
  }

  private[this] def testAtomicIDGenerator() = {
      //add this test
      val idGen = new IDGenerator[Int]{
          val count = new AtomicInteger(scala.util.Random.nextInt)
          def generate = count.incrementAndGet
      }
    val n = scala.util.Random.nextInt%100
    (0 to n).foldLeft(idGen.generate){ case (accum, next) =>
        if(idGen.generate <= accum) throw new Exception("ID Generator failed") else idGen.generate
    }
    true
  }

  private[this] def testTakeNWhile() = {
    val n = scala.util.Random.nextInt%100  
    val expected = (0 until n).map{_*2}.toList
    
    val l = table2.takeNWhile((0 to (n*2)).iterator, n){_%2 == 0}
    l must beEqualTo(expected)
  }



}