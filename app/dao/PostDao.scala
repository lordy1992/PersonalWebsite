package dao

import objects.{InternalPost, Post}
import org.joda.time.DateTime
import play.api.db.Database

import scala.collection.mutable.ListBuffer

/**
  * Created by jlord on 10/16/2016.
  */
class PostDao(db: Database) {

  def addPost(post: Post): Unit = {
    val sqlAddPost = "INSERT INTO Posts (title, content, post_time) VALUES(?, ?, ?)"
    db.withConnection { conn =>
      val addPostStatement = conn.prepareStatement(sqlAddPost)
      addPostStatement.setString(1, post.title)
      addPostStatement.setString(2, post.content)
      addPostStatement.setLong(3, DateTime.now.getMillis / 1000)
      addPostStatement.executeUpdate()
    }
  }

  def getPostTitles(): List[(Int, String, DateTime)] = {
    val sqlListPosts = "SELECT id, title, post_time FROM Posts ORDER BY post_time DESC"
    val messagesBuffer = new ListBuffer[(Int, String, DateTime)]()
    db.withConnection { conn =>
      val listPostStatement = conn.createStatement()
      val resultSet = listPostStatement.executeQuery(sqlListPosts)

      while (resultSet.next()) {
        val id = resultSet.getInt("id")
        val title = resultSet.getString("title")
        val postTime = resultSet.getInt("post_time")
        messagesBuffer += ((id,  title, new DateTime(postTime * 1000)))
      }
    }

    messagesBuffer.toList
  }

  def getPostContent(postId: Int): InternalPost = {
    val sqlGetPost = "SELECT * FROM Posts WHERE id = ?"
    db.withConnection { conn =>
      val getPostStatement = conn.prepareStatement(sqlGetPost)
      getPostStatement.setInt(1, postId)
      val resultSet = getPostStatement.executeQuery()

      val title = resultSet.getString("title")
      val content = resultSet.getString("content")
      val postTime = resultSet.getInt("post_time")

      InternalPost(postId, new DateTime(postTime * 1000), Post(title, content))
    }
  }

  def getNumPosts(): Int = {
    val sqlPostCount = "SELECT COUNT(*) AS total FROM Posts"
    db.withConnection { conn =>
      val postCountStatement = conn.createStatement()
      val resultSet = postCountStatement.executeQuery(sqlPostCount)
      resultSet.getInt("total")
    }
  }

  def removePost(postId: Int): Unit = {
    val sqlRemoveMessage = "DELETE FROM Posts WHERE id = ?"
    db.withConnection { conn =>
      val removeStatement = conn.prepareStatement(sqlRemoveMessage)
      removeStatement.setInt(1, postId)
      removeStatement.executeUpdate()
    }
  }
}