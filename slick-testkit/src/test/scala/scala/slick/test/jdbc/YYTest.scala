package scala.slick.test.jdbc

import scala.language.implicitConversions
import org.junit.Test
import org.junit.Assert._
import scala.slick.yy._

class YYTest {
  import Shallow._

  @Test def simpleTest() {
    val y = 5.3
    val r1 = slickYY {
      val q = Query(y)
      q.toSeq
    }
    assertEquals("Query of int", y, r1.head, 0.1)
    val r2 = slickYY {
      Query(y).map(x => x).toSeq
    }
    assertEquals("Query identity map", y, r2.head, 0.1)
    val r3 = slickYY {
      Query(y).map(x => false).toSeq
    }
    assertEquals("Query dummy map", false, r3.head)
    val r4 = slickYY {
      Query(y).filter(x => x < 1).toSeq
    }
    assertEquals("Query filter", 0, r4.length)

    val z = 3
    val r5 = slickYY {
      Query(z).filter(x => x > 2.5).toSeq
    }
    assertEquals("Query filter + captured var", z, r5.head)
    val a = 1
    val r6 = slickYY {
      val b = 1
      Query(a).filter(x => x == b).toSeq
    }
    assertEquals("Query filter + Column ==", a, r6.head)
    val r7 = slickYY {
      val b = 1
      Query(2 > b).filter(x => x == true).toSeq
    }
    assertEquals("Query filter + Column == (2)", true, r7.head)
  }

  @Test def tuple2Test() {
    val r1 = slickYY {
      val x = (1, 2)
      Query(x).first
    }
    assertEquals("Query of tuple2", (1, 2), r1)
    val r2 = slickYY {
      val x = (1, 2.5)
      Query(x).map(x => x._2).first
    }
    assertEquals("Query map _2 of tuple", 2.5, r2, 0.1)
    val r3 = slickYY {
      val x = (1, 2)
      val q = Query(x).map(x => x._2)
      val q2 = q.filter(x => x == 1)
      q2.toSeq
    }
    assertEquals("Query filter of tuple2 + Column ==", 0, r3.length)
    val r4 = slickYY {
      val x = (1, 2)
      Query(x).map(x => x._2).filter(x => x == 2).toSeq
    }
    assertEquals("Query filter of tuple2 + Column == (2)", 1, r4.length)
    val r5 = slickYY {
      val x = (1, 2)
      Query(x).map(x => x._2).filter(x => x > 1).toSeq
    }
    assertEquals("Query filter of tuple2 + Column >", 1, r5.length)
  }

  @Test def testTableTest() {
    initTable()
    val r1 = slickYY {
      val tbl = Table.test()
      val q = Query.ofTable(tbl)
      q.toSeq
    }
    assertEquals("Query of Table", 4, r1.length)
    val r2 = slickYY {
      val tbl = Table.test()
      val q = Query.ofTable(tbl).map(x => x)
      q.toSeq
    }
    assertEquals("Query identity map of Table", 4, r2.length)

    val r3 = slickYY {
      val tbl = Table.test2()
      val q = Query.ofTable(tbl) map (x => x.id)
      q.toSeq
    }
    assertEquals("Query map _1 of Table", List(14, 18, 15, 20), r3.toList)

    val r4 = slickYY {
      val tbl = Table.test2()
      val q = Query.ofTable(tbl) map (x => x.id) filter (x => x < 16) filter
        (x => x > 14)
      q.toSeq
    }
    assertEquals("Query filter map _1 of Table", List(15), r4.toList)

    val r5 = slickYY {
      val tbl = Table.test2()
      val q = Query ofTable tbl filter (x => x.id == 14)
      q.toSeq
    }
    assertEquals("Query filter of Table", List(YYTableARow(14, 1)), r5.toList)

    YYUtils.closeSession
  }

  def initTable() {
    import scala.slick.driver.H2Driver.simple._
    object Test extends YYSlickCake {
      import TestTable.TableA
      import TestTable.YYTableA
      import TestTable.underlying
      import TestTable.convertTuple2ToTableARow

      implicit val session = YYUtils.provideSession

      (TableA.ddl).create

      TableA.insert((14, 1))
      TableA.insert((18, 1))
      TableA.insert((15, 2))
      TableA.insert((20, 3))
    }
    Test
  }
}