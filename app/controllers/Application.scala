package controllers

import play.api.mvc._

object Application extends Controller {

  def index(csspath: String) = Action {
    implicit request =>
      Ok(views.html.perfect(routes.Assets.at(s"$csspath").absoluteURL()))
  }

}