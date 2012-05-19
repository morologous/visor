package net.yankus.visor

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilePhase

/**
 * Full disclosure: I figured out how to do this from reading the groovy transform ToString tranformation
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

            classNode.addMethod(makeSearchMethod())
            classNode.addMethod(makeIndexMethod())
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
}