package d3v4

import org.scalajs.dom

import scala.scalajs.js
import scalajs.js.{`|`, undefined}
import scala.scalajs.js.annotation._

//https://github.com/d3/d3-fetch
@JSImport("d3", JSImport.Namespace)
@js.native
object d3fetch extends js.Object {
    // TODO is init correct? should it return Unit?
    def json(url: String, init: ListenerFunction1[js.Any]): Xhr = js.native
}

@js.native
trait Xhr extends js.Object {
    def header(name: String): String = js.native

    def header(name: String, value: String): Xhr = js.native

    def mimeType(): String = js.native

    def mimeType(`type`: String): Xhr = js.native

    def responseType(): String = js.native

    def responseType(`type`: String): Xhr = js.native

    def response(): js.Function1[dom.XMLHttpRequest, Any] = js.native

    def response(value: js.Function1[dom.XMLHttpRequest, Any]): Xhr = js.native

    def get(callback: js.Function2[js.Any, js.Any, Unit] = ???): Xhr = js.native

    def post(data: js.Any = ???, callback: js.Function2[js.Any, js.Any, Unit] = ???): Xhr = js.native

    def post(callback: js.Function2[js.Any, js.Any, Unit]): Xhr = js.native

    def send(method: String, data: js.Any = ???, callback: js.Function2[js.Any, js.Any, Unit] = ???): Xhr = js.native

    def send(method: String, callback: js.Function2[js.Any, js.Any, Unit]): Xhr = js.native

    def abort(): Xhr = js.native

    def on(`type`: String): js.Function = js.native

    def on(`type`: String, listener: js.Function): Xhr = js.native
}
