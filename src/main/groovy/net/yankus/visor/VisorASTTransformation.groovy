package net.yankus.visor

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.CompilePhase;

@GroovyASTTransformation(phase=CompilePhase.CANONICALIZATION)
class VisorASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = Visor.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        init(nodes, sourceUnit);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);

            cNode.addMethod(makeSearchMethod())
        }
    }

    private def makeSearchMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('search', ACC_PUBLIC, Object) {
                parameters { }
                exceptions {}
                block { 
                    returnStatement {
                        staticMethodCall(net.yankus.visor.Engine, "search") {
                            argumentList {
                                variable "this"
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