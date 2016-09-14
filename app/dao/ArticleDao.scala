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
}
