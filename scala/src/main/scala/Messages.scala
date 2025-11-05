
import akka.actor.ActorRef

sealed trait Message

case class Tick(minute: Int) extends Message
case class UserCall(userId: Int, fromFloor: Int, toFloor: Int, userRef: ActorRef) extends Message
case class PickupRequest(userId: Int, fromFloor: Int, toFloor: Int, callMinute: Int, userRef: ActorRef) extends Message
case class UserBoarded(userId: Int, elevatorId: Int, floor: Int, callMinute: Int, boardMinute: Int) extends Message
case class UserExited(userId: Int, elevatorId: Int, floor: Int, exitMinute: Int) extends Message
case class RideCompleted(userId: Int, newFloor: Int) extends Message
case class ElevatorArrived(elevatorId: Int, floor: Int, minute: Int) extends Message
case object SimulationEnd extends Message
