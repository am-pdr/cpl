package cool.compiler;

import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.*;

import static cool.structures.SymbolTable.globals;

public class RulesChecker {
    ArrayList<String> nonInheritable = new ArrayList<>(){{
        add("Int");
        add("String");
        add("Bool");
        add("SELF_TYPE");
    }};

    // check the invalid name SELF_TYPE and redefinition for class
    public boolean checkClassName(Class classs, Scope currentScope) {
        String name = classs.type.getText();

        // illegal name for class
        if (name.equals("SELF_TYPE")){
            SymbolTable.error(classs.ctx, classs.type, "Class has illegal name SELF_TYPE");
            return false;
        }

        // redefinition
        if (currentScope.lookup(name) != null) {
            SymbolTable.error(classs.ctx, classs.type, "Class " + name + " is redefined");
            return false;
        }
        return true;
    }

    // check that parent has nonInheritable classes (Int, Bool, etc.)
    public boolean checkParentName(Class classs) {
        String name = classs.type.getText();

        if (nonInheritable.contains(classs.inherit.getText())) {
            SymbolTable.error(classs.ctx, classs.inherit, "Class " + name + " has illegal parent " +
                    classs.inherit.getText());
            return false;
        }
        return true;
    }

    public boolean isParentClassDefined(Class classs) {
        String name = classs.type.getText();
        Token parent = classs.inherit;
        String parentName = parent.getText();

        if (globals.lookup(parentName) == null) {
            SymbolTable.error(classs.ctx, classs.inherit, "Class " + name +
                    " has undefined parent " + parentName);
            return false;
        }
        return true;
    }

    public boolean checkInheritanceCycle(Class classs) {
        String name = classs.type.getText();
        Token parent = classs.inherit;
        String parentName = parent.getText();

        ClassSymbol parentSym = (ClassSymbol) globals.lookup(parentName);
        while (parentSym != null) {
            if (parentSym.getName().equals(name)) {
                SymbolTable.error(classs.ctx, classs.type, "Inheritance cycle for class " +
                        name);
                return false;
            }
            parentSym = (ClassSymbol) globals.lookup(parentSym.getParentName());
        }
        return true;
    }

    public ClassSymbol getCommonParrent(ClassSymbol c1, ClassSymbol c2, Scope currentScope) {
        while (!(currentScope instanceof ClassSymbol)) {
            if (currentScope == null)
                return null;
            currentScope = currentScope.getParent();
        }

        if (c1 == null || c2 == null) {
            return null;
        }

        if (c1.getName().equals(c2.getName())) {
            return c1;
        }

        if (c1.getName().equals("SELF_TYPE")) {
            c1 = (ClassSymbol) currentScope;
        }

        if (c2.getName().equals("SELF_TYPE")) {
            c2 = (ClassSymbol) currentScope;
        }

        Set<String> ancestorsOfC1 = new HashSet<>();
        ClassSymbol current = c1;

        while (current != null) {
            ancestorsOfC1.add(current.getName());
            current = (ClassSymbol) globals.lookup(current.getParentName());
        }

        current = c2;
        while (current != null) {
            if (ancestorsOfC1.contains(current.getName())) {
                return current;
            }
            current = (ClassSymbol) globals.lookup(current.getParentName());
        }

        return (ClassSymbol) globals.lookup("Object");
    }

    public boolean checkAttributeResolution(Attr attribute) {
        if (attribute.id.getSymbol() == null)
            return false;

        ClassSymbol scope = (ClassSymbol) attribute.id.getSymbol().getScope();

        if (scope.getParentName() != null) {
            ClassSymbol parent = (ClassSymbol) globals.lookup(scope.getParentName());
            while (parent != null) {
                if (parent.lookup(attribute.id.token.getText()) != null) {
                    SymbolTable.error(attribute.ctx, attribute.token, "Class " + scope.getName() +
                            " redefines inherited attribute " + attribute.id.token.getText());
                    return false;
                }
                parent = (ClassSymbol) globals.lookup(parent.getParentName());
            }
        }
        return true;
    }

