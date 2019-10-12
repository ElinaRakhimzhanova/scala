package week6.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import week6.model.{Director, ErrorResponse, Movie, SuccessfulResponse}

object TestBot {

  case object TestCreate

  case object TestConflict

  case object TestRead

  case object TestNotFound

  case object TestUpdate

  case object TestDelete

  def props(manager: ActorRef) = Props(new TestBot(manager))
}

class TestBot(manager: ActorRef) extends Actor with ActorLogging {
  import TestBot._

  override def receive: Receive = {
    case TestCreate =>
      manager ! MovieManager.CreateMovie(Movie("1", "Joker", Director("dir-1", "Todd", "Philips"), 2019))
      manager ! MovieManager.CreateMovie(Movie("4", "Harry Potter", Director("dir-4", "David", "Yates"), 2016))
      manager ! MovieManager.CreateMovie(Movie("7", "1+1", Director("dir-5", "Olivier", "Nakache"), 2011))


    case TestConflict =>
      manager ! MovieManager.CreateMovie(Movie("2", "Charlie's Angels", Director("dir-2", "Ivan", "Ivanov"), 2019))
      manager ! MovieManager.CreateMovie(Movie("2", "Test Test", Director("dir-2", "Ivan", "Ivanov"), 2019))

//      manager ! MovieManager.UpdateMovie(Movie("2", "Test Test", Director("dir-2", "Ivan", "Ivanov"), 2019))
//      manager ! MovieManager.UpdateMovie(Movie("2", "Test Test", Director("dir-2", "Ivan", "Ivanov"), 2019))

    case TestRead =>
      manager ! MovieManager.ReadMovie("1")

    case TestUpdate =>
      manager ! MovieManager.UpdateMovie(Movie("4", "Harry Potter", Director("dir-4", "David", "Yates"), 2014))

    case TestDelete =>
      manager ! MovieManager.DeleteMovie("7")

    case TestNotFound =>
      manager ! MovieManager.ReadMovie("3")
      manager ! MovieManager.UpdateMovie(Movie("6", "Test Test", Director("dir-2", "Ivan", "Ivanov"), 2019))
      manager ! MovieManager.UpdateMovie(Movie("5", "Test Test", Director("dir-2", "Ivan", "Ivanov"), 2019))
      manager ! MovieManager.DeleteMovie("8")

    case SuccessfulResponse(status, msg) =>
      log.info("Received Successful Response with status: {} and message: {}", status, msg)

    case ErrorResponse(status, msg) =>
      log.warning("Received Error Response with status: {} and message: {}", status, msg)

    case movie: Movie =>
      log.info("Received movie: [{}]", movie)

      // TODO: add tests
  }
}
