package com.stream.Actor

import akka.actor.{Actor, ActorRef}


trait UserEvent
case class UserJoined(User: User,actorRef: ActorRef) extends UserEvent
case class UserLeft(UserName: String) extends UserEvent
case class UserMoveRequest(UserName: String,direction: String) extends UserEvent
case class UsersChanged(Users: Iterable[User]) extends UserEvent

case class User(name: String)
case class UserWithActor(User: User,actor: ActorRef)


class UserManager extends Actor {

  val Users = collection.mutable.LinkedHashMap[String,UserWithActor]()

  override def receive: Receive = {
    case UserJoined(user,actor) => {
      val newUser = User(user.name)
      Users += (user.name -> UserWithActor(newUser,actor))
      notifyUsersChanged()
    }
    case UserLeft(userName) => {
      Users -= userName
      notifyUsersChanged()
    }
    case UserMoveRequest(userName,direction) =>  ???
  }

  def notifyUsersChanged(): Unit = {
    Users.values.foreach(_.actor ! UsersChanged(Users.values.map(_.User)))
  }
}
