import akka.actor.{Actor, ActorRef}
import scala.collection.mutable

class ElevatorActor(val id: Int, val numFloors: Int, building: ActorRef) extends Actor {

  var currentFloor: Int = 0
  var direction: Int = 1
  val pendingPickups = mutable.ListBuffer[PickupRequest]()

  case class Passenger(userId: Int, dropFloor: Int, callMinute: Int, boardMinute: Int)
  val passengers = mutable.ListBuffer[Passenger]()
  var stopCountdown: Int = 0

  def receive: Receive = {
    case Tick(minute) =>
      if (stopCountdown > 0) {
        stopCountdown -= 1
      } else {
        val pickupsHere = pendingPickups.filter(req => req.fromFloor == currentFloor)
        val dropOffsHere = passengers.filter(p => p.dropFloor == currentFloor)

        if (pickupsHere.nonEmpty || dropOffsHere.nonEmpty) {
          /*ο user βγαίνει*/
          dropOffsHere.foreach { p =>
            building ! UserExited(p.userId, id, currentFloor, minute)
          }
          passengers --= dropOffsHere
          /*ο user μπαίνει στον ανελκυστήραα*/
          pickupsHere.foreach { req =>
            building ! UserBoarded(req.userId, id, currentFloor, req.callMinute, minute)
            passengers += Passenger(req.userId, req.toFloor, req.callMinute, minute)
          }
          pendingPickups --= pickupsHere
          stopCountdown = 1
        } else {
          var nextFloor = currentFloor + direction
          if (nextFloor >= numFloors) {
            direction = -1
            nextFloor = currentFloor + direction
          } else if (nextFloor < 0) {
            direction = 1
            nextFloor = currentFloor + direction
          }
          currentFloor = nextFloor
          building ! ElevatorArrived(id, currentFloor, minute)
        }
      }

    case req: PickupRequest =>
      pendingPickups += req
    case _ =>
  }
}
