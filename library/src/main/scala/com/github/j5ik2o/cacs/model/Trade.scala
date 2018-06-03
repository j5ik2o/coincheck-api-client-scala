package com.github.j5ik2o.cacs.model


case class Trades(success: Boolean, pagination: Pagination, data: Seq[Trade])

case class Trade(id: Long, amount: String, rate: Long, pair: String, order_type: String, created_at: String)
