package objects

import org.joda.time.DateTime

/**
  * Created by jlord on 10/16/2016.
  */
case class InternalPost(id: Int, postTime: DateTime, post: Post)
