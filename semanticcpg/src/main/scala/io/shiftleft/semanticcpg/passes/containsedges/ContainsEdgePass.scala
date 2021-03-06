package io.shiftleft.semanticcpg.passes.containsedges

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, NodeTypes, nodes}
import overflowdb.OdbGraph
import io.shiftleft.passes.{DiffGraph, ParallelCpgPass}
import io.shiftleft.semanticcpg.language.NodeTypeDeco

import scala.jdk.CollectionConverters._

/**
  * This pass has MethodStubCreator and TypeDeclStubCreator as prerequisite for
  * language frontends which do not provide method stubs and type decl stubs.
  */
class ContainsEdgePass(cpg: Cpg) extends ParallelCpgPass[nodes.AstNode](cpg) {
  import ContainsEdgePass.{destinationTypes, sourceTypes}

  override def partIterator: Iterator[nodes.AstNode] =
    cpg.graph.nodes(sourceTypes: _*).asScala.map(_.asInstanceOf[nodes.AstNode])

  override def runOnPart(source: nodes.AstNode): Iterator[DiffGraph] = {
    val dstGraph = DiffGraph.newBuilder

    source.start
      .walkAstUntilReaching(sourceTypes)
      .sideEffect { destination =>
        if (destinationTypes.contains(destination.label))
          dstGraph.addEdgeInOriginal(source, destination, EdgeTypes.CONTAINS)
      }
      .iterate()

    Iterator(dstGraph.build())
  }
}

object ContainsEdgePass {

  private val destinationTypes = List(
    NodeTypes.BLOCK,
    NodeTypes.IDENTIFIER,
    NodeTypes.FIELD_IDENTIFIER,
    NodeTypes.RETURN,
    NodeTypes.METHOD,
    NodeTypes.TYPE_DECL,
    NodeTypes.CALL,
    NodeTypes.LITERAL,
    NodeTypes.METHOD_REF,
    NodeTypes.CONTROL_STRUCTURE,
    NodeTypes.JUMP_TARGET,
    NodeTypes.UNKNOWN
  )

  private val sourceTypes = List(
    NodeTypes.METHOD,
    NodeTypes.TYPE_DECL,
    NodeTypes.FILE
  )

}
