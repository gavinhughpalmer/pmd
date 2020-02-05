/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.rule.bestpractices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.sourceforge.pmd.lang.apex.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.apex.ast.ASTMethod;
import net.sourceforge.pmd.lang.apex.ast.ASTMethodCallExpression;
import net.sourceforge.pmd.lang.apex.ast.ASTStatement;
import net.sourceforge.pmd.lang.apex.ast.ApexNode;
import net.sourceforge.pmd.lang.apex.rule.AbstractApexUnitTestRule;

/**
 * Apex unit tests should have System.assert methods in them
 *
 * @author a.subramanian
 */
public class AssertStatementsShouldContainTheOptionalParameter extends AbstractApexUnitTestRule {

    private static final String ASSERT = "system.assert";
    private static final String ASSERT_EQUALS = "system.assertequals";
    private static final String ASSERT_NOT_EQUALS = "system.assertnotequals";

    @Override
    public Object visit(ASTMethod node, Object data) {
        if (!isTestMethodOrClass(node)) {
            return data;
        }

        return checkAssertStatementsContainOptionalParameter(node, data);
    }

    private Object checkAssertStatementsContainOptionalParameter(ApexNode<?> node, Object data) {
        final List<ASTBlockStatement> blockStatements = node.findDescendantsOfType(ASTBlockStatement.class);
        final List<ASTMethodCallExpression> methodCalls = new ArrayList<>();
        for (ASTBlockStatement blockStatement : blockStatements) {
            methodCalls.addAll(blockStatement.findDescendantsOfType(ASTMethodCallExpression.class));
        }
        boolean isAssertFoundWithoutOptionalParameter = false;

        for (final ASTMethodCallExpression methodCallExpression : methodCalls) {
            if (isAssertWithoutOptionalParameters(methodCallExpression)) {
                isAssertFoundWithoutOptionalParameter = true;
                break;
            }
        }

        if (!isAssertFoundWithoutOptionalParameter) {
            addViolation(data, node);
        }

        return data;
    }

    private boolean isAssertWithoutOptionalParameters(ASTMethodCallExpression methodCallExpression) {
        final String methodName = methodCallExpression.getFullMethodName().toLowerCase(Locale.ROOT);
        final int numberOfParameters = methodCallExpression.jjtGetNumChildren();
        return (methodName.equals(ASSERT) && numberOfParameters < 2) ||
            ((methodName.equals(ASSERT_EQUALS) || methodName.equals(ASSERT_NOT_EQUALS)) && numberOfParameters < 3);
    }
}
