package com.stream.Actor

import akka.actor.{Actor, ActorRef}
import com.stream.domain._




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
    case UserMoveRequest(userName,location) =>  {
      notifyUserMoved(userName ,location)
    }
  }

  def notifyUserMoved(userName : String,location : Location) ={
    Users.values.foreach(_.actor ! UserMoveRequest(userName,location))
  }

  def notifyUsersChanged(): Unit = {
    Users.values.foreach(_.actor ! UsersChanged(Users.values.map(_.User)))
  }
}
