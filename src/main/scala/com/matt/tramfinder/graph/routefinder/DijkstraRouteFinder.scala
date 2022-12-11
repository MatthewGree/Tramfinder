package com.matt.tramfinder.graph.routefinder

import cats.implicits.{catsSyntaxOptionId, catsSyntaxOrder, catsSyntaxPartialOrder, catsSyntaxSemigroup}
import com.matt.tramfinder.graph.{Graph, LineId, Node}

import java.time.Instant
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

class DijkstraRouteFinder extends RouteFinder {

  private case class NodeDistanced(node: Node, duration: Duration, lineId: LineId)

  private implicit val nodeOrdering: Ordering[NodeDistanced] =
    (x: NodeDistanced, y: NodeDistanced) => (y.duration.hour, y.duration.minutes).compare((x.duration.hour, x.duration.minutes))


  override def findBestRoute(graph: Graph, start: Int, end: Int, time: Instant): Either[RouteError, Route] = {
    for {
      startNode <- graph.getNodeEither(start)
      endNode <- graph.getNodeEither(end)
      route <- calculateShortestRoute(startNode, endNode, graph, time)
    } yield route
  }

  private def calculateShortestRoute(startNode: Node, endNode: Node, graph: Graph, time: Instant): Either[RouteError, Route] = {
    val durations = new mutable.HashMap[Node, Duration]()
    val prev = new mutable.HashMap[Node, Connection]()

    val queue = new mutable.PriorityQueue[NodeDistanced]()
    graph.getAllNodes.foreach(node =>
      if (node.id != startNode.id) {
        durations.addOne(node -> Duration(Int.MaxValue, Int.MaxValue))
      }
    )
    queue.addOne(NodeDistanced(startNode, Duration(0, 0), LineId("", 0)))
    durations.addOne(startNode -> Duration(0, 0))

    whileContinuable(queue.nonEmpty) {
      val nodeWithDistance = queue.dequeue()
      if (nodeWithDistance.duration != durations(nodeWithDistance.node)) {
        break()
      }
      val nodeInstant = time.plus(nodeWithDistance.duration)
      graph.getNodeEdges(nodeWithDistance.node)
        .filter(edge =>
          nodeInstant.getTime.isInRange(5, edge.info.time) &&
            nodeInstant.getDayType == edge.info.dayType
        )
        .foreach { edge =>
          val newDuration =
            durations(nodeWithDistance.node) combine
              Duration(0, edge.info.timeCost) combine
              (edge.info.time - nodeInstant.getTime) combine
              (if (edge.info.lineId != nodeWithDistance.lineId) Duration(0, 5) else Duration(0, 0))
          if (newDuration < durations(edge.node)) {
            durations.update(edge.node, newDuration)
            prev.update(edge.node, Connection(nodeWithDistance.node, edge.node, edge.info.time, edge.info.time + edge.info.timeCost, edge.info.lineId))
            queue.addOne(NodeDistanced(edge.node, newDuration, edge.info.lineId))
          }
        }
    }

    extractConnections(prev, startNode, endNode)
      .map(connections => Route(connections, durations(endNode))) match {
      case Some(route) => Right(route)
      case None => Left(RouteNotFound(startNode.id, endNode.id))
    }
  }

  private def extractConnections(prev: mutable.Map[Node, Connection], startNode: Node, endNode: Node): Option[List[Connection]] =
    prev.get(endNode).flatMap { connection =>
      if (connection.from == startNode) {
        List(connection).some
      } else {
        extractConnections(prev, startNode, connection.from).map(_.appended(connection))
      }
    }

  private def whileContinuable(condition: => Boolean)(statement: => Unit): Unit =
    while (condition) {
      breakable(statement)
    }
}
