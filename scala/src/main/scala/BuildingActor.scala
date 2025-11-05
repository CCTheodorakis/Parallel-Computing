import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable

class BuildingActor(numElevators: Int, numFloors: Int) extends Actor {

  /* Δημιουργια ανελκυστηρων ως sub-actors*/
  val elevators: IndexedSeq[ActorRef] =
    (1 to numElevators).map(i => context.actorOf(Props(new ElevatorActor(i, numFloors, self)), s"Elevator_$i"))

  val userActors = mutable.Map[Int, ActorRef]()
  var totalWaitingTime: Long = 0
  var boardings: Long = 0
  var simulationMinute: Int = 0
  var nextElevatorIndex: Int = 0

  def receive: Receive = {
    case Tick(minute) =>
      simulationMinute = minute
      elevators.foreach(_ ! Tick(minute))

    case call @ UserCall(userId, fromFloor, toFloor, userRef) =>
      userActors.update(userId, userRef)
      println(s"Minute $simulationMinute: User $userId calls elevator at floor $fromFloor (destination: $toFloor)")
      val req = PickupRequest(userId, fromFloor, toFloor, simulationMinute, userRef)
      elevators(nextElevatorIndex) ! req
      nextElevatorIndex = (nextElevatorIndex + 1) % elevators.size

    case ElevatorArrived(elevatorId, floor, minute) =>
      println(s"Minute $minute: Elevator $elevatorId reaches floor $floor")

    case UserBoarded(userId, elevatorId, floor, callMinute, boardMinute) =>
      val waitingTime = boardMinute - callMinute
      totalWaitingTime += waitingTime
      boardings += 1
      println(s"Minute $boardMinute: User $userId enters elevator $elevatorId at floor $floor (waited $waitingTime minutes)")

    case UserExited(userId, elevatorId, floor, exitMinute) =>
      println(s"Minute $exitMinute: User $userId exits elevator $elevatorId at floor $floor")
      userActors.get(userId).foreach(_ ! RideCompleted(userId, floor))

    case SimulationEnd =>
      val avg = if (boardings > 0) totalWaitingTime.toDouble / boardings else 0.0
      println(s"Simulation finished. Average waiting time: $avg minutes ($boardings boardings)")
      context.system.terminate()

    case _ =>
  }
}
