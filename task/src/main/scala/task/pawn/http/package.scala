package task.pawn

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.Path

import com.ning.http.client.{AsyncCompletionHandler, AsyncHttpClient, Response}

package object http {

  import scalaz.concurrent.Task
  import scalaz.syntax.either._

  def download(uri: String)(path: Path): Task[Unit] = for {
    response <- get(uri)
    _ <- writeTo(path)(response.getResponseBodyAsBytes)
  } yield ()

  def writeTo(path: Path)(blob: Array[Byte]) = for {
    writer <- Task(new BufferedOutputStream(new FileOutputStream(path.toFile)))
    _ <- Task {
      Stream.continually(writer.write(blob))
    }.onFinish(_ => Task {
      writer.close()
    })
  } yield ()


  def get(uri: String): Task[Response] = for {
    client <- initClient
    response <- doGet(uri)(client).onFinish(_ =>
      closeClient(client)
    )
  } yield response

  private def doGet(s: String)(client: AsyncHttpClient): Task[Response] = Task.async[Response] { callback =>
      client.prepareGet(s).execute(
        new AsyncCompletionHandler[Unit] {
          def onCompleted(r: Response): Unit = {
            callback(r.right)
          }
          def onError(e: Throwable): Unit = callback(e.left)
        }
      )
    }

  private def initClient: Task[AsyncHttpClient] = Task(new AsyncHttpClient())

  private def closeClient(client: AsyncHttpClient): Task[Unit] = Task(client.close())

}
