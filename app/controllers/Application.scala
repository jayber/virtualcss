package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect("/assets/html/perfect.html")
  }
}