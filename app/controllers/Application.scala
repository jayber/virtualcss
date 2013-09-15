package controllers

import play.api.mvc._

object Application extends Controller {

  def index(csspath: String) = Action {
    Ok(views.html.perfect(routes.Assets.at(s"$csspath")))
  }

  def virtualCssJs() =

}