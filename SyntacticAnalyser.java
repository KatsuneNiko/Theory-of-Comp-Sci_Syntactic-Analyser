import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SyntacticAnalyser {

	public static ParseTree parse(List<Token> tokens) throws SyntaxException {
		//Turn the List of Tokens into a ParseTree.

		Stack<TreeNode> stack = new Stack<TreeNode>(); //Initializes the stack

		TreeNode root = new TreeNode(TreeNode.Label.prog, null);
		stack.push(root); //Sets the start state of the stack as "prog"

		ParseTree parseTree = new ParseTree();
		parseTree.setRoot(root); //Sets the root node as "prog" as well

		TreeNode currentNode = parseTree.getRoot(); //Used to track which variable node to add children too

		boolean failedMatch = false;

		for (Token token : tokens){
			//If the first thing in the stack matches the token, then remove it from the stack,
			//add it as a child node and go to next iteration
			
			//If the first thing in the stack does not match the token, then use the parsing table
			//to adjust the stack and try to parse the token again. Repeat until it's able to be
			//parsed, or a failed match occurs

			TreeNode tokenNode = new TreeNode(TreeNode.Label.terminal, token, currentNode);

			while (stack.peek().getLabel() != TreeNode.Label.terminal && !failedMatch){
				//Check the top of the stack, and implement the corresponding rule based on the
				//Top of the stack. If a rule is used, add a new Node to the tree and adjust
				//the "current Node" accordingly

				//Parsing table implementation as a switch-case statement.
				switch(stack.peek().getLabel()) {
					case endvar:
						//Added an "end of variable" character to know when to stop matching a variable and move up the treee
						if (currentNode.getLabel() != TreeNode.Label.prog){
							currentNode = currentNode.getParent();
						}
						stack.pop();
						break;
					case prog: 
						switch(token.getType()){
							case PUBLIC:
								rule1(stack); break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case los:
						switch(token.getType()) {
							case ID:
							case SEMICOLON:
							case WHILE:
							case FOR:
							case IF:
							case PRINT:
							case TYPE:
								rule2(stack); 
								TreeNode losNode = new TreeNode(TreeNode.Label.los, currentNode);
								currentNode.addChild(losNode);
								currentNode = losNode;
								break;
							case RBRACE: //Seeing } should apply epsilon transition according to table
								eTransition(stack);
								TreeNode losNode2 = new TreeNode(TreeNode.Label.los, currentNode);
								losNode2.addChild(new TreeNode(TreeNode.Label.epsilon, losNode2));
								currentNode.addChild(losNode2);
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case stat:
						switch (token.getType()){
							case ID:
								rule7(stack);
								TreeNode statNode = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode);
								currentNode = statNode;
								break;
							case SEMICOLON:
								rule10(stack);
								TreeNode statNode2 = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode2);
								currentNode = statNode2;
								break;
							case WHILE:
								rule4(stack);
								TreeNode statNode3 = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode3);
								currentNode = statNode3;
								break;
							case FOR:
								rule5(stack);
								TreeNode statNode4 = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode4);
								currentNode = statNode4;
								break;
							case IF:
								rule6(stack);
								TreeNode statNode5 = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode5);
								currentNode = statNode5;
								break;
							case PRINT:
								rule9(stack);
								TreeNode statNode6 = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode6);
								currentNode = statNode6;
								break;
							case TYPE:
								rule8(stack);
								TreeNode statNode7 = new TreeNode(TreeNode.Label.stat, currentNode);
								currentNode.addChild(statNode7);
								currentNode = statNode7;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case whilestat:
						switch (token.getType()){
							case WHILE:
								rule11(stack);
								TreeNode whileNode = new TreeNode(TreeNode.Label.whilestat, currentNode);
								currentNode.addChild(whileNode);
								currentNode = whileNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case forstat: 
						switch (token.getType()){
							case FOR:
								rule12(stack);
								TreeNode forNode = new TreeNode(TreeNode.Label.forstat, currentNode);
								currentNode.addChild(forNode);
								currentNode = forNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case forstart:
						switch(token.getType()) {
							case ID: 
								rule14(stack); 
								TreeNode forstartNode = new TreeNode(TreeNode.Label.forstart, currentNode);
								currentNode.addChild(forstartNode);
								currentNode = forstartNode;
								break;
							case SEMICOLON: //epsilon transition
								eTransition(stack); 
								TreeNode forstartNode2 = new TreeNode(TreeNode.Label.forstart, currentNode);
								forstartNode2.addChild(new TreeNode(TreeNode.Label.epsilon, forstartNode2));
								currentNode.addChild(forstartNode2);
								break;
							case TYPE:
								rule13(stack);
								TreeNode forstartNode3 = new TreeNode(TreeNode.Label.forstart, currentNode);
								currentNode.addChild(forstartNode3);
								currentNode = forstartNode3;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case forarith:
						switch(token.getType()){
							case ID:
							case LPAREN:
								rule16(stack);
								TreeNode forarithNode = new TreeNode(TreeNode.Label.forarith, currentNode);
								currentNode.addChild(forarithNode);
								currentNode = forarithNode;
								break;
							case RPAREN: //epsilon transition
								eTransition(stack);
								TreeNode forarithNode2 = new TreeNode(TreeNode.Label.forarith, currentNode);
								forarithNode2.addChild(new TreeNode(TreeNode.Label.epsilon, forarithNode2));
								currentNode.addChild(forarithNode2);
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case ifstat:
						switch(token.getType()){
							case IF:
								rule18(stack);
								TreeNode ifNode = new TreeNode(TreeNode.Label.ifstat, currentNode);
								currentNode.addChild(ifNode);
								currentNode = ifNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case elseifstat:
						switch(token.getType()){
							case ID:
							case RBRACE:
							case SEMICOLON:
							case WHILE:
							case FOR:
							case IF:
							case PRINT:
							case TYPE: //epsilon transition
								eTransition(stack);
								TreeNode elseifNode = new TreeNode(TreeNode.Label.elseifstat, currentNode);
								elseifNode.addChild(new TreeNode(TreeNode.Label.epsilon, elseifNode));
								currentNode.addChild(elseifNode);
								break;
							case ELSE:
								rule19(stack);
								TreeNode elseifNode2 = new TreeNode(TreeNode.Label.elseifstat, currentNode);
								currentNode.addChild(elseifNode2);
								currentNode = elseifNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case elseorelseif:
						switch(token.getType()){
							case ELSE:
								rule21(stack);
								TreeNode elseorelseifNode = new TreeNode(TreeNode.Label.elseorelseif, currentNode);
								currentNode.addChild(elseorelseifNode);
								currentNode = elseorelseifNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case possif:
						switch(token.getType()){
							case LBRACE: //epsilon transition
								eTransition(stack);
								TreeNode possifNode = new TreeNode(TreeNode.Label.possif, currentNode);
								possifNode.addChild(new TreeNode(TreeNode.Label.epsilon, possifNode));
								currentNode.addChild(possifNode);
								break;
							case IF:
								rule22(stack);
								TreeNode possifNode2 = new TreeNode(TreeNode.Label.possif, currentNode);
								currentNode.addChild(possifNode2);
								currentNode = possifNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case assign:
						switch(token.getType()){
							case ID:
								rule24(stack);
								TreeNode assignNode = new TreeNode(TreeNode.Label.assign, currentNode);
								currentNode.addChild(assignNode);
								currentNode = assignNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case decl:
						switch(token.getType()){
							case TYPE:
								rule25(stack);
								TreeNode declNode = new TreeNode(TreeNode.Label.decl, currentNode);
								currentNode.addChild(declNode);
								currentNode = declNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case possassign:
						switch(token.getType()){
							case SEMICOLON: //epsilon transition
								eTransition(stack);
								TreeNode possassignNode = new TreeNode(TreeNode.Label.possassign, currentNode);
								possassignNode.addChild(new TreeNode(TreeNode.Label.epsilon, possassignNode));
								currentNode.addChild(possassignNode);
								break;
							case ASSIGN:
								rule26(stack);
								TreeNode possassignNode2 = new TreeNode(TreeNode.Label.possassign, currentNode);
								currentNode.addChild(possassignNode2);
								currentNode = possassignNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case print:
						switch(token.getType()){
							case PRINT:
								rule28(stack);
								TreeNode printNode = new TreeNode(TreeNode.Label.print, currentNode);
								currentNode.addChild(printNode);
								currentNode = printNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case type:
						switch(token.getType()){
							case TYPE:
								switch(token.getValue().get()){
									case "int":
										rule29(stack);
										TreeNode intNode = new TreeNode(TreeNode.Label.type, currentNode);
										currentNode.addChild(intNode);
										currentNode = intNode;
										break;
									case "boolean":
										rule30(stack);
										TreeNode booleanNode = new TreeNode(TreeNode.Label.type, currentNode);
										currentNode.addChild(booleanNode);
										currentNode = booleanNode;
										break;
									case "char":
										rule31(stack);
										TreeNode charNode = new TreeNode(TreeNode.Label.type, currentNode);
										currentNode.addChild(charNode);
										currentNode = charNode;
										break;
									default:
										failedMatch = true; break;
								}
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case expr:
						switch(token.getType()){
							case ID:
							case LPAREN:
							case TRUE:
							case FALSE:
							case NUM:
								rule32(stack);
								TreeNode exprNode = new TreeNode(TreeNode.Label.expr, currentNode);
								currentNode.addChild(exprNode);
								currentNode = exprNode;
								break;
							case SQUOTE:
								rule33(stack);
								TreeNode exprNode2 = new TreeNode(TreeNode.Label.expr, currentNode);
								currentNode.addChild(exprNode2);
								currentNode = exprNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case charexpr:
						switch(token.getType()){
							case SQUOTE:
								rule34(stack);
								TreeNode charexprNode = new TreeNode(TreeNode.Label.charexpr, currentNode);
								currentNode.addChild(charexprNode);
								currentNode = charexprNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case boolexpr:
						switch(token.getType()){
							case RPAREN:
							case SEMICOLON: //epsilon transition
								eTransition(stack);
								TreeNode boolexprNode = new TreeNode(TreeNode.Label.boolexpr, currentNode);
								boolexprNode.addChild(new TreeNode(TreeNode.Label.epsilon, boolexprNode));
								currentNode.addChild(boolexprNode);
								break;
							case EQUAL:
							case NEQUAL:
							case AND:
							case OR:
								rule35(stack);
								TreeNode boolexprNode2 = new TreeNode(TreeNode.Label.boolexpr, currentNode);
								currentNode.addChild(boolexprNode2);
								currentNode = boolexprNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case boolop:
						switch(token.getType()){
							case EQUAL:
							case NEQUAL:
								rule37(stack);
								TreeNode boolopNode = new TreeNode(TreeNode.Label.boolop, currentNode);
								currentNode.addChild(boolopNode);
								currentNode = boolopNode;
								break;
							case AND:
							case OR:
								rule38(stack);
								TreeNode boolopNode2 = new TreeNode(TreeNode.Label.boolop, currentNode);
								currentNode.addChild(boolopNode2);
								currentNode = boolopNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case booleq:
						switch(token.getType()){
							case EQUAL:
								rule39(stack);
								TreeNode booleqNode = new TreeNode(TreeNode.Label.booleq, currentNode);
								currentNode.addChild(booleqNode);
								currentNode = booleqNode;
								break;
							case NEQUAL:
								rule40(stack);
								TreeNode booleqNode2 = new TreeNode(TreeNode.Label.booleq, currentNode);
								currentNode.addChild(booleqNode2);
								currentNode = booleqNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case boollog:
						switch(token.getType()){
							case AND:
								rule41(stack);
								TreeNode boollogNode = new TreeNode(TreeNode.Label.boollog, currentNode);
								currentNode.addChild(boollogNode);
								currentNode = boollogNode;
								break;
							case OR:
								rule40(stack);
								TreeNode boollogNode2 = new TreeNode(TreeNode.Label.boollog, currentNode);
								currentNode.addChild(boollogNode2);
								currentNode = boollogNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case relexpr:
						switch(token.getType()){
							case ID:
							case LPAREN:
							case NUM:
								rule43(stack);
								TreeNode relexprNode = new TreeNode(TreeNode.Label.relexpr, currentNode);
								currentNode.addChild(relexprNode);
								currentNode = relexprNode;
								break;
							case TRUE:
								rule44(stack);
								TreeNode relexprNode2 = new TreeNode(TreeNode.Label.relexpr, currentNode);
								currentNode.addChild(relexprNode2);
								currentNode = relexprNode2;
								break;
							case FALSE:
								rule45(stack);
								TreeNode relexprNode3 = new TreeNode(TreeNode.Label.relexpr, currentNode);
								currentNode.addChild(relexprNode3);
								currentNode = relexprNode3;
								break;
							default:
								failedMatch = true; break;
						}
						break;

					case relexprprime:
						switch(token.getType()){
							case RPAREN:
							case SEMICOLON:
							case EQUAL:
							case NEQUAL:
							case AND:
							case OR: //epsilon transition
								eTransition(stack);
								TreeNode relexprprimeNode = new TreeNode(TreeNode.Label.relexprprime, currentNode);
								relexprprimeNode.addChild(new TreeNode(TreeNode.Label.epsilon, relexprprimeNode));
								currentNode.addChild(relexprprimeNode);
								break;
							case LT:
							case LE:
							case GT:
							case GE:
								rule46(stack);
								TreeNode relexprprimeNode2 = new TreeNode(TreeNode.Label.relexprprime, currentNode);
								currentNode.addChild(relexprprimeNode2);
								currentNode = relexprprimeNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case relop:
						switch(token.getType()){
							case LT:
								rule48(stack);
								TreeNode relopNode = new TreeNode(TreeNode.Label.relop, currentNode);
								currentNode.addChild(relopNode);
								currentNode = relopNode;
								break;
							case LE:
								rule49(stack);
								TreeNode relopNode2 = new TreeNode(TreeNode.Label.relop, currentNode);
								currentNode.addChild(relopNode2);
								currentNode = relopNode2;
								break;
							case GT:
								rule50(stack);
								TreeNode relopNode3 = new TreeNode(TreeNode.Label.relop, currentNode);
								currentNode.addChild(relopNode3);
								currentNode = relopNode3;
								break;
							case GE:
								rule51(stack);
								TreeNode relopNode4 = new TreeNode(TreeNode.Label.relop, currentNode);
								currentNode.addChild(relopNode4);
								currentNode = relopNode4;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case arithexpr:
						switch(token.getType()){
							case ID:
							case LPAREN:
							case NUM:
								rule52(stack);
								TreeNode arithexprNode = new TreeNode(TreeNode.Label.arithexpr, currentNode);
								currentNode.addChild(arithexprNode);
								currentNode = arithexprNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case arithexprprime:
						switch(token.getType()){
							case PLUS:
								rule53(stack);
								TreeNode arithexprprimeNode = new TreeNode(TreeNode.Label.arithexprprime, currentNode);
								currentNode.addChild(arithexprprimeNode);
								currentNode = arithexprprimeNode;
								break;
							case MINUS:
								rule54(stack);
								TreeNode arithexprprimeNode2 = new TreeNode(TreeNode.Label.arithexprprime, currentNode);
								currentNode.addChild(arithexprprimeNode2);
								currentNode = arithexprprimeNode2;
								break;
							case ID:
							case RPAREN:
							case EQUAL:
							case NEQUAL:
							case AND:
							case OR:
							case LT:
							case LE:
							case GT:
							case GE:
							case NUM:
							case SEMICOLON: //epsilon transition
								eTransition(stack);
								TreeNode arithexprprimeNode3 = new TreeNode(TreeNode.Label.arithexprprime, currentNode);
								arithexprprimeNode3.addChild(new TreeNode(TreeNode.Label.epsilon, arithexprprimeNode3));
								currentNode.addChild(arithexprprimeNode3);
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case term:
						switch(token.getType()){
							case ID:
							case LPAREN:
							case NUM:
								rule56(stack);
								TreeNode termNode = new TreeNode(TreeNode.Label.term, currentNode);
								currentNode.addChild(termNode);
								currentNode = termNode;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case termprime:
						switch(token.getType()){
							case TIMES:
								rule57(stack);
								TreeNode timesNode = new TreeNode(TreeNode.Label.termprime, currentNode);
								currentNode.addChild(timesNode);
								currentNode = timesNode;
								break;
							case DIVIDE:
								rule58(stack);
								TreeNode divideNode = new TreeNode(TreeNode.Label.termprime, currentNode);
								currentNode.addChild(divideNode);
								currentNode = divideNode;
								break;
							case MOD:
								rule59(stack);
								TreeNode modNode = new TreeNode(TreeNode.Label.termprime, currentNode);
								currentNode.addChild(modNode);
								currentNode = modNode;
								break;
							case PLUS:
							case MINUS:
							case RPAREN:
							case SEMICOLON:
							case EQUAL:
							case NEQUAL:
							case AND:
							case OR: //epsilon transition
								eTransition(stack);
								TreeNode termprimeNode = new TreeNode(TreeNode.Label.termprime, currentNode);
								termprimeNode.addChild(new TreeNode(TreeNode.Label.epsilon, termprimeNode));
								currentNode.addChild(termprimeNode);
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case factor:
						switch(token.getType()){
							case LPAREN:
								rule61(stack);
								TreeNode factorNode = new TreeNode(TreeNode.Label.factor, currentNode);
								currentNode.addChild(factorNode);
								currentNode = factorNode;
								break;
							case ID:
								rule62(stack);
								TreeNode factorNode2 = new TreeNode(TreeNode.Label.factor, currentNode);
								currentNode.addChild(factorNode2);
								currentNode = factorNode2;
								break;
							case NUM:
								rule63(stack);
								TreeNode factorNode3 = new TreeNode(TreeNode.Label.factor, currentNode);
								currentNode.addChild(factorNode3);
								currentNode = factorNode3;
								break;
							default:
								failedMatch = true; break;
						}
						break;
					
					case printexpr:
						switch(token.getType()){
							case ID:
							case LPAREN:
							case TRUE:
							case FALSE:
							case NUM:
								rule64(stack);
								TreeNode printexprNode = new TreeNode(TreeNode.Label.printexpr, currentNode);
								currentNode.addChild(printexprNode);
								currentNode = printexprNode;
								break;
							case DQUOTE:
								rule65(stack);
								TreeNode printexprNode2 = new TreeNode(TreeNode.Label.printexpr, currentNode);
								currentNode.addChild(printexprNode2);
								currentNode = printexprNode2;
								break;
							default:
								failedMatch = true; break;
						}
						break;
				}
			}

			//If a failed match is found, stop trying to process the stack and throw an exception
			if (failedMatch){
				throw new SyntaxException("Failed to match variable to token list. Current variable is: " + stack.pop() + " and current token is: " + token);
			}

			//If no failed matches were made, compare the terminal in the stack with the terminal in the token.
			if (stack.peek().getToken().get().getType() == token.getType()){
				stack.pop();
				currentNode.addChild(tokenNode);
			}
			else {
				throw new SyntaxException("Discrepancy between terminal in stack and terminal in token list.");
			}
		}

		if (stack.empty()){
			return parseTree;
		}
		else{
			throw new SyntaxException("Stack is not empty after processing token list!");
		}
	}

	//SimpleJava Grammar has been broken down as 65 rules, and implemented here as
	//methods to be called from the parse() method. All the epsilon transition rules
	//are functionally identical, and have been condensed into the eTransition() method.

	//Contributions:
	//Katherine Mai - setting up the logic for handling the stack and implementing the first 17 rules
	//Julian Peen - suggesting the use of stacks to keep track of the rules and implementing rule 18-65

	//Turning prog into public class <<ID>> { public static void main (String[] args) { <<los>> } }
	private static void rule1(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.los, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ARGS), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.STRINGARR), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.MAIN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.VOID), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.STATIC), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.PUBLIC), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ID), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.CLASS), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.PUBLIC), null));
	}

	//Turning <<los>> into <<stat>><<los>>
	private static void rule2(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.los, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.stat, null));
	}

	//Epsilon transition for rules 3, 15, 17, 20, 23, 27, 36, 47, 55 and 60
	private static void eTransition(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();
		stack.pop();
	}

	//Turning <<stat>> into <<while>>
	private static void rule4(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.whilestat, null));
	}

	//Turning <<stat>> into <<for>>
	private static void rule5(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.forstat, null));
	}

	//Turning <<stat>> into <<if>>
	private static void rule6(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.ifstat, null));
	}

	//Turning <<stat>> into <<assign>> ;
	private static void rule7(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SEMICOLON), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.assign, null));
	}

	//Turning <<stat>> into <<decl>> ;
	private static void rule8(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SEMICOLON), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.decl, null));
	}

	//Turning <<stat>> into <<print>> ;
	private static void rule9(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SEMICOLON), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.print, null));
	}

	//Turning <<stat>> into ;
	private static void rule10(Stack<TreeNode> stack) throws SyntaxException { 
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SEMICOLON), null));
	}

	//Turning <<while>> into while ( <<rel expr>> <<bool expr>> ) { <<los>> }
	private static void rule11(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.los, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.WHILE), null));
	}

	//Turning <<for>> into for ( <<for start>> ; <<rel expr>> <<bool expr>> ; <<for arith>> ) { <<los>> }
	private static void rule12(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.los, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.forarith, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SEMICOLON), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SEMICOLON), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.forstart, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.FOR), null));
	}

	//Turning <<for start>> into <<decl>>
	private static void rule13(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.decl, null));
	}

	//Turning <<for start>> into <<assign>>
	private static void rule14(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.assign, null));
	}

	//Turning <<for arith>> into <<arith expr>>
	private static void rule16(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexpr, null));
	}

	//Turning <<if>> into if ( <<rel expr>> <<bool expr>> ) { <<los>> } <<else if>>
	private static void rule18(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.elseifstat, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.los, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.IF), null));
	}

	//Turning <<else if>> into <<else?if>> { <<los>> } <<else if>>
	private static void rule19(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.elseifstat, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.los, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LBRACE), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.elseorelseif, null));
	}

	//Turning <<else?if>> into else <<poss if>>
	private static void rule21(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.possif, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ELSE), null));
	}

	//Turning <<poss if>> into if ( <<rel expr>> <<bool expr>> )
	private static void rule22(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.IF), null));
	}

	//Turning <<assign>> into <<ID>> = <<expr>>
	private static void rule24(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.expr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ASSIGN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ID), null));
	}

	//Turning <<decl>> into <<type>> <<ID>> <<poss assign>>
	private static void rule25(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.possassign, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ID), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.type, null));
	}

	//Turning <<poss assign>> into = <<expr>>
	private static void rule26(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.expr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ASSIGN), null));
	}

	//Turning <<print>> into System.out.println ( <<print expr>> )
	private static void rule28(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.printexpr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.PRINT), null));
	}

	//Turning <<type>> into int
	private static void rule29(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.TYPE, "int"), null));
	}

	//Turning <<type>> into boolean
	private static void rule30(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.TYPE, "boolean"), null));
	}

	//Turning <<type>> into char
	private static void rule31(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.TYPE, "char"), null));
	}

	//Turning <<expr>> into <<rel expr>> <<bool expr>>
	private static void rule32(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
	}

	//Turning <<expr>> into <<char expr>>
	private static void rule33(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.charexpr, null));
	}

	//Turning <<char expr>> into ' <<char>> ' 
	private static void rule34(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SQUOTE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.CHARLIT), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.SQUOTE), null));
	}

	//Turning <<bool expr>> into <<bool op>> <<rel expr>> <<bool expr>>
	private static void rule35(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolop, null));
	}

	//Turning <<bool op>> into <<bool eq>>
	private static void rule37(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.booleq, null));
	}

	//Turning <<bool op>> into <<bool log>>
	private static void rule38(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boollog, null));
	}

	//Turning <<bool eq>> into ==
	private static void rule39(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.EQUAL), null));
	}

	//Turning <<bool eq>> into !=
	private static void rule40(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.NEQUAL), null));
	}

	//Turning <<bool log>> into &&
	private static void rule41(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.AND), null));
	}

	//Turning <<bool log>> into ||
	private static void rule42(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.OR), null));
	}

	//Turning <<rel expr>> into <<arith expr>> <<rel expr'>>
	private static void rule43(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexprprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexpr, null));
	}

	//Turning <<rel expr>> into true
	private static void rule44(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.TRUE), null));

	}

	//Turning <<rel expr>> into false
	private static void rule45(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.FALSE), null));
	}

	//Turning <<rel expr'>> into <<rel op>> <<arith expr>>
	private static void rule46(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relop, null));
	}

	//Turning <<rel op>> into <
	private static void rule48(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LT), null));
	}

	//Turning <<rel op>> into <=
	private static void rule49(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LE), null));
	}

	//Turning <<rel op>> into >
	private static void rule50(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.GT), null));
	}

	//Turning <<rel op>> into >=
	private static void rule51(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.GE), null));
	}

	//Turning <<arith expr>> into <<term>> <<arith expr'>>
	private static void rule52(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexprprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.term, null));
	}

	//Turning <<arith expr'>> into + <<term>> <<arith expr'>>
	private static void rule53(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexprprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.term, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.PLUS), null));
	}

	//Turning <<arith expr'>> into - <<term>> <<arith expr'>>
	private static void rule54(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexprprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.term, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.MINUS), null));
	}

	//Turning <<term>> into <<factor>> <<term'>>
	private static void rule56(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.termprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.factor, null));
	}

	//Turning <<term'>> into * <<factor>> <<term'>>
	private static void rule57(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.termprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.factor, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.TIMES), null));
	}

	//Turning <<term'>> into / <<factor>> <<term'>>
	private static void rule58(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.termprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.factor, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.DIVIDE), null));
	}

	//Turning <<term'>> into % <<factor>> <<term'>>
	private static void rule59(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.termprime, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.factor, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.MOD), null));
	}

	//Turning <<factor>> into ( <<arith expr>> )
	private static void rule61(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.RPAREN), null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.arithexpr, null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.LPAREN), null));
	}

	//Turning <<factor>> into <<ID>>
	private static void rule62(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.ID), null));
	}

	//Turning <<factor>> into <<NUM>>
	private static void rule63(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.NUM), null));
	}

	//Turning <<print expr>> into <<rel expr>> <<bool expr>>
	private static void rule64(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.boolexpr, null));
		stack.push(new TreeNode(TreeNode.Label.endvar, null));
		stack.push(new TreeNode(TreeNode.Label.relexpr, null));
	}

	//Turning <<print expr>> into " <<string lit>> "
	private static void rule65(Stack<TreeNode> stack) throws SyntaxException {
		stack.pop();

		//add from right to left
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.DQUOTE), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.STRINGLIT), null));
		stack.push(new TreeNode(TreeNode.Label.terminal, new Token(Token.TokenType.DQUOTE), null));
	}
}

// The following class may be helpful.

class Pair<A, B> {
	private final A a;
	private final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A fst() {
		return a;
	}

	public B snd() {
		return b;
	}

	@Override
	public int hashCode() {
		return 3 * a.hashCode() + 7 * b.hashCode();
	}

	@Override
	public String toString() {
		return "{" + a + ", " + b + "}";
	}

	@Override
	public boolean equals(Object o) {
		if ((o instanceof Pair<?, ?>)) {
			Pair<?, ?> other = (Pair<?, ?>) o;
			return other.fst().equals(a) && other.snd().equals(b);
		}

		return false;
	}

}
