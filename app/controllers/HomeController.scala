package controllers

import javax.inject._

import dao.{ArticleDao, MessageDao, PostDao}
import enums.PostStatus
import objects._
import play.api.cache.Cached
import play.api.data.Forms._
import play.api.data._
import play.api.db.Database
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(db: Database, cached: Cached) extends Controller with Secured {

  implicit val title = "Jeremy Lord"

  // Constants
  //val ActionCacheDuration = 43200 // Cache for 12 hours -- the content on this page will be updated infrequently, and
                                  // it is unnecessary for content changes to be visible within a day of the change.
  val ActionCacheDuration = 10
  val articleDao = new ArticleDao(db)
  val messageDao = new MessageDao(db)
  val postDao = new PostDao(db)

  val articleForm = Form(
    mapping(
      "post-type" -> text,
      "article-name" -> text,
      "article" -> text
    )(GenericPost.apply)(GenericPost.unapply)
  )

  val messageForm = Form(
    mapping(
      "message-subject" -> text,
      "message-author" -> text,
      "message" -> text
    )(Message.apply)(Message.unapply)
  )

  val deleteForm = Form(
    "messageid" -> number
  )

  def index = cached(_ => "index", duration = ActionCacheDuration) {
    Action {
      Ok(views.html.main())
    }
  }

  def about = cached(_ => "about", duration = ActionCacheDuration) {
    Action {
      Ok(views.html.about())
    }
  }
  def contact = cached(_ => "contact", duration = ActionCacheDuration) {
    Action {
      Ok(views.html.contact())
    }
  }

  def technical_post(id: Int) = technical(Some(id))

  def technical(id: Option[Int]) = cached(request => "technical." + id.getOrElse(-1)
      + "." + request.session.get(Security.username).isDefined, duration = ActionCacheDuration) {
    Action { request =>
      val isAdmin = request.session.get(Security.username).isDefined
      val titles = postDao.getPostTitles(isAdmin)
      if (titles.isEmpty) {
        Ok(views.html.technical(None, titles, isAdmin))
      } else {
        val selectedPost = postDao.getPostContent(id.getOrElse(titles(0)._1), isAdmin)
        if (selectedPost.post.status != PostStatus.PUBLISHED && !isAdmin) {
          // Regular users should not be able to access unpublished posts
          Ok(views.html.technical(None, titles, isAdmin))
        } else {
          Ok(views.html.technical(Some(selectedPost), titles, isAdmin))
        }
      }
    }
  }

  def writing = cached(request => "writing." + request.session.get(Security.username).isDefined,
      duration = ActionCacheDuration) {
    Action { request =>
      val isAdmin = request.session.get(Security.username).isDefined
      Ok(views.html.writing(articleDao.listAllArticles(), isAdmin))
    }
  }

  def post_message = Action { implicit request =>
    val messageData = messageForm.bindFromRequest.get
    messageDao.addMessage(messageData)
    Ok(views.html.contact())
  }

  def admin = withAuth { username => implicit request =>
    Ok(views.html.admin(messageDao.listAllMessages()))
  }

  def post_article = withAuth { username => implicit request =>
    val articleData = articleForm.bindFromRequest.get
    if (articleData.postType == "writing") {
      val writingArticle = Article(articleData.name, articleData.content)
      articleDao.addArticle(writingArticle)
      Redirect(controllers.routes.HomeController.writing())
    } else {
      val post = Post(articleData.name, articleData.content, PostStatus.EDIT)
      postDao.addPost(post)
      Redirect(controllers.routes.HomeController.technical())
    }
  }

  def publish_delete_article = withAuth { username => implicit request =>
    val publishForm = Form(tuple("id" -> number, "action-button" -> text))
    val formData = publishForm.bindFromRequest.get
    val actionType = formData._2
    val postId = formData._1

    if (actionType == "publish") {
      postDao.publishPost(postId)
    } else if (actionType == "delete") {
      postDao.softDeletePost(postId)
    }

    Redirect(controllers.routes.HomeController.technical())
  }

  def delete_message = withAuth { username => implicit request =>
    val messageId = deleteForm.bindFromRequest.get
    messageDao.removeMessage(messageId)
    Redirect(controllers.routes.HomeController.admin())
  }

  def delete_writing = withAuth { username => implicit request =>
    val deleteForm = Form(single("id" -> number))
    val writingId = deleteForm.bindFromRequest.get
    articleDao.removeArticle(writingId)
    Redirect(controllers.routes.HomeController.writing())
  }
}
