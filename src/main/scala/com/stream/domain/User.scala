package com.stream.domain

import akka.actor.ActorRef

case class User(name: String)
case class UserWithActor(User: User,actor: ActorRef)
case class UserWithLocation(user : User , location: Location)

trait UserEvent
case class UserJoined(User: User,actorRef: ActorRef) extends UserEvent
case class UserLeft(UserName: String) extends UserEvent
case class UserMoveRequest(UserName: String,location: Location) extends UserEvent
case class UsersChanged(Users: Iterable[User]) extends UserEvent



