package objects

import enums.PostStatus.PostStatus

/**
  * Created by jlord on 10/16/2016.
  */
case class Post(title: String, content: String, status: PostStatus)
