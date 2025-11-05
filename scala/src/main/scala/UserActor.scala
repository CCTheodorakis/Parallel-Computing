import akka.actor.{Actor, ActorRef}
import scala.concurrent.duration._
import scala.util.Random

class UserActor(val id: Int, building: ActorRef, numFloors: Int) extends Actor {
  import context.dispatcher

  var currentFloor: Int = 0
  val random = new Random()

  override def preStart(): Unit = {
    scheduleCall()
  }

  def scheduleCall(): Unit = {
    val waitTime: Int = random.nextInt(60) + 1
    context.system.scheduler.scheduleOnce(waitTime.seconds, self, "Call")
  }

  def receive: Receive = {
    case "Call" =>
      /*Απιλογή random floor*/
      var destination = random.nextInt(numFloors)
      while (destination == currentFloor) {
        destination = random.nextInt(numFloors)
      }
      building ! UserCall(id, currentFloor, destination, self)

    case RideCompleted(`id`, newFloor) =>
      currentFloor = newFloor
      scheduleCall()
    case _ =>
  }
}
