package net.yankus.visor

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilePhase

/**
 * Full disclosure: I figured out how to do this from reading the groovy
 *                  ToString tranformation class and the Groovy AST unittests.
 */
@GroovyASTTransformation(phase=CompilePhase.CANONICALIZATION)
class VisorASTTransformation extends AbstractASTTransformation {

    static final Class VISOR_CLASS = Visor.class
    static final ClassNode VISOR_NODE = ClassHelper.make(VISOR_CLASS)
    static final String VISOR_NODE_NAME = '@' + VISOR_NODE.getNameWithoutPackage()

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        init(nodes, sourceUnit)

        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode annotation = (AnnotationNode) nodes[0]

        // sanity check
        if (!VISOR_NODE.equals(annotation.getClassNode())) return

        if (parent instanceof ClassNode) {
            ClassNode classNode = (ClassNode) parent
            checkNotInterface(classNode, VISOR_NODE_NAME)
            
            def methodNames =['search', 'index', 'update', 'delete']
            if (classNode.getAllDeclaredMethods().name.find { methodNames.contains it }) {
                throw new IllegalStateException("Visor annotated classes should not have any of the following methods declared: $methodNames")
            }

            def propertyNames = ['queryString', 'score', 'pageSize', 'startingIndex', 'sortOrder', 'snippets']
            if (classNode.getProperties().name.find { propertyNames.contains it}) {
                throw new IllegalStateException("Visor annotated classes should not have any properties with the following names: $propertyNames")
            }

            // TODO: preconfirm that the methods don't already exist, for sanity (i.e., what about if they're already gormified.)
            classNode.addMethod(makeSearchMethod())
            classNode.addMethod(makeIndexMethod())
            classNode.addMethod(makeDeleteMethod())
            classNode.addMethod(makeUpdateMethod())

            classNode.addProperty(makeQueryStringField())
            classNode.addProperty(makeScoreField())
            classNode.addProperty(makeSnippetField())
            classNode.addProperty(makePageSizeField())
            classNode.addProperty(makeStartingIndexField())
            classNode.addProperty(makeSortOrderField())
        }
    }

    private def makeSearchMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('search', ACC_PUBLIC, Object) {
                parameters { }
                exceptions {}
                block { 
                    returnStatement {
                        staticMethodCall(net.yankus.visor.Engine, 'search') {
                            argumentList {
                                variable 'this'
                            }
                        }
                    }
                }
            }
        }
        MethodNode method = ast[0]

        method
    }

    private def makeIndexMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('index', ACC_PUBLIC, Object) {
                parameters { }
                exceptions { }
                block {
                    returnStatement {
                        staticMethodCall(net.yankus.visor.Engine, 'index') {
                            argumentList {
                                variable 'this'
                            }
                        }
                    }
                }
            }
        }
        MethodNode method = ast[0]

        method
    }

    private def makeDeleteMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('delete', ACC_PUBLIC, Object) {
                parameters { }
                exceptions { }
                block {
                    returnStatement {
                        staticMethodCall(net.yankus.visor.Engine, 'delete') {
                            argumentList {
                                variable 'this'
                            }
                        }
                    }
                }
            }
        }
        MethodNode method = ast[0]

        method
    }


    private def makeUpdateMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('update', ACC_PUBLIC, Object) {
                parameters { }
                exceptions { }
                block {
                    returnStatement {
                        staticMethodCall(net.yankus.visor.Engine, 'index') {
                            argumentList {
                                variable 'this'
                            }
                        }
                    }
                }
            }
        }
        MethodNode method = ast[0]

        method
    }

    private def makeQueryStringField() {
        def ast = new AstBuilder().buildFromSpec {
            propertyNode "queryString", ACC_PUBLIC, String, this.class, {
                constant null
            }
        }
        PropertyNode field = ast[0]

        field
    }

    private def makeScoreField() {
        def ast = new AstBuilder().buildFromSpec {
            propertyNode "score", ACC_PUBLIC, Double, this.class, {
                constant 0.0d
            }
        }
        PropertyNode field = ast[0]

        field
    }


    private def makePageSizeField() {
        def ast = new AstBuilder().buildFromSpec {
            propertyNode "pageSize", ACC_PUBLIC, Long, this.class, {
                constant 25L
            }
        }
        PropertyNode field = ast[0]

        field
    }


    private def makeStartingIndexField() {
        def ast = new AstBuilder().buildFromSpec {
            propertyNode "startingIndex", ACC_PUBLIC, Long, this.class, {
                constant 0L
            }
        }
        PropertyNode field = ast[0]

        field
    }

    private def makeSortOrderField() {
        def ast = new AstBuilder().buildFromSpec {
            propertyNode "sortOrder", ACC_PUBLIC, Object, this.class, {
               null
            }
        }
        PropertyNode field = ast[0]

        field
    }

    private def makeSnippetField() {
        def ast = new AstBuilder().buildFromSpec {
            propertyNode "snippets", ACC_PUBLIC, Object, this.class, {
               null
            }
        }
        PropertyNode field = ast[0]

        field
    }
}