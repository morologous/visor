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
 *
 * TODO: maybe make these properties and methods less common and more specific to visor
 *       like visorSearch or visorIndex... though, GORM doesn't do those things.
 *       The properties may just want underscores or something.
 */
@GroovyASTTransformation(phase=CompilePhase.CANONICALIZATION)
class VisorASTTransformation extends AbstractASTTransformation {

    static final Class VISOR_CLASS = Visor.class
    static final ClassNode VISOR_NODE = ClassHelper.make(VISOR_CLASS)
    static final String VISOR_NODE_NAME = '@' + VISOR_NODE.getNameWithoutPackage()

	static final Set<String> VISOR_METHOD_NAMES = ['search', 'index', 'update', 'delete']
	static final Set<String> VISOR_PROPERTY_NAMES = ['queryString', 'score', 'pageSize', 'startingIndex', 'sortOrder', 'snippets']
	
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        init(nodes, sourceUnit)

        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode annotation = (AnnotationNode) nodes[0]

        // sanity check
        if (!VISOR_NODE.equals(annotation.getClassNode())) return

        if (parent instanceof ClassNode) {
            ClassNode classNode = (ClassNode) parent
            checkNotInterface(classNode, VISOR_NODE_NAME)
            
            def methodNames = VISOR_METHOD_NAMES
            if (classNode.getAllDeclaredMethods().name.find { methodNames.contains it }) {
                throw new IllegalStateException("Visor annotated classes should not have any of the following methods declared: $methodNames")
            }

            def propertyNames = VISOR_PROPERTY_NAMES
            if (classNode.getProperties().name.find { propertyNames.contains it}) {
                throw new IllegalStateException("Visor annotated classes should not have any properties with the following names: $propertyNames")
            }

            // TODO: preconfirm that the methods don't already exist, for sanity (i.e., what about if they're already gormified.)
            classNode.addMethod(makeSearchMethod())
            classNode.addMethod(makeIndexMethod())
            classNode.addMethod(makeDeleteMethod())
            classNode.addMethod(makeUpdateMethod())
            classNode.addMethod(makeCountMethod())

            classNode.addProperty(makeQueryStringField())
            classNode.addProperty(makeScoreField())
            classNode.addProperty(makeSnippetField())
            classNode.addProperty(makePageSizeField())
            classNode.addProperty(makeStartingIndexField())
            classNode.addProperty(makeSortOrderField())
            classNode.addProperty(makeVisorOptsField())
        }
    }
	
	private MethodNode makeMethod(beanMethodName, engineMethodName=null) {
		if (engineMethodName == null) {
			engineMethodName = beanMethodName
		}
		def ast = new AstBuilder().buildFromSpec {
			method(beanMethodName, ACC_PUBLIC, Object) {
				parameters { }
				exceptions { }
				block {
					returnStatement {
						staticMethodCall(net.yankus.visor.Engine, engineMethodName) {
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
	
	private PropertyNode makeProperty(propertyName, propertyType=String.class, defaultValue=null) {
		def ast = new AstBuilder().buildFromSpec {
			propertyNode propertyName, ACC_PUBLIC, propertyType, this.class, {
				constant defaultValue
			}
		}
		PropertyNode field = ast[0]

		field
	}

    private def makeSearchMethod() {
		makeMethod 'search'
    }

    private def makeCountMethod() {
		makeMethod 'count'
    }

    private def makeIndexMethod() {
		makeMethod 'index'
    }

    private def makeDeleteMethod() {
		makeMethod 'delete'
    }


    private def makeUpdateMethod() {
		makeMethod 'update', 'index'
    }

    private def makeQueryStringField() {
		makeProperty 'queryString'
    }

    private def makeScoreField() {
		makeProperty 'score', Double, 0.0d
    }

    private def makePageSizeField() {
		makeProperty 'pageSize', Long, 25L
    }


    private def makeStartingIndexField() {
		makeProperty 'startingIndex', Long, 0L
    }

    private def makeSortOrderField() {
		makeProperty 'sortOrder', Object
    }

    private def makeSnippetField() {
		makeProperty 'snippets', Object
    }

    private def makeVisorOptsField() {
		makeProperty 'visorOpts', Map, new HashMap()
    }
}