    public boolean checkMethodDefinition(Method method, Scope currentScope) {
        if (currentScope instanceof ClassSymbol) {
            Symbol sym = ((ClassSymbol) currentScope).lookupMethod(method.id.token.getText());
            if (sym != null) {
                SymbolTable.error(method.ctx, method.token, "Class " + ((ClassSymbol) currentScope).getName() +
                        " redefines method " + method.id.token.getText());
                return false;
            }
        }

        return true;
    }

    public boolean checkFormalDefinition(Formal formal, Scope currentScope) {
        String methodName = ((MethodSymbol) currentScope).getName();
        String className = ((ClassSymbol) currentScope.getParent()).getName();

        if (formal.id.token.getText().equals("self")) {
            SymbolTable.error(formal.ctx, formal.token, "Method " + methodName + " of class " + className +
                    " has formal parameter with illegal name self");
            return false;
        }

        if (((MethodSymbol) currentScope).hasSymbol(formal.id.token.getText()) != null) {
            SymbolTable.error(formal.ctx, formal.token, "Method " + methodName + " of class " + className
                    + " redefines formal parameter " + formal.id.token.getText());
            return false;
        }

        if (formal.type.getText().equals("SELF_TYPE")) {
            SymbolTable.error(formal.ctx, formal.type, "Method " + methodName + " of class " + className +
                    " has formal parameter " + formal.id.token.getText() + " with illegal type SELF_TYPE");
            return false;
        }

        return true;
    }

    public boolean checkFormalResolution(Formal formal) {
        String methodName = ((MethodSymbol) formal.id.getSymbol().getScope()).getName();
        String className = ((ClassSymbol) (formal.id.getSymbol().getScope().getParent())).getName();

        ClassSymbol type = (ClassSymbol) globals.lookup(formal.type.getText());
        if (type == null) {
            SymbolTable.error(formal.ctx, formal.type,
                    "Method " + methodName + " of class " + className + " has formal parameter "
                            + formal.id.token.getText() + " with undefined type " + formal.type.getText());
            return false;
        }
        return true;
    }

    public boolean checkMethodOverride(Method method, MethodSymbol currentMethod, String className, String methodName) {
        ClassSymbol currentClass = (ClassSymbol) method.id.getSymbol().getScope();

        while (currentClass != null) {
            MethodSymbol overriddenMethod = (MethodSymbol) currentClass.lookupMethod(methodName);

            if (overriddenMethod != null) {
                String comparisonResult = overriddenMethod.compare(currentMethod);

                if (!comparisonResult.isEmpty()) {
                    if (comparisonResult.contains("number")) {
                        SymbolTable.error(method.ctx, method.token,
                                "Class " + className +
                                        " overrides method " + methodName +
                                        " with different number of formal parameters");
                    } else {
                        reportParameterTypeError(method, comparisonResult, className, methodName);
                    }
                    return false;
                }

                if (!currentMethod.getType().getName().equals(overriddenMethod.getType().getName())) {
                    SymbolTable.error(method.ctx, method.returnType,
                            "Class " + className +
                                    " overrides method " + methodName +
                                    " but changes return type from " +
                                    overriddenMethod.getType().getName() + " to " +
                                    currentMethod.getType().getName());
                    return false;
                }
            }
            currentClass = (ClassSymbol) globals.lookup(currentClass.getParentName());
        }
        return true;
    }

    public void reportParameterTypeError(Method method, String comparisonResult, String className, String methodName) {
        String[] tokens = comparisonResult.split(" ");
        String paramName = tokens[0];
        String oldType = tokens[1];
        String newType = tokens[2];

        for (var formal : method.formals) {
            if (formal.id.token.getText().equals(paramName)) {
                SymbolTable.error(method.ctx, formal.type,
                        "Class " + className +
                                " overrides method " + methodName +
                                " but changes type of formal parameter " + paramName +
                                " from " + oldType + " to " + newType);
                return;
            }
        }
    }

    public boolean isCompatibleReturnType(ClassSymbol declaredType, ClassSymbol actualType, Method method, String methodName) {
        String commonParentName = getCommonParrent(declaredType, actualType, method.id.getSymbol().getScope()).getName();

        if (!declaredType.getName().equals(commonParentName)) {
            SymbolTable.error(method.ctx, method.body.getToken(),
                    "Type " + actualType.getName() +
                            " of the body of method " + methodName +
                            " is incompatible with declared return type " + declaredType.getName());
            return false;
        }
        return true;
    }
}
