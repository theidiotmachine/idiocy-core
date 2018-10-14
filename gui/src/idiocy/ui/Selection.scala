package idiocy.ui
import upickle.default.{ReadWriter => RW, macroRW}

object Selection{
  implicit def rw: RW[Selection] = RW.merge(
    MeasureSelection.rw,
    EventSelection.rw,
    //MultiEventSelection.rw,
    NoSelection.rw
  )
}

sealed trait Selection {

}


object MeasureSelection {
  implicit def rw: RW[MeasureSelection] = macroRW
}

case class MeasureSelection(staffT: Int = 0, measureL: Int = 0,
                            staffB: Int = 0, measureR: Int = 0,
                           ) extends Selection

object EventSelection {
  implicit def rw: RW[EventSelection] = macroRW
}

case class EventSelection(staff: Int = 0,
                          measureL: Int = 0, eventL: Int = 0, //barLineT: Int = 0,
                          measureR: Int = 0, eventR: Int = 0//, barLineB: Int = 0
                         ) extends Selection


/*
object MultiEventSelection{
  implicit def rw: RW[MultiEventSelection] = macroRW
}
case class MultiEventSelection(events: Array[Cursor]) extends Selection
*/

object NoSelection{
  implicit def rw: RW[NoSelection] = macroRW
}

case class NoSelection() extends Selection