package com.matt.tramfinder.graph.routefinder

import cats.implicits.catsSyntaxOptionId
import com.matt.tramfinder.graph.{Graph, Node}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

class DijkstraRouteFinder extends RouteFinder {

  private case class NodeDistanced(node: Node, distance: Int)

  private implicit val nodeOrdering: Ordering[NodeDistanced] =
    (x: NodeDistanced, y: NodeDistanced) => y.distance.compare(x.distance)


  override def findBestRoute(graph: Graph, start: Int, end: Int, time: Instant): Either[RouteError, List[Connection]] = {
    for {
      startNode <- graph.getNodeEither(start)
      endNode <- graph.getNodeEither(end)
      route <- calculateShortestRoute(startNode, endNode, graph, time)
    } yield route
  }

  private def calculateShortestRoute(startNode: Node, endNode: Node, graph: Graph, time: Instant): Either[RouteError, List[Connection]] = {
    val distances = new mutable.HashMap[Node, Int]()
    val prev = new mutable.HashMap[Node, Connection]()

    val queue = new mutable.PriorityQueue[NodeDistanced]()
    graph.getAllNodes.foreach(node =>
      if (node.id != startNode.id) {
        distances.addOne(node -> Int.MaxValue)
      }
    )
    queue.addOne(NodeDistanced(startNode, 0))
    distances.addOne(startNode -> 0)

    whileContinuable(queue.nonEmpty) {
      val nodeWithDistance = queue.dequeue()
      if (nodeWithDistance.distance != distances(nodeWithDistance.node)) {
        break()
      }
      graph.getNodeEdges(nodeWithDistance.node)
        .filter(edge => {
          val edgeInstant = time.plus(nodeWithDistance.distance.toLong, ChronoUnit.MINUTES)
          edgeInstant.getTime.isInRange(1, edge.info.time) &&
            edgeInstant.getDayType == edge.info.dayType
        })
        .foreach { edge =>
          val newDistance = distances(nodeWithDistance.node) + edge.info.timeCost
          if (newDistance < distances(edge.node)) {
            distances.update(edge.node, newDistance)
            prev.update(edge.node, Connection(nodeWithDistance.node, edge.node, edge.info.time, edge.info.time + edge.info.timeCost, edge.info.lineId))
            queue.addOne(NodeDistanced(edge.node, newDistance))
          }
        }
    }

    extractPath(prev, startNode, endNode) match {
      case Some(path) => Right(path)
      case None => Left(RouteNotFound(startNode.id, endNode.id))
    }
  }

  private def extractPath(prev: mutable.Map[Node, Connection], startNode: Node, endNode: Node): Option[List[Connection]] =
    prev.get(endNode).flatMap { connection =>
      if (connection.from == startNode) {
        List(connection).some
      } else {
        extractPath(prev, startNode, connection.from).map(_.appended(connection))
      }
    }

  private def whileContinuable(condition: => Boolean)(statement: => Unit): Unit =
    while (condition) {
      breakable(statement)
    }
}
