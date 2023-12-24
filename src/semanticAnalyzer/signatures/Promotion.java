package semanticAnalyzer.signatures;

import java.util.*;
import lexicalAnalyzer.*;
import parseTree.ParseNode;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.FuncInvocNode;
import parseTree.nodeTypes.TypeCastingNode;
import parseTree.nodeTypes.OperatorNode;
import semanticAnalyzer.types.*;
import tokens.LextantToken;

public class Promotion {
    HashMap<ParseNode, List<TypeLiteral>> promotionList;

    // create a constructor
    public Promotion() {
        promotionList = new HashMap<ParseNode, List<TypeLiteral>>();
    }

    public void addPromotion(ParseNode node, List<TypeLiteral> castTypes) {
        promotionList.put(node, castTypes);
    }

    public void removePromotion(ParseNode node) {
        promotionList.remove(node);
    }

    public boolean promotable(OperatorNode node) {
        Lextant operator = operatorFor(node);

        List<Type> childTypes = new ArrayList<Type>();
        node.getChildren().forEach((child) -> childTypes.add(child.getType()));
        FunctionSignature signature = FunctionSignatures.signature(operator, childTypes);

        // Check for cast to integer
        for (int i = 0; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                childTypes.set(i, PrimitiveType.INTEGER);
                signature = FunctionSignatures.signature(operator, childTypes);
                if (signature.isNull()) {
                    childTypes.set(i, PrimitiveType.CHARACTER);
                } else {
                    addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER)));
                }
            }
        }

        if (signature.accepts(childTypes)) {
            node.setSignature(signature);
            node.setType(signature.resultType());
            return true;
        }

        // Check for cast to float
        for (int i = 0; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                childTypes.set(i, PrimitiveType.FLOAT);
                signature = FunctionSignatures.signature(operator, childTypes);
                if (signature.isNull()) {
                    childTypes.set(i, PrimitiveType.CHARACTER);
                } else {
                    addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER),
                            new TypeLiteral(PrimitiveType.FLOAT)));
                }
            }

            if (childTypes.get(i) == PrimitiveType.INTEGER) {
                childTypes.set(i, PrimitiveType.FLOAT);
                signature = FunctionSignatures.signature(operator, childTypes);
                if (signature.isNull()) {
                    childTypes.set(i, PrimitiveType.INTEGER);
                } else {
                    addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.FLOAT)));
                }
            }
        }

        if (signature.accepts(childTypes)) {
            node.setSignature(signature);
            node.setType(signature.resultType());
            return true;
        }

        return false;
    }

    public boolean promotable(ArrayNode node) {
        // Skip promotion on arrays being cloned
        // if (node.getToken().isLextant(Keyword.CLONE)) return false;

        List<Type> childTypes = new ArrayList<Type>();
        List<Type> castTypes = new ArrayList<Type>();

        node.getChildren().forEach((child) -> childTypes.add(child.getType()));
        node.getChildren().forEach((child) -> castTypes.add(child.getType()));

        // Attempt to promote index for empty arrays
        if (node.isEmpty()) {
            if (childTypes.get(0) == PrimitiveType.CHARACTER) {
                castTypes.set(0, PrimitiveType.INTEGER);
                addPromotion(node.child(0), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER)));
                return true;
            } else {
                return false;
            }
        }

        int numType = 0;
        int totalNum = node.nChildren();

        // Check for cast to integer
        for (int i = 0; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                castTypes.set(i, PrimitiveType.INTEGER);
                addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER)));
            }
        }

        numType = Collections.frequency(castTypes, PrimitiveType.INTEGER);
        if (numType == totalNum) {
            node.setSubtype(PrimitiveType.INTEGER);
            return true;
        } else {
            promotionList.clear();
            castTypes.clear();
            node.getChildren().forEach((child) -> castTypes.add(child.getType()));
        }

        // Check for cast to float
        for (int i = 0; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                castTypes.set(i, PrimitiveType.FLOAT);
                addPromotion(node.child(i),
                        Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER), new TypeLiteral(PrimitiveType.FLOAT)));
            }

            if (childTypes.get(i) == PrimitiveType.INTEGER) {
                castTypes.set(i, PrimitiveType.FLOAT);
                addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.FLOAT)));
            }
        }

        numType = Collections.frequency(castTypes, PrimitiveType.FLOAT);
        if (numType == totalNum) {
            node.setSubtype(PrimitiveType.FLOAT);
            return true;
        } else {
            promotionList.clear();
            castTypes.clear();
            node.getChildren().forEach((child) -> castTypes.add(child.getType()));
        }

        return false;
    }

    public boolean promotable(ParseNode node) {
        Lextant operator = operatorFor(node);

        List<Type> childTypes = new ArrayList<Type>();
        node.getChildren().forEach((child) -> childTypes.add(child.getType()));
        FunctionSignature signature = FunctionSignatures.signature(operator, childTypes);

        // Check for cast to integer
        for (int i = 1; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                childTypes.set(i, PrimitiveType.INTEGER);
                signature = FunctionSignatures.signature(operator, childTypes);
                if (signature.isNull()) {
                    childTypes.set(i, PrimitiveType.CHARACTER);
                } else {
                    addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER)));
                }
            }
        }

        if (signature.accepts(childTypes)) {
            node.setType(signature.resultType());
            return true;
        }

        // Check for cast to float
        for (int i = 1; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                childTypes.set(i, PrimitiveType.FLOAT);
                signature = FunctionSignatures.signature(operator, childTypes);
                if (signature.isNull()) {
                    childTypes.set(i, PrimitiveType.CHARACTER);
                } else {
                    addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER),
                            new TypeLiteral(PrimitiveType.FLOAT)));
                }
            }

            if (childTypes.get(i) == PrimitiveType.INTEGER) {
                childTypes.set(i, PrimitiveType.FLOAT);
                signature = FunctionSignatures.signature(operator, childTypes);
                if (signature.isNull()) {
                    childTypes.set(i, PrimitiveType.INTEGER);
                } else {
                    addPromotion(node.child(i), Arrays.asList(new TypeLiteral(PrimitiveType.FLOAT)));
                }
            }
        }

        if (signature.accepts(childTypes)) {
            node.setType(signature.resultType());
            return true;
        }

        return false;
    }

    private Lextant operatorFor(ParseNode node) {
        LextantToken token = (LextantToken) node.getToken();
        return token.getLextant();
    }

    public boolean promotable(FuncInvocNode node) {
        FunctionSignature signature;

        if (node.child(0).getType() instanceof FunctionType) {
            FunctionType type = (FunctionType) node.child(0).getType();
            signature = type.getSignature();
        } else {
            signature = node.findVariableBinding().getSignature();
        }

        assert signature != null;

        List<Type> childTypes = new ArrayList<Type>();
        node.getChildren().forEach((child) -> childTypes.add(child.getType()));
        childTypes.remove(0);

        // Check for cast to integer
        for (int i = 0; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                childTypes.set(i, PrimitiveType.INTEGER);
                if (signature.accepts(childTypes)) {
                    addPromotion(node.child(i + 1), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER)));
                } else {
                    childTypes.set(i, PrimitiveType.CHARACTER);
                }
            }
        }

        if (signature.accepts(childTypes)) {
            node.setType(signature.resultType());
            return true;
        }

        // Check for cast to float
        for (int i = 0; i < childTypes.size(); i++) {
            if (childTypes.get(i) == PrimitiveType.CHARACTER) {
                childTypes.set(i, PrimitiveType.FLOAT);
                if (signature.accepts(childTypes)) {
                    addPromotion(node.child(i + 1), Arrays.asList(new TypeLiteral(PrimitiveType.INTEGER), new TypeLiteral(PrimitiveType.FLOAT)));
                } else {
                    childTypes.set(i, PrimitiveType.CHARACTER);
                }
            }

            if (childTypes.get(i) == PrimitiveType.INTEGER) {
                childTypes.set(i, PrimitiveType.FLOAT);
                if (signature.accepts(childTypes)) {
                    addPromotion(node.child(i + 1), Arrays.asList(new TypeLiteral(PrimitiveType.FLOAT)));
                } else {
                    childTypes.set(i, PrimitiveType.INTEGER);
                }
            }
        }

        if (signature.accepts(childTypes)) {
            node.setType(signature.resultType());
            return true;
        }

        return false;
    }

    public void promote() {
        for (Map.Entry<ParseNode, List<TypeLiteral>> entry : promotionList.entrySet()) {
            ParseNode node = entry.getKey();
            List<TypeLiteral> casts = entry.getValue();

            for (TypeLiteral typeLiteral : casts) {
                ParseNode parentNode = node.getParent();

                // Configure CastNode
                List<Type> childTypes = Arrays.asList(node.getType(), typeLiteral.getLiteraltype());
                FunctionSignature signature = FunctionSignatures.signature(Punctuator.CAST, childTypes);
                TypeCastingNode cast = TypeCastingNode.withChildren(Punctuator.CAST.prototype(), node, typeLiteral);
                cast.setSignature(signature);
                cast.setType(signature.resultType());

                parentNode.replaceChild(node, cast);
                node = cast;
            }
        }
    }

}