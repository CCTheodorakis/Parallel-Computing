import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration._

object ElevatorSimulation extends App {
  if (args.length != 4) {
    println("Usage: ElevatorSimulation <numElevators> <numFloors> <numUsers> <simulationTimeInMinutes>")
    System.exit(1)
  }

  val numElevators = args(0).toInt
  val numFloors = args(1).toInt
  val numUsers = args(2).toInt
  val simulationTime = args(3).toInt
  val system = ActorSystem("ElevatorSim")
  /*κεντρικοσ coordinator*/
  val buildingActor = system.actorOf(Props(new BuildingActor(numElevators, numFloors)), "BuildingActor")

  for (i <- 1 to numUsers) {
    system.actorOf(Props(new UserActor(i, buildingActor, numFloors)), s"User_$i")
  }

  import system.dispatcher
  /*Scheduler για το simulation clock*/
  var currentMinute = 0
  val tickTask = system.scheduler.scheduleAtFixedRate(
    initialDelay = 0.seconds,
    interval = 100.milliseconds
  ) { () =>
    currentMinute += 1
    buildingActor ! Tick(currentMinute)
  }
  /*End of simulation*/
  system.scheduler.scheduleOnce((simulationTime * 100).milliseconds) {
    tickTask.cancel()
    buildingActor ! SimulationEnd
  }
}
