package com.github.j5ik2o.cacs.model

case class Pagination(limit: Int, order: String, starting_after: Option[Long], ending_before: Option[Long])
