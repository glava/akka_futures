package com.example

import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.{ ActorRef, ActorSystem }
import scala.collection.mutable.ListBuffer
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Future
import scala.concurrent.duration._
import akka.pattern.{ ask, pipe }

final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: Seq[User])
final case class UserRepsonse(user: Option[User], original: ActorRef)

object UserRegistryActor {

  final case class GetUser(name: String)
  def props: Props = Props[UserRegistryActor]
}

class UserRegistryActor extends Actor with ActorLogging {
  import UserRegistryActor._
  implicit lazy val timeout = Timeout(5.seconds)

  var users = Set(User("travis", 35, "DE"))

  def receive: Receive = {
    case GetUser(name) =>
      import context.dispatcher
      pipe(Future(users.find(_.name == name))) to sender()
  }
}

class CachedUserRegistryActor extends Actor with ActorLogging {
  import UserRegistryActor._
  val userRegistryActor: ActorRef = context.actorOf(UserRegistryActor.props, "userRegistryActor")
  implicit lazy val timeout = Timeout(5.seconds)

  var users = Set.empty[User]

  def receive: Receive = {
    case GetUser(name) =>
      import context.dispatcher
      pipe(userRegistryActor ? GetUser(name)) to sender()

  }
}

object CachedUserRegistryActor {
  def props = Props[CachedUserRegistryActor]
}

