package com.matt.tramfinder.graph

import com.matt.tramfinder.model.DayType.DayType
import com.matt.tramfinder.model.Time

case class Node(id: Int, name: String)


case class EdgeInfo(timeCost: Int, time: Time, dayType: DayType, lineId: LineId)
case class LineId(lineId: String, varianceId: Int)
case class Edge(node: Node, info: EdgeInfo)

case class Graph private[graph](private val nodes: Map[Int, Node], private val edges: Map[Int, Seq[Edge]]) {
  def getNode(nodeId: Int): Option[Node] = nodes.get(nodeId)
  def getAllNodes: Iterable[Node] = nodes.values
  def getNodeEdges(node: Node): Seq[Edge] = edges.getOrElse(node.id, Seq.empty)
}
