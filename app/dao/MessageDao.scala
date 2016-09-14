package dao

import objects.{InternalMessage, Message}
import org.joda.time.DateTime
import play.api.db.Database

import scala.collection.mutable.ListBuffer

/**
  * Created by jlord on 5/9/2016.
  */
class MessageDao(db: Database) {

  private val MaximumMessages = 1000
  private val MaximumMessageLength = 5000
  private val MaximumSubjectLength = 200

  def addMessage(message: Message): Unit = {
    val currentNumMessages = getNumMessages()
    if (currentNumMessages < MaximumMessages && message.content.length < MaximumMessageLength
      && message.subject.length < MaximumSubjectLength) {

      val sqlAddMessage = "INSERT INTO Messages (subject, author, content, sent_time) VALUES(?, ?, ?, ?)"
      db.withConnection { conn =>
        val addMessageStatement = conn.prepareStatement(sqlAddMessage)
        addMessageStatement.setString(1, message.subject)
        addMessageStatement.setString(2, message.author)
        addMessageStatement.setString(3, message.content)
        addMessageStatement.setLong(4, DateTime.now.getMillis / 1000)
        addMessageStatement.executeUpdate()
      }
    }
  }

  def listAllMessages(): List[InternalMessage] = {
    val sqlListMessage = "SELECT * FROM Messages ORDER BY sent_time ASC"
    val messagesBuffer = new ListBuffer[InternalMessage]()
    db.withConnection { conn =>
      val listMessageStatement = conn.createStatement()
      val resultSet = listMessageStatement.executeQuery(sqlListMessage)

      while (resultSet.next()) {
        val id = resultSet.getInt("id")
        val subject = resultSet.getString("subject")
        val author = resultSet.getString("author")
        val content = resultSet.getString("content")
        messagesBuffer += InternalMessage(id, Message(subject, author, content))
      }
    }

    messagesBuffer.toList
  }

  def getNumMessages(): Int = {
    val sqlMessageCount = "SELECT COUNT(*) AS total FROM Messages"
    db.withConnection { conn =>
      val messageCountStatement = conn.createStatement()
      val resultSet = messageCountStatement.executeQuery(sqlMessageCount)
      resultSet.getInt("total")
    }
  }

  def removeMessage(messageId: Int) = {
    val sqlRemoveMessage = "DELETE FROM Messages WHERE id = ?"
    db.withConnection { conn =>
      val removeStatement = conn.prepareStatement(sqlRemoveMessage)
      removeStatement.setInt(1, messageId)
      removeStatement.executeUpdate()
    }
  }
}
