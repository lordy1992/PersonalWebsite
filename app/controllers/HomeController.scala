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

  implicit val title = "Jeremy Lord - Musings on Tech, Fiction"

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

  val editTechnicalPostForm = Form(
    tuple(
      "edit-content" -> text,
      "id" -> number,
      "title" -> text
    )
  )

  def index = cached(_ => "index", duration = ActionCacheDuration) {
    Action {
      Ok(views.html.main())
    }
  }

  def about = cached(_ => "about", duration = ActionCacheDuration) {
    Action {
      Ok(views.html.about()(title + " | About"))
    }
  }

  def contact(sent: Boolean) = cached(_ => "contact", duration = ActionCacheDuration) {
    Action {
      Ok(views.html.contact(justSubmitted = sent)(title + " | Contact"))
    }
  }

  def technical_post(id: Int) = technical(Some(id))

  def technical(id: Option[Int]) = cached(request => "technical." + id.getOrElse(-1)
      + "." + request.session.get(Security.username).isDefined, duration = ActionCacheDuration) {
    Action { request =>
      val isAdmin = request.session.get(Security.username).isDefined
      val titles = postDao.getPostTitles(isAdmin)
      if (titles.isEmpty) {
        Ok(views.html.technical(None, titles, isAdmin)(title + " | Technical"))
      } else {
        val selectedPost = postDao.getPostContent(id.getOrElse(titles(0)._1), isAdmin)
        if (selectedPost.post.status != PostStatus.PUBLISHED && !isAdmin) {
          // Regular users should not be able to access unpublished posts
          Ok(views.html.technical(None, titles, isAdmin)(title + " | Technical | " + selectedPost.post.title))
        } else {
          Ok(views.html.technical(Some(selectedPost), titles, isAdmin)(title + " | Technical | "
            + selectedPost.post.title))
        }
      }
    }
  }

  def edit_technical_post = withAuth { username => implicit request =>
    val contentIdTitle = editTechnicalPostForm.bindFromRequest.get
    postDao.updatePostContent(contentIdTitle._2, contentIdTitle._1, contentIdTitle._3)
    Redirect(controllers.routes.HomeController.technical_post(contentIdTitle._2))
  }

  def writing_article(id: Int) = writing(Some(id))

  def writing(id: Option[Int]) = cached(request => "writing." + id.getOrElse(-1)
      + "." + request.session.get(Security.username).isDefined, duration = ActionCacheDuration) {
    Action { request =>
      val isAdmin = request.session.get(Security.username).isDefined
      val titles = articleDao.getArticleTitles()
      if (titles.isEmpty) {
        Ok(views.html.writing(None, titles, isAdmin)(title + " | Writing"))
      } else {
        val selectedArticle = articleDao.getArticleById(id.getOrElse(titles(0)._1))
        Ok(views.html.writing(Some(selectedArticle), titles, isAdmin)(title + " | Writing | "
          + selectedArticle.article.name))
      }
    }
  }

  def post_message = Action { implicit request =>
    val messageData = messageForm.bindFromRequest.get
    messageDao.addMessage(messageData)
    Redirect(controllers.routes.HomeController.contact(true))
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

  def sitemap = cached(_ => "index", duration = ActionCacheDuration) {
    Action {
      val articleTitles = articleDao.getArticleTitles.map(articleTitle => ((articleTitle._1, articleTitle._3)))
      val postTitles = postDao.getPostTitles().map(postTitle => ((postTitle._1, postTitle._3)))
      Ok(views.xml.sitemap(postTitles, articleTitles))
    }
  }

  def removeTrailingSlash(path: String) = Action {
    MovedPermanently("/" + path)
  }
}
