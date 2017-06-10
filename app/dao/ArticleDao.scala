package dao

import objects.{Article, InternalArticle}
import org.joda.time.DateTime
import play.api.db.Database

import scala.collection.mutable.ListBuffer

/**
  * Created by jlord on 5/9/2016.
  */
class ArticleDao(db: Database) {

  def addArticle(article: Article): Unit = {
    val addArticleSql = "INSERT INTO Articles (name, content, created_time) VALUES (?, ?, ?)"

    db.withConnection { conn =>
      val addArticleStatement = conn.prepareStatement(addArticleSql)
      addArticleStatement.setString(1, article.name)
      addArticleStatement.setString(2, article.content)
      addArticleStatement.setLong(3, DateTime.now.getMillis / 1000)
      addArticleStatement.executeUpdate()
    }
  }

  def getArticleTitles(): List[(Int, String, DateTime)] = {
    val getArticleTitlesSql = "SELECT id, name, created_time FROM Articles ORDER BY created_time ASC"

    val resultBuffer = new ListBuffer[(Int, String, DateTime)]()
    db.withConnection { conn =>
      val getArticleTitlesStatement = conn.createStatement()
      val resultSet = getArticleTitlesStatement.executeQuery(getArticleTitlesSql)

      while (resultSet.next()) {
        val id = resultSet.getInt("id")
        val name = resultSet.getString("name")
        val createdTime = resultSet.getLong("created_time")

        resultBuffer += ((id, name, new DateTime(createdTime)))
      }
    }

    resultBuffer.toList
  }

  def getArticleById(id: Int): InternalArticle = {
    val sqlGetArticle = "SELECT * FROM Articles WHERE id = ?"

    db.withConnection { conn =>
      val getArticleStatement = conn.prepareStatement(sqlGetArticle)
      getArticleStatement.setInt(1, id)
      val resultSet = getArticleStatement.executeQuery()

      val articleId = resultSet.getInt("id")
      val name = resultSet.getString("name")
      val content = resultSet.getString("content")

      InternalArticle(articleId, Article(name, content))
    }
  }

  def listAllArticles(): List[InternalArticle] = {
    val listAllArticlesSql = "SELECT * FROM Articles ORDER BY created_time ASC"

    val resultBuffer = new ListBuffer[InternalArticle]()
    db.withConnection { conn =>
      val listArticlesStatement = conn.createStatement()
      val resultSet = listArticlesStatement.executeQuery(listAllArticlesSql)

      while (resultSet.next()) {
        val id = resultSet.getInt("id")
        val name = resultSet.getString("name")
        val content = resultSet.getString("content")

        resultBuffer += InternalArticle(id, Article(name, content))
      }
    }

    resultBuffer.toList
  }

  def removeArticle(id: Int): Unit = {
    val removeArticleSql = "DELETE FROM Articles WHERE id = ?"
    db.withConnection { conn =>
      val removeStatement = conn.prepareStatement(removeArticleSql)
      removeStatement.setInt(1, id)
      removeStatement.executeUpdate()
    }
  }
}